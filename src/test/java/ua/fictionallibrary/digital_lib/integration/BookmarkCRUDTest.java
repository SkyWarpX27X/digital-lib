package ua.fictionallibrary.digital_lib.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ua.fictionallibrary.digital_lib.bookmark.UpdatePageNumberCommand;
import ua.fictionallibrary.digital_lib.bookmark.dto.BookmarkRequest;
import ua.fictionallibrary.digital_lib.user.UserRole;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class BookmarkCRUDTest extends AbstractIT {

    private static final String URL = "/api/v1/bookmark";

    private String readerUsername;
    private UUID readerId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        readerUsername = createUser(UserRole.READER);
        readerId = jdbc.sql("SELECT id FROM users WHERE username = :username").param("username", readerUsername)
                .query(UUID.class).single();
        bookId = UUID.randomUUID();
    }

    @Test
    void create_shouldStoreRow() throws Exception {
        mockMvc.perform(post(URL)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookmarkJson(bookId, 42)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(readerId.toString()))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.pageNumber").value(42));
        flush();

        assertThat(pageNumber(readerId, bookId)).isEqualTo(42);
    }

    @Test
    void create_existingBookmark_shouldReturn409() throws Exception {
        insertBookmark(readerId, bookId, 10);

        mockMvc.perform(post(URL)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookmarkJson(bookId, 99)))
                .andExpect(status().isConflict());

        assertThat(count(readerId, bookId)).isOne();
        assertThat(pageNumber(readerId, bookId)).isEqualTo(10);
    }

    @Test
    void create_withoutBook_shouldReturn400() throws Exception {
        mockMvc.perform(post(URL)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookmarkJson(null, 5)))
                .andExpect(status().isBadRequest());

        assertThat(jdbc.sql("SELECT count(*) FROM bookmarks WHERE user_id = :userId").param("userId", readerId)
                .query(Integer.class).single()).isZero();
    }

    @Test
    void create_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookmarkJson(bookId, 5)))
                .andExpect(status().isUnauthorized());

        assertThat(count(readerId, bookId)).isZero();
    }

    @Test
    void get_shouldReturnBookmark() throws Exception {
        insertBookmark(readerId, bookId, 17);

        mockMvc.perform(get(URL + "/{userId}/{bookId}", readerId, bookId).with(bearerFor(readerUsername)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(readerId.toString()))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.pageNumber").value(17));
    }

    @Test
    void get_asBookDeliveryDepartment_shouldReturn403() throws Exception {
        insertBookmark(readerId, bookId, 17);

        mockMvc.perform(get(URL + "/{userId}/{bookId}", readerId, bookId).with(asBookDeliveryDepartment()))
                .andExpect(status().isForbidden());
    }

    @Test
    void get_unknown_shouldReturn404() throws Exception {
        mockMvc.perform(get(URL + "/{userId}/{bookId}", readerId, UUID.randomUUID()).with(bearerFor(readerUsername)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePage_shouldChangeRow() throws Exception {
        insertBookmark(readerId, bookId, 10);

        mockMvc.perform(patch(URL + "/{userId}/{bookId}", readerId, bookId)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pageJson(120)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(120));
        flush();

        assertThat(pageNumber(readerId, bookId)).isEqualTo(120);
    }

    @Test
    void updatePage_asDigitalArchiveDepartment_shouldReturn403() throws Exception {
        insertBookmark(readerId, bookId, 10);

        mockMvc.perform(patch(URL + "/{userId}/{bookId}", readerId, bookId)
                        .with(asDigitalArchiveDepartment())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pageJson(55)))
                .andExpect(status().isForbidden());

        assertThat(pageNumber(readerId, bookId)).isEqualTo(10);
    }

    @Test
    void get_otherReadersBookmark_shouldReturn403() throws Exception {
        insertBookmark(readerId, bookId, 17);

        mockMvc.perform(get(URL + "/{userId}/{bookId}", readerId, bookId).with(asReader()))
                .andExpect(status().isForbidden());
    }

    @Test
    void get_otherReadersBookmark_asAdmin_shouldSucceed() throws Exception {
        insertBookmark(readerId, bookId, 17);

        mockMvc.perform(get(URL + "/{userId}/{bookId}", readerId, bookId).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(17));
    }

    @Test
    void updatePage_otherReadersBookmark_shouldReturn403AndKeepRow() throws Exception {
        insertBookmark(readerId, bookId, 10);

        mockMvc.perform(patch(URL + "/{userId}/{bookId}", readerId, bookId)
                        .with(asReader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pageJson(55)))
                .andExpect(status().isForbidden());

        assertThat(pageNumber(readerId, bookId)).isEqualTo(10);
    }

    @Test
    void create_withUserIdInBody_shouldReturn400() throws Exception {
        UUID otherId = UUID.randomUUID();

        mockMvc.perform(post(URL)
                        .with(bearerFor(readerUsername))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": "%s", "bookId": "%s", "pageNumber": 3}
                                """.formatted(otherId, bookId)))
                .andExpect(status().isBadRequest());

        assertThat(count(otherId, bookId)).isZero();
        assertThat(count(readerId, bookId)).isZero();
    }

    private String bookmarkJson(UUID bookId, int pageNumber) {
        return objectMapper.writeValueAsString(new BookmarkRequest(bookId, pageNumber));
    }

    private String pageJson(int pageNumber) {
        return objectMapper.writeValueAsString(new UpdatePageNumberCommand(pageNumber));
    }

    private int pageNumber(UUID userId, UUID bookId) {
        return jdbc.sql("SELECT page_number FROM bookmarks WHERE user_id = :userId AND book_id = :bookId")
                .param("userId", userId).param("bookId", bookId)
                .query(Integer.class).single();
    }

    private int count(UUID userId, UUID bookId) {
        return jdbc.sql("SELECT count(*) FROM bookmarks WHERE user_id = :userId AND book_id = :bookId")
                .param("userId", userId).param("bookId", bookId)
                .query(Integer.class).single();
    }

    private void insertBookmark(UUID userId, UUID bookId, int pageNumber) {
        jdbc.sql("""
                INSERT INTO bookmarks (user_id, book_id, page_number)
                VALUES (:userId, :bookId, :pageNumber)
                """)
                .param("userId", userId)
                .param("bookId", bookId)
                .param("pageNumber", pageNumber)
                .update();
    }
}
