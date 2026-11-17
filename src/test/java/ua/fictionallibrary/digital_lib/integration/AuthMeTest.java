package ua.fictionallibrary.digital_lib.integration;

import org.junit.jupiter.api.Test;
import ua.fictionallibrary.digital_lib.user.UserRole;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthMeTest extends AbstractIT {

    private static final String URL = "/api/v1/auth/me";

    @Test
    void me_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withToken_shouldReturnCurrentUser() throws Exception {
        String username = createUser(UserRole.BOOK_DELIVERY_DEPARTMENT);

        mockMvc.perform(get(URL).with(bearerFor(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("BOOK_DELIVERY_DEPARTMENT"));
    }
}