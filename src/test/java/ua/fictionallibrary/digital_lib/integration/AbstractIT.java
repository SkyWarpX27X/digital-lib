package ua.fictionallibrary.digital_lib.integration;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.ObjectMapper;
import ua.fictionallibrary.digital_lib.security.AppUserDetails;
import ua.fictionallibrary.digital_lib.security.UserDetailsService;
import ua.fictionallibrary.digital_lib.security.jwt.JwtService;
import ua.fictionallibrary.digital_lib.support.IntegrationTest;
import ua.fictionallibrary.digital_lib.user.UserRole;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@IntegrationTest
@Transactional
public abstract class AbstractIT {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcClient jdbc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected UserDetailsService userDetailsService;

    @Autowired
    protected EntityManager entityManager;

    protected RequestPostProcessor bearer(UserRole role) {
        String username = createUser(role);
        return bearerFor(username);
    }

    protected RequestPostProcessor bearerFor(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(userDetails);
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return request;
        };
    }

    protected String createUser(UserRole role) {
        String username = role.name().toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.sql("""
                INSERT INTO users (id, username, password, name, surname, patronymic, role, is_active)
                VALUES (:id, :username, :password, :name, :surname, :patronymic, :role, :isActive)
                """)
                .param("id", UUID.randomUUID())
                .param("username", username)
                .param("password", "randompwd")
                .param("name", "John")
                .param("surname", "Doe")
                .param("patronymic", "Johnovich")
                .param("role", role.name())
                .param("isActive", true)
                .update();
        return username;
    }

    protected RequestPostProcessor asReader() {
        return bearer(UserRole.READER);
    }

    protected RequestPostProcessor asAdmin() {
        return bearer(UserRole.ADMIN);
    }

    protected RequestPostProcessor asDigitalArchiveDepartment() {
        return bearer(UserRole.DIGITAL_ARCHIVE_DEPARTMENT);
    }

    protected RequestPostProcessor asBookDeliveryDepartment() {
        return bearer(UserRole.BOOK_DELIVERY_DEPARTMENT);
    }

    protected void flush() {
        entityManager.flush();
    }
}
