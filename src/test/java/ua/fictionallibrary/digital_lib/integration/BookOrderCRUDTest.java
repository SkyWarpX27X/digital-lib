package ua.fictionallibrary.digital_lib.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ua.fictionallibrary.digital_lib.bookorder.UpdateStatusCommand;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderRequest;
import ua.fictionallibrary.digital_lib.user.UserRole;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class BookOrderCRUDTest extends AbstractIT {

    private static final String URL = "/api/v1/book-orders";

    private String readerUsername;
    private UUID readerId;
    private UUID physicalBookId;

    @BeforeEach
    void setUp() {
        readerUsername = createUser(UserRole.READER);
        readerId = idOf(readerUsername);
        jdbc.sql("""
                INSERT INTO library_cards (owner_id, email, post_code, birth_year, living_address)
                VALUES (:ownerId, :email, '01001', 1990, 'Kyiv, Khreshchatyk 1')
                """)
                .param("ownerId", readerId)
                .param("email", "reader@digital-lib.com")
                .update();
        physicalBookId = insertPhysicalBook();
    }

    @Test
    void create_shouldSaveOrder() throws Exception {
        mockMvc.perform(post(URL)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(readerId, "delivery@digital-lib.com", physicalBookId, true)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.creatorId").value(readerId.toString()))
                .andExpect(jsonPath("$.book").value(physicalBookId.toString()));
        flush();

        Map<String, Object> row = jdbc.sql("SELECT * FROM book_orders WHERE book_id = :bookId")
                .param("bookId", physicalBookId)
                .query()
                .singleRow();

        assertThat(row.get("user_id")).isEqualTo(readerId);
        assertThat(row.get("email_for_deliver")).isEqualTo("delivery@digital-lib.com");
        assertThat(row.get("is_open")).isEqualTo(true);
    }

    @Test
    void create_withoutEmail_shouldUseLibraryCardEmail() throws Exception {
        mockMvc.perform(post(URL)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(readerId, null, physicalBookId, true)))
                .andExpect(status().isCreated());
        flush();

        String email = jdbc.sql("SELECT email_for_deliver FROM book_orders WHERE book_id = :bookId")
                .param("bookId", physicalBookId)
                .query(String.class)
                .single();

        assertThat(email).isEqualTo("reader@digital-lib.com");
    }

    @Test
    void create_alreadyClosed_shouldReturn400() throws Exception {
        mockMvc.perform(post(URL)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(readerId, null, physicalBookId, false)))
                .andExpect(status().isBadRequest());

        assertThat(countByBook(physicalBookId)).isZero();
    }

    @Test
    void create_withoutBook_shouldReturn400() throws Exception {
        mockMvc.perform(post(URL)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(readerId, null, null, true)))
                .andExpect(status().isBadRequest());

        assertThat(countByUser(readerId)).isZero();
    }

    @Test
    void create_withoutLibraryCard_shouldReturn422() throws Exception {
        String username = createUser(UserRole.READER);
        UUID userId = idOf(username);

        mockMvc.perform(post(URL)
                        .with(bearerFor(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(userId, "delivery@digital-lib.com", physicalBookId, true)))
                .andExpect(status().isUnprocessableContent());

        assertThat(countByBook(physicalBookId)).isZero();
    }

    @Test
    void create_forAlreadyOrderedBook_shouldReturn409() throws Exception {
        insertBookOrder(readerId, physicalBookId, true);

        mockMvc.perform(post(URL)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(readerId, null, physicalBookId, true)))
                .andExpect(status().isConflict());

        assertThat(countByBook(physicalBookId)).isOne();
    }

    @Test
    void create_forUnknownBook_shouldReturn404() throws Exception {
        mockMvc.perform(post(URL)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(readerId, null, UUID.randomUUID(), true)))
                .andExpect(status().isNotFound());

        assertThat(countByUser(readerId)).isZero();
    }

    @Test
    void create_asNotReader_shouldReturn403() throws Exception {
        mockMvc.perform(post(URL)
                        .with(asBookDeliveryDepartment())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(readerId, null, physicalBookId, true)))
                .andExpect(status().isForbidden());

        assertThat(countByBook(physicalBookId)).isZero();
    }

    @Test
    void create_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(readerId, null, physicalBookId, true)))
                .andExpect(status().isUnauthorized());

        assertThat(countByBook(physicalBookId)).isZero();
    }

    @Test
    void getById_shouldReturnStoredOrder() throws Exception {
        UUID id = insertBookOrder(readerId, physicalBookId, true);

        mockMvc.perform(get(URL + "/{id}", id).with(bearerFor(readerUsername)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.creatorId").value(readerId.toString()))
                .andExpect(jsonPath("$.book").value(physicalBookId.toString()))
                .andExpect(jsonPath("$.emailForDelivery").value("order@digital-lib.com"));
    }

    @Test
    void getById_unknown_shouldReturn404() throws Exception {
        mockMvc.perform(get(URL + "/{id}", UUID.randomUUID()).with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_byDefault_shouldReturnOnlyOpenOrders() throws Exception {
        UUID open = insertBookOrder(readerId, physicalBookId, true);
        UUID closed = insertBookOrder(readerId, insertPhysicalBook(), false);

        mockMvc.perform(get(URL).with(asBookDeliveryDepartment()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(open)).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(closed)).doesNotExist());
    }

    @Test
    void getAll_withOnlyOpenFalse_shouldReturnAllOrders() throws Exception {
        UUID open = insertBookOrder(readerId, physicalBookId, true);
        UUID closed = insertBookOrder(readerId, insertPhysicalBook(), false);

        mockMvc.perform(get(URL).param("onlyOpen", "false").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(open)).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(closed)).exists());
    }

    @Test
    void getAll_asReader_shouldReturn403() throws Exception {
        mockMvc.perform(get(URL).with(bearerFor(readerUsername)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_close_shouldChangeRow() throws Exception {
        UUID id = insertBookOrder(readerId, physicalBookId, true);

        mockMvc.perform(patch(URL + "/{id}", id)
                        .with(asBookDeliveryDepartment())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isOpen").value(false));
        flush();

        assertThat(isOpen(id)).isFalse();
    }

    @Test
    void updateStatus_reopenClosed_shouldReturn422AndKeepRow() throws Exception {
        UUID id = insertBookOrder(readerId, physicalBookId, false);

        mockMvc.perform(patch(URL + "/{id}", id)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson(true)))
                .andExpect(status().isUnprocessableContent());

        assertThat(isOpen(id)).isFalse();
    }

    @Test
    void updateStatus_asReader_shouldReturn403AndKeepRow() throws Exception {
        UUID id = insertBookOrder(readerId, physicalBookId, true);

        mockMvc.perform(patch(URL + "/{id}", id)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson(false)))
                .andExpect(status().isForbidden());

        assertThat(isOpen(id)).isTrue();
    }

    @Test
    void updateStatus_unknownId_shouldReturn404() throws Exception {
        mockMvc.perform(patch(URL + "/{id}", UUID.randomUUID())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson(false)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_isNotSupported_shouldReturn403AndKeepRow() throws Exception {
        UUID id = insertBookOrder(readerId, physicalBookId, true);

        mockMvc.perform(delete(URL + "/{id}", id).with(asAdmin()))
                .andExpect(status().isForbidden());

        assertThat(jdbc.sql("SELECT count(*) FROM book_orders WHERE id = :id").param("id", id)
                .query(Integer.class).single()).isOne();
    }

    private String orderJson(UUID creatorId, String email, UUID bookId, boolean isOpen) {
        return objectMapper.writeValueAsString(new BookOrderRequest(creatorId, email, bookId, isOpen));
    }

    private String statusJson(boolean isOpen) {
        return objectMapper.writeValueAsString(new UpdateStatusCommand(isOpen));
    }

    private UUID idOf(String username) {
        return jdbc.sql("SELECT id FROM users WHERE username = :username").param("username", username)
                .query(UUID.class).single();
    }

    private boolean isOpen(UUID id) {
        return jdbc.sql("SELECT is_open FROM book_orders WHERE id = :id").param("id", id)
                .query(Boolean.class).single();
    }

    private int countByBook(UUID bookId) {
        return jdbc.sql("SELECT count(*) FROM book_orders WHERE book_id = :bookId").param("bookId", bookId)
                .query(Integer.class).single();
    }

    private int countByUser(UUID userId) {
        return jdbc.sql("SELECT count(*) FROM book_orders WHERE user_id = :userId").param("userId", userId)
                .query(Integer.class).single();
    }


    private UUID insertPhysicalBook() {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO physical_books
                  (id, name, description, topic, publishing_year, language, resource_type)
                VALUES (:id, :name, 'desc', 'Історична', 1800, 'eng', 'Стародрук')
                """)
                .param("id", id)
                .param("name", "Physical " + id)
                .update();
        return id;
    }

    private UUID insertBookOrder(UUID userId, UUID bookId, boolean isOpen) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO book_orders (id, user_id, email_for_deliver, book_id, is_open)
                VALUES (:id, :userId, 'order@digital-lib.com', :bookId, :isOpen)
                """)
                .param("id", id)
                .param("userId", userId)
                .param("bookId", bookId)
                .param("isOpen", isOpen)
                .update();
        return id;
    }
}
