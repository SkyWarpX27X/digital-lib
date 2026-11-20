package ua.fictionallibrary.digital_lib.integration;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.core.exception.SdkClientException;
import ua.fictionallibrary.digital_lib.user.UserRole;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ActuatorSecurityIT extends AbstractIT {

    @MockitoBean
    private S3Template s3Template;

    @BeforeEach
    void s3IsAvailable() {
        given(s3Template.bucketExists(anyString())).willReturn(true);
    }

    @Test
    void health_anonymous_shouldReturn200WithoutDetails() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void healthProbes_anonymous_shouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());
    }

    @Test
    void info_anonymous_shouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/info")).andExpect(status().isOk());
    }

    @Test
    void health_asAdmin_shouldShowDetails() throws Exception {
        mockMvc.perform(get("/actuator/health").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.db.status").value("UP"))
                .andExpect(jsonPath("$.components.s3.status").value("UP"))
                .andExpect(jsonPath("$.components.s3.details.bucket").exists());
    }

    // --- S3 health indicator ---

    @Test
    void health_s3Unreachable_shouldBeDownForAdminWithDetails() throws Exception {
        given(s3Template.bucketExists(anyString())).willThrow(SdkClientException.create("Unable to connect"));

        mockMvc.perform(get("/actuator/health").with(asAdmin()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components.s3.status").value("DOWN"))
                .andExpect(jsonPath("$.components.s3.details.error").exists());
    }

    @Test
    void health_bucketMissing_shouldBeDown() throws Exception {
        given(s3Template.bucketExists(anyString())).willReturn(false);

        mockMvc.perform(get("/actuator/health").with(asAdmin()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.components.s3.status").value("DOWN"))
                .andExpect(jsonPath("$.components.s3.details.error").value("Бакет не існує"));
    }

    @Test
    void health_s3Unreachable_anonymousSeesOnlyStatus() throws Exception {
        given(s3Template.bucketExists(anyString())).willThrow(SdkClientException.create("Unable to connect"));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void readiness_s3Unreachable_shouldStayUp() throws Exception {
        given(s3Template.bucketExists(anyString())).willThrow(SdkClientException.create("Unable to connect"));

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void managementEndpoints_anonymous_shouldReturn401() throws Exception {
        for (String url : new String[]{"/actuator", "/actuator/metrics", "/actuator/loggers", "/actuator/env", "/actuator/beans"}) {
            mockMvc.perform(get(url)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void managementEndpoints_nonAdminRoles_shouldReturn403() throws Exception {
        for (UserRole role : new UserRole[]{UserRole.READER, UserRole.DIGITAL_ARCHIVE_DEPARTMENT, UserRole.BOOK_DELIVERY_DEPARTMENT}) {
            mockMvc.perform(get("/actuator/metrics").with(bearer(role)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void managementEndpoints_invalidToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/actuator/metrics").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void managementEndpoints_asAdminWithJwt_shouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/metrics").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }

    @Test
    void loggers_asAdmin_canChangeLogLevel() throws Exception {
        mockMvc.perform(post("/actuator/loggers/ua.fictionallibrary")
                        .with(asAdmin())
                        .contentType("application/json")
                        .content("{\"configuredLevel\":\"INFO\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void loggers_asReader_cannotChangeLogLevel() throws Exception {
        mockMvc.perform(post("/actuator/loggers/ua.fictionallibrary")
                        .with(asReader())
                        .contentType("application/json")
                        .content("{\"configuredLevel\":\"TRACE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void managementEndpoints_httpBasic_isNotAccepted() throws Exception {
        String username = createUser(UserRole.ADMIN);
        mockMvc.perform(get("/actuator/metrics").with(httpBasic(username, "randompwd")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownActuatorPath_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/actuator/does-not-exist")).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownActuatorPath_asAdmin_shouldReturn403() throws Exception {
        mockMvc.perform(get("/actuator/does-not-exist").with(asAdmin())).andExpect(status().isForbidden());
    }

    @Test
    void businessApi_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/users"))
                .andExpect(jsonPath("$.properties").doesNotExist());
        mockMvc.perform(get("/api/v1/book-orders")).andExpect(status().isUnauthorized());
    }

    @Test
    void businessApi_adminOnly_asReader_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/users").with(asReader()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.path").value("/api/v1/users"))
                .andExpect(jsonPath("$.properties").doesNotExist());
    }
}
