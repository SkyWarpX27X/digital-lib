package ua.fictionallibrary.digital_lib.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ua.fictionallibrary.digital_lib.user.UserRole;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LibraryCardListIT extends AbstractIT {

    private static final String URL = "/api/v1/library-cards";

    @Test
    void asAdmin_returnsCardsWithOwnerName() throws Exception {
        String username = createUser(UserRole.READER);
        UUID ownerId = userId(username);
        String email = username + "@example.com";
        mockMvc.perform(post("/api/v1/users/{id}/library-card", ownerId).with(bearerFor(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "postcode": "01001", "birthYear": "1990", "livingAddress": "Kyiv"}
                                """.formatted(email)))
                .andExpect(status().isCreated());

        String body = mockMvc.perform(get(URL).with(asAdmin())).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Map<String, Object>> ours = JsonPath.read(body, "$[?(@.ownerId == '" + ownerId + "')]");
        assertThat(ours).hasSize(1);
        assertThat(ours.getFirst())
                .containsEntry("surname", "Doe")
                .containsEntry("name", "John")
                .containsEntry("patronymic", "Johnovich")
                .containsEntry("email", email)
                .containsEntry("birthYear", "1990")
                .containsEntry("livingAddress", "Kyiv");
    }

    @Test
    void asReader_returns403() throws Exception {
        mockMvc.perform(get(URL).with(asReader())).andExpect(status().isForbidden());
    }

    @Test
    void anonymous_returns401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void userResponse_containsUsername() throws Exception {
        String username = createUser(UserRole.BOOK_DELIVERY_DEPARTMENT);
        String body = mockMvc.perform(get("/api/v1/users/{id}", userId(username)).with(asAdmin()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat((String) JsonPath.read(body, "$.username")).isEqualTo(username);
    }
}
