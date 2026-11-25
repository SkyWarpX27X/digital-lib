package ua.fictionallibrary.digital_lib.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.fictionallibrary.digital_lib.user.UserRole;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FavoriteCRUDTest extends AbstractIT {

    private static final String URL = "/api/v1/favorites";

    private String readerUsername;
    private UUID readerId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        readerUsername = createUser(UserRole.READER);
        readerId = userId(readerUsername);
        bookId = insertDigitizedBook("Улюблена книга");
    }

    @Test
    void add_shouldStoreRow() throws Exception {
        mockMvc.perform(put(URL + "/{bookId}", bookId).with(bearerFor(readerUsername)))
                .andExpect(status().isNoContent());
        flush();

        assertThat(count(readerId, bookId)).isOne();
    }

    @Test
    void add_twice_shouldKeepOneRow() throws Exception {
        mockMvc.perform(put(URL + "/{bookId}", bookId).with(bearerFor(readerUsername)))
                .andExpect(status().isNoContent());
        mockMvc.perform(put(URL + "/{bookId}", bookId).with(bearerFor(readerUsername)))
                .andExpect(status().isNoContent());
        flush();

        assertThat(count(readerId, bookId)).isOne();
    }

    @Test
    void add_unknownBook_shouldReturn404() throws Exception {
        UUID unknown = UUID.randomUUID();
        mockMvc.perform(put(URL + "/{bookId}", unknown).with(bearerFor(readerUsername)))
                .andExpect(status().isNotFound());

        assertThat(count(readerId, unknown)).isZero();
    }

    @Test
    void remove_shouldDeleteRow() throws Exception {
        insertFavorite(readerId, bookId);

        mockMvc.perform(delete(URL + "/{bookId}", bookId).with(bearerFor(readerUsername)))
                .andExpect(status().isNoContent());
        flush();

        assertThat(count(readerId, bookId)).isZero();
    }

    @Test
    void remove_notFavorite_shouldReturn204() throws Exception {
        mockMvc.perform(delete(URL + "/{bookId}", bookId).with(bearerFor(readerUsername)))
                .andExpect(status().isNoContent());
    }

    @Test
    void list_shouldReturnOnlyOwnFavorites() throws Exception {
        insertFavorite(readerId, bookId);
        UUID otherReader = userId(createUser(UserRole.READER));
        insertFavorite(otherReader, insertDigitizedBook("Чужа книга"));

        mockMvc.perform(get(URL).with(bearerFor(readerUsername)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(bookId.toString()))
                .andExpect(jsonPath("$[0].name").value("Улюблена книга"));
    }

    @Test
    void add_asStaff_shouldReturn403() throws Exception {
        mockMvc.perform(put(URL + "/{bookId}", bookId).with(asDigitalArchiveDepartment()))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    private void insertFavorite(UUID userId, UUID bookId) {
        jdbc.sql("INSERT INTO favorites (user_id, book_id) VALUES (:userId, :bookId)")
                .param("userId", userId).param("bookId", bookId).update();
    }

    private int count(UUID userId, UUID bookId) {
        return jdbc.sql("SELECT count(*) FROM favorites WHERE user_id = :userId AND book_id = :bookId")
                .param("userId", userId).param("bookId", bookId)
                .query(Integer.class).single();
    }
}
