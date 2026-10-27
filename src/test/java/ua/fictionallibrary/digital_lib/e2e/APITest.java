package ua.fictionallibrary.digital_lib.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBook;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookResponse;
import ua.fictionallibrary.digital_lib.security.AuthResponse;
import ua.fictionallibrary.digital_lib.support.E2ETest;

import java.time.Year;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@E2ETest
@AutoConfigureRestTestClient
public class APITest {
    @Autowired
    private RestTestClient client;

    @Autowired
    private JdbcClient jdbc;

    private String login(String username, String password) {
        AuthResponse response = client.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"username": "%s", "password": "%s"}
                        """.formatted(username, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        return response.token();
    }

    @Test
    void adminCreatesPhysicalBook_ReaderCanAccess() {
        String adminToken = login("queen", "M@m@@@@@@@!!!!___");

        client.post().uri("/api/v1/physical-books")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"name": "Idea medicinae philosophicae", "authors": ["Petro Severino"],
                        "description": "description", "topic": "Історія медицини", "publishingYear": "1660", "language": "lat",
                        "resourceType": "Стародрук"}
                       """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Idea medicinae philosophicae");

        assertThat(jdbc.sql("SELECT COUNT(*) FROM physical_books").query(Integer.class).single()).isEqualTo(1);

        UUID bookId = jdbc.sql("SELECT id FROM physical_books WHERE name = 'Idea medicinae philosophicae'")
                .query(UUID.class).single();

        String readerToken = login("username666", "abba123");

        PhysicalBookResponse book = client.get().uri("/api/v1/physical-books/{id}", bookId)
                .headers(headers -> headers.setBearerAuth(readerToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(PhysicalBookResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(book).isEqualTo(new PhysicalBookResponse(bookId, "Idea medicinae philosophicae",
                List.of("Petro Severino"), "description", "Історія медицини", Year.of(1660),
                "lat", "Стародрук"));
    }

}
