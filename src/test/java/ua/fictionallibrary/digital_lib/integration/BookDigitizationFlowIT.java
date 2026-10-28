package ua.fictionallibrary.digital_lib.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.support.IntegrationTest;
import ua.fictionallibrary.digital_lib.user.UserRole;
import ua.fictionallibrary.digital_lib.user.dto.LibraryCardRequest;
import ua.fictionallibrary.digital_lib.user.dto.UserRequest;

import java.time.Duration;
import java.time.Year;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookDigitizationFlowIT extends AbstractFlowIT {

    private static final String USERS = "users";
    private static final String LIBRARY_CARDS = "library_cards";
    private static final String PHYSICAL_BOOKS = "physical_books";
    private static final String BOOK_ORDERS = "book_orders";
    private static final String USERNAME = "new_user";

    private UUID userId;
    private UUID physicalBookId;
    private UUID bookOrderId;
    @Autowired
    private javax.sql.DataSource dataSource;

    @BeforeAll
    @AfterAll
    void cleanUp() throws Exception {
        try (var conn = dataSource.getConnection()) {
            org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(conn,
                    new org.springframework.core.io.ClassPathResource("sql/reset_db.sql"));
        }
    }


    @Test
    @Order(1)
    void createUser_invalidBody_returns400() throws Exception {
        UserRequest invalid = new UserRequest("", "", "", "", "", UserRole.READER, true);
        post("/api/v1/users", invalid, null).andExpect(status().isBadRequest());
    }

    @Test
    @Order(2)
    void createUser_storesRowNoAuth() throws Exception {
        UserRequest user = new UserRequest(USERNAME, "pwd123", "Walter", "White",
                "Hartwell", UserRole.READER, true);
        post("/api/v1/users", user, null).andExpect(status().isCreated());

        userId = jdbc.sql("SELECT id FROM " + USERS + " WHERE username = :u")
                .param("u", USERNAME).query(UUID.class).single();

        String name = jdbc.sql("SELECT name FROM " + USERS + " WHERE id = :id")
                .param("id", userId).query(String.class).single();
        assertThat(name).isEqualTo("Walter");
    }

    @Test
    @Order(3)
    void createUser_duplicateUsername_isRejected() throws Exception {
        UserRequest dup = new UserRequest(USERNAME, "pwd123", "Jesse", "Pinkman",
                "Bitch", UserRole.READER, true);
        post("/api/v1/users", dup, null).andExpect(status().isConflict());
    }

    @Test
    @Order(4)
    void createBookOrder_withoutLibraryCard_isRejected() throws Exception {
        BookOrderRequest order = new BookOrderRequest(userId, null, UUID.randomUUID(), true);
        post("/api/v1/book-orders", order, bearerFor(USERNAME))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(5)
    void createLibraryCard_unauthenticated_returns401() throws Exception {
        post("/api/v1/users/%s/library-card".formatted(userId), validCard(), null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    void createLibraryCard_asBookDelivery_returns403() throws Exception {
        post("/api/v1/users/%s/library-card".formatted(userId), validCard(), asBookDeliveryDepartment())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void createLibraryCard_invalidEmail_returns400() throws Exception {
        LibraryCardRequest bad = new LibraryCardRequest("not-an-email", "87104", Year.of(2007),
                "308 Negra Arroyo Lane", null, null, null);
        post("/api/v1/users/%s/library-card".formatted(userId), bad, bearerFor(USERNAME))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    void createLibraryCard_storedRowAuthenticated() throws Exception {
        post("/api/v1/users/%s/library-card".formatted(userId), validCard(), bearerFor(USERNAME))
                .andExpect(status().isCreated());

        String email = jdbc.sql("SELECT email FROM " + LIBRARY_CARDS + " WHERE owner_id = :id")
                .param("id", userId).query(String.class).single();
        assertThat(email).isEqualTo("doubleudoubleu@example.com");
    }

    @Test
    @Order(9)
    void createPhysicalBook_asReader_returns403() throws Exception {
        post("/api/v1/physical-books", validPhysicalBook(), bearerFor(USERNAME))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(10)
    void createPhysicalBook_invalidBody_returns400() throws Exception {
        PhysicalBookRequest bad = new PhysicalBookRequest("", List.of(), "", "",
                Year.of(1829), "Uk", "Стародрук");
        post("/api/v1/physical-books", bad, asBookDeliveryDepartment())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    void createPhysicalBook_storedRowAsBookDelivery() throws Exception {
        String body = post("/api/v1/physical-books", validPhysicalBook(), asBookDeliveryDepartment())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        physicalBookId = jdbc.sql("SELECT id FROM " + PHYSICAL_BOOKS + " WHERE name = :name")
                .param("name", "Тигролови").query(UUID.class).single();

        String name = jdbc.sql("SELECT name FROM " + PHYSICAL_BOOKS + " WHERE id = :id")
                .param("id", physicalBookId).query(String.class).single();
        assertThat(name).isEqualTo("Тигролови");
    }

    @Test
    @Order(12)
    void createBookOrder_asBookDelivery_returns403() throws Exception {
        post("/api/v1/book-orders", new BookOrderRequest(userId, null, physicalBookId, true),
                asBookDeliveryDepartment())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(13)
    void createBookOrder_createdAlreadyClosed_returns400() throws Exception {
        post("/api/v1/book-orders", new BookOrderRequest(userId, null, physicalBookId, false),
                bearerFor(USERNAME))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(14)
    void createBookOrder_unknownBook_returns404() throws Exception {
        post("/api/v1/book-orders", new BookOrderRequest(userId, null, UUID.randomUUID(), true),
                bearerFor(USERNAME))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(15)
    void createBookOrder_storedRowWhenHasLibraryCard() throws Exception {
        String body = post("/api/v1/book-orders", new BookOrderRequest(userId, null, physicalBookId, true),
                bearerFor(USERNAME))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        bookOrderId = UUID.fromString(JsonPath.read(body, "$.id"));

        Boolean open = jdbc.sql("SELECT is_open FROM " + BOOK_ORDERS + " WHERE id = :id")
                .param("id", bookOrderId).query(Boolean.class).single();
        assertThat(open).isTrue();

        String email = jdbc.sql("SELECT email_for_deliver FROM " + BOOK_ORDERS + " WHERE id = :id")
                .param("id", bookOrderId).query(String.class).single();
        assertThat(email).isEqualTo("doubleudoubleu@example.com");
    }

    @Test
    @Order(16)
    void createBookOrder_sameBookTwice_returns409() throws Exception {
        post("/api/v1/book-orders", new BookOrderRequest(userId, null, physicalBookId, true),
                bearerFor(USERNAME))
                .andExpect(status().isConflict());
    }


    @Test
    @Order(17)
    void createDigitizedBook_asReader_returns403() throws Exception {
        post("/api/v1/digitized-books", validDigitizedBook(physicalBookId), bearerFor(USERNAME))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(18)
    void createDigitizedBook_withoutPhysicalBookId_returns400() throws Exception {
        post("/api/v1/digitized-books", validDigitizedBook(null), asDigitalArchiveDepartment())
                .andExpect(status().isBadRequest());
        assertThat(orderIsOpen()).isTrue();
    }

    @Test
    @Order(19)
    void createDigitizedBook_asDigitalArchiveUpdatedOrderStatusAndRemovedPhysicalBook() throws Exception {
        String json = objectMapper.writeValueAsString(validDigitizedBook(physicalBookId));
        System.out.println("DEBUG json = " + json);System.out.println("DEBUG physicalBookId = " + physicalBookId);
        String body = post("/api/v1/digitized-books", validDigitizedBook(physicalBookId),
                asDigitalArchiveDepartment())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(orderIsOpen()).isFalse());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(jdbc.sql("SELECT count(*) FROM " + PHYSICAL_BOOKS + " WHERE id = :id")
                        .param("id", physicalBookId).query(Long.class).single()).isZero());
    }

    @Test
    @Order(20)
    void updateOrderStatus_reopeningClosedOrder_isRejected() throws Exception {
        mockMvc.perform(patch("/api/v1/book-orders/%s".formatted(bookOrderId))
                        .with(asBookDeliveryDepartment())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isOpen\": true}"))
                .andExpect(status().is4xxClientError());
    }

    private ResultActions post(String url, Object body, RequestPostProcessor auth)
            throws Exception {
        var request = MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        if (auth != null) request.with(auth);
        return mockMvc.perform(request).andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.log());
    }

    private boolean orderIsOpen() {
        return jdbc.sql("SELECT is_open FROM " + BOOK_ORDERS + " WHERE id = :id")
                .param("id", bookOrderId).query(Boolean.class).single();
    }

    private void delete(String table, String column, UUID id) {
        jdbc.sql("DELETE FROM " + table + " WHERE " + column + " = :id").param("id", id).update();
    }

    private LibraryCardRequest validCard() {
        return new LibraryCardRequest("doubleudoubleu@example.com", "87104", Year.of(2007),
                "308 Negra Arroyo Lane, Albuquerque, New Mexico",
                "Some Highschool in New Mexico", "The Meth Factory", "Chemistry Teacher");
    }

    private PhysicalBookRequest validPhysicalBook() {
        return new PhysicalBookRequest("Тигролови", List.of("Іван Багряний"),
                "Пригодницький роман", "Історичний пригодницький роман",
                Year.of(1944), "Українська", "Книга");
    }

    private DigitizedBookRequest validDigitizedBook(UUID physicalId) {
        return new DigitizedBookRequest("Тигролови", List.of("Іван Багряний"),
                "Пригодницький роман", "Історичний пригодницький роман",
                Year.of(1944), "Українська", "Книга", false,
                "https://digital-lib.com/image", "https://digital-lib.com/file", physicalId);
    }
}
