package ua.fictionallibrary.digital_lib.integration;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.ObjectMapper;
import ua.fictionallibrary.digital_lib.filestorage.FileStorageService;
import ua.fictionallibrary.digital_lib.security.AppUserDetailsService;
import ua.fictionallibrary.digital_lib.security.jwt.JwtService;
import ua.fictionallibrary.digital_lib.user.UserRole;

import java.util.UUID;

public abstract class AbstractITSupport {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcClient jdbc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected AppUserDetailsService appUserDetailsService;

    @Autowired
    protected EntityManager entityManager;

    @MockitoBean
    protected FileStorageService fileStorageService;

    protected RequestPostProcessor bearer(UserRole role) {
        String username = createUser(role);
        return bearerFor(username);
    }

    protected RequestPostProcessor bearerFor(String username) {
        UserDetails userDetails = appUserDetailsService.loadUserByUsername(username);
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

    protected UUID userId(String username) {
        return jdbc.sql("SELECT id FROM users WHERE username = :username").param("username", username)
                .query(UUID.class).single();
    }

    protected UUID insertDigitizedBook(String name) {
        return insertDigitizedBook(name, false);
    }

    protected UUID insertDigitizedBook(String name, boolean copyrighted) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
            INSERT INTO digitized_books
              (id, name, description, topic, publishing_year, language, resource_type, is_copyrighted, pdf_key)
            VALUES (:id, :name, 'desc', 'Історична', 1850, 'eng', 'Стародрук', :copyrighted, :pdfKey)
            """)
                .param("id", id).param("name", name).param("copyrighted", copyrighted)
                .param("pdfKey", "digitized-books/" + id + "/book.pdf")
                .update();
        return id;
    }

    protected void flush() {
        entityManager.flush();
    }
}
