package ua.fictionallibrary.digital_lib.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.fictionallibrary.digital_lib.user.UserRole;
import ua.fictionallibrary.digital_lib.user.dto.UserRequest;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserCRUDTest extends AbstractIT {

    private static final String URL = "/api/v1/users";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createReader_unauthenticated_shouldSucceed() throws Exception {
        String username = uniqueUsername();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(username, "secret123", UserRole.READER)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.role").value("READER"));
        flush();

        Map<String, Object> row = jdbc.sql("SELECT * FROM users WHERE username = :username")
                .param("username", username)
                .query()
                .singleRow();

        assertThat(row.get("name")).isEqualTo("Taras");
        assertThat(row.get("surname")).isEqualTo("Shevchenko");
        assertThat(row.get("patronymic")).isEqualTo("Hryhorovych");
        assertThat(row.get("role")).isEqualTo("READER");
        assertThat(row.get("is_active")).isEqualTo(true);
        assertThat((String) row.get("password"))
                .isNotEqualTo("secret123")
                .satisfies(hash -> assertThat(passwordEncoder.matches("secret123", hash)).isTrue());
    }

    @Test
    void createAdmin_asAdmin_shouldSucceed() throws Exception {
        String username = uniqueUsername();

        mockMvc.perform(post(URL)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(username, "secret123", UserRole.ADMIN)))
                .andExpect(status().isCreated());
        flush();

        assertThat(roleByUsername(username)).isEqualTo("ADMIN");
    }

    @Test
    void createAdmin_unauthenticated_shouldReturn401() throws Exception {
        String username = uniqueUsername();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(username, "secret123", UserRole.ADMIN)))
                .andExpect(status().isUnauthorized());

        assertThat(countByUsername(username)).isZero();
    }

    @Test
    void createDepartmentUser_asReader_shouldReturn403() throws Exception {
        String username = uniqueUsername();

        mockMvc.perform(post(URL)
                        .with(asReader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(username, "secret123", UserRole.BOOK_DELIVERY_DEPARTMENT)))
                .andExpect(status().isForbidden());

        assertThat(countByUsername(username)).isZero();
    }

    @Test
    void create_withExistingUsername_shouldReturn409() throws Exception {
        String username = createUser(UserRole.READER);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(username, "secret123", UserRole.READER)))
                .andExpect(status().isConflict());

        assertThat(countByUsername(username)).isOne();
    }

    @Test
    void create_withBlankPassword_shouldReturn400() throws Exception {
        String username = uniqueUsername();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(username, " ", UserRole.READER)))
                .andExpect(status().isBadRequest());

        assertThat(countByUsername(username)).isZero();
    }

    @Test
    void create_withoutRole_shouldReturn400() throws Exception {
        String username = uniqueUsername();

        mockMvc.perform(post(URL)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(username, "secret123", null)))
                .andExpect(status().isBadRequest());

        assertThat(countByUsername(username)).isZero();
    }

    @Test
    void getById_shouldReturnStoredUser() throws Exception {
        String username = createUser(UserRole.READER);
        UUID id = idOf(username);

        mockMvc.perform(get(URL + "/{id}", id).with(bearerFor(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.role").value("READER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getById_unauthenticated_shouldReturn401() throws Exception {
        UUID id = idOf(createUser(UserRole.READER));

        mockMvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_unknown_shouldReturn404() throws Exception {
        mockMvc.perform(get(URL + "/{id}", UUID.randomUUID()).with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_asAdmin_shouldContainStoredUsers() throws Exception {
        UUID first = idOf(createUser(UserRole.READER));
        UUID second = idOf(createUser(UserRole.DIGITAL_ARCHIVE_DEPARTMENT));

        mockMvc.perform(get(URL).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(first)).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(second)).exists());
    }

    @Test
    void getAll_asReader_shouldReturn403() throws Exception {
        mockMvc.perform(get(URL).with(asReader()))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_self_shouldChangeRow() throws Exception {
        String username = createUser(UserRole.READER);
        UUID id = idOf(username);
        String newUsername = uniqueUsername();

        mockMvc.perform(put(URL + "/{id}", id)
                        .with(bearerFor(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(newUsername, "newSecret", UserRole.READER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Taras"));
        flush();

        Map<String, Object> row = jdbc.sql("SELECT * FROM users WHERE id = :id")
                .param("id", id)
                .query()
                .singleRow();

        assertThat(row.get("username")).isEqualTo(newUsername);
        assertThat(row.get("name")).isEqualTo("Taras");
        assertThat(row.get("surname")).isEqualTo("Shevchenko");
        assertThat(passwordEncoder.matches("newSecret", (String) row.get("password"))).isTrue();
    }

    @Test
    void update_otherUser_asAdmin_shouldChangeRow() throws Exception {
        UUID id = idOf(createUser(UserRole.READER));
        String newUsername = uniqueUsername();

        mockMvc.perform(put(URL + "/{id}", id)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(newUsername, "newSecret", UserRole.BOOK_DELIVERY_DEPARTMENT)))
                .andExpect(status().isOk());
        flush();

        assertThat(roleByUsername(newUsername)).isEqualTo("BOOK_DELIVERY_DEPARTMENT");
    }

    @Test
    void update_otherUser_asReader_shouldReturn403AndKeepRow() throws Exception {
        String username = createUser(UserRole.READER);
        UUID id = idOf(username);

        mockMvc.perform(put(URL + "/{id}", id)
                        .with(asReader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(uniqueUsername(), "newSecret", UserRole.READER)))
                .andExpect(status().isForbidden());

        assertThat(usernameById(id)).isEqualTo(username);
    }

    @Test
    void update_withTakenUsername_shouldReturn409AndKeepRow() throws Exception {
        String username = createUser(UserRole.READER);
        String takenUsername = createUser(UserRole.READER);
        UUID id = idOf(username);

        mockMvc.perform(put(URL + "/{id}", id)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(takenUsername, "newSecret", UserRole.READER)))
                .andExpect(status().isConflict());

        assertThat(usernameById(id)).isEqualTo(username);
    }

    @Test
    void update_withBlankName_shouldReturn400AndKeepRow() throws Exception {
        String username = createUser(UserRole.READER);
        UUID id = idOf(username);

        UserRequest request = new UserRequest(uniqueUsername(), "newSecret", " ", "Shevchenko",
                null, UserRole.READER, true);

        mockMvc.perform(put(URL + "/{id}", id)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(usernameById(id)).isEqualTo(username);
    }

    @Test
    void update_unknownId_shouldReturn404() throws Exception {
        mockMvc.perform(put(URL + "/{id}", UUID.randomUUID())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(uniqueUsername(), "newSecret", UserRole.READER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_isNotSupported_shouldReturn403AndKeepRow() throws Exception {
        UUID id = idOf(createUser(UserRole.READER));

        mockMvc.perform(delete(URL + "/{id}", id).with(asAdmin()))
                .andExpect(status().isForbidden());

        assertThat(jdbc.sql("SELECT count(*) FROM users WHERE id = :id").param("id", id)
                .query(Integer.class).single()).isOne();
    }

    @Test
    void create_unauthenticated_withInactiveFlag_shouldBeActive() throws Exception {
        String username = uniqueUsername();
        UserRequest request = new UserRequest(username, "secret123", "Taras", "Shevchenko",
                null, UserRole.READER, false);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        flush();

        assertThat(isActiveByUsername(username)).isTrue();
    }

    @Test
    void update_self_asReader_cannotChangeRoleOrActive() throws Exception {
        String username = createUser(UserRole.READER);
        UUID id = idOf(username);
        UserRequest request = new UserRequest(username, "newSecret", "Taras", "Shevchenko",
                null, UserRole.ADMIN, false);

        mockMvc.perform(put(URL + "/{id}", id)
                        .with(bearerFor(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("READER"));
        flush();

        assertThat(roleByUsername(username)).isEqualTo("READER");
        assertThat(isActiveByUsername(username)).isTrue();
    }

    @Test
    void getById_otherUser_asReader_shouldReturn403() throws Exception {
        UUID id = idOf(createUser(UserRole.READER));

        mockMvc.perform(get(URL + "/{id}", id).with(asReader()))
                .andExpect(status().isForbidden());
    }

    @Test
    void patch_asAdmin_shouldChangeRoleAndStatus_andKeepPassword() throws Exception {
        String username = createUser(UserRole.READER);
        UUID id = idOf(username);
        String passwordBefore = passwordByUsername(username);

        mockMvc.perform(patch(URL + "/{id}", id)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "DIGITAL_ARCHIVE_DEPARTMENT", "isActive": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("DIGITAL_ARCHIVE_DEPARTMENT"));
        flush();

        assertThat(roleByUsername(username)).isEqualTo("DIGITAL_ARCHIVE_DEPARTMENT");
        assertThat(isActiveByUsername(username)).isFalse();
        assertThat(passwordByUsername(username)).isEqualTo(passwordBefore);
    }

    @Test
    void patch_onlyRole_shouldKeepActive() throws Exception {
        String username = createUser(UserRole.READER);

        mockMvc.perform(patch(URL + "/{id}", idOf(username))
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "BOOK_DELIVERY_DEPARTMENT"}
                                """))
                .andExpect(status().isOk());
        flush();

        assertThat(roleByUsername(username)).isEqualTo("BOOK_DELIVERY_DEPARTMENT");
        assertThat(isActiveByUsername(username)).isTrue();
    }

    @Test
    void patch_asReader_shouldReturn403() throws Exception {
        String username = createUser(UserRole.READER);

        mockMvc.perform(patch(URL + "/{id}", idOf(username))
                        .with(bearerFor(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "ADMIN"}
                                """))
                .andExpect(status().isForbidden());

        assertThat(roleByUsername(username)).isEqualTo("READER");
    }

    @Test
    void patch_selfDeactivation_asAdmin_shouldReturn422() throws Exception {
        String admin = createUser(UserRole.ADMIN);

        mockMvc.perform(patch(URL + "/{id}", idOf(admin))
                        .with(bearerFor(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isActive": false}
                                """))
                .andExpect(status().isUnprocessableContent());

        assertThat(isActiveByUsername(admin)).isTrue();
    }

    @Test
    void patch_unknown_shouldReturn404() throws Exception {
        mockMvc.perform(patch(URL + "/{id}", UUID.randomUUID())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isActive": false}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivatedUser_withOldToken_shouldReturn401() throws Exception {
        String username = createUser(UserRole.READER);
        UUID id = idOf(username);
        var oldToken = bearerFor(username);

        mockMvc.perform(get(URL + "/{id}", id).with(oldToken))
                .andExpect(status().isOk());

        jdbc.sql("UPDATE users SET is_active = false WHERE id = :id").param("id", id).update();
        entityManager.clear();

        mockMvc.perform(get(URL + "/{id}", id).with(oldToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_deactivatedUser_shouldReturn401() throws Exception {
        String username = createUser(UserRole.READER);
        jdbc.sql("UPDATE users SET password = :password WHERE username = :username")
                .param("password", passwordEncoder.encode("secret123"))
                .param("username", username)
                .update();
        String loginJson = """
                {"username": "%s", "password": "secret123"}
                """.formatted(username);

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson))
                .andExpect(status().isOk());

        jdbc.sql("UPDATE users SET is_active = false WHERE username = :username").param("username", username).update();
        entityManager.clear();

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    private boolean isActiveByUsername(String username) {
        return jdbc.sql("SELECT is_active FROM users WHERE username = :username").param("username", username)
                .query(Boolean.class).single();
    }

    private String passwordByUsername(String username) {
        return jdbc.sql("SELECT password FROM users WHERE username = :username").param("username", username)
                .query(String.class).single();
    }

    private static String uniqueUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String userJson(String username, String password, UserRole role) {
        UserRequest request = new UserRequest(
                username,
                password,
                "Taras",
                "Shevchenko",
                "Hryhorovych",
                role,
                true
        );
        return objectMapper.writeValueAsString(request);
    }

    private UUID idOf(String username) {
        return jdbc.sql("SELECT id FROM users WHERE username = :username").param("username", username)
                .query(UUID.class).single();
    }

    private String usernameById(UUID id) {
        return jdbc.sql("SELECT username FROM users WHERE id = :id").param("id", id)
                .query(String.class).single();
    }

    private String roleByUsername(String username) {
        return jdbc.sql("SELECT role FROM users WHERE username = :username").param("username", username)
                .query(String.class).single();
    }

    private int countByUsername(String username) {
        return jdbc.sql("SELECT count(*) FROM users WHERE username = :username").param("username", username)
                .query(Integer.class).single();
    }
}
