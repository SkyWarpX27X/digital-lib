package ua.fictionallibrary.digital_lib.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;

import java.time.Year;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;

public class DigitizedBookIT extends AbstractIT {

    private static final String URL = "/api/v1/digitized-books";

    private UUID physicalBookId;

    @BeforeEach
    void setUp() {
        physicalBookId = insertPhysicalBook();
    }

    @Test
    void createDigitizedBook_shouldStoreRow() throws Exception {
        String name = uniqueName();

        mockMvc.perform(post(URL)
                        .with(asDigitalArchiveDepartment())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(name, physicalBookId)))
                .andExpect(status().isCreated());
        flush();
        String stored = jdbc.sql(
                        "SELECT name FROM digitized_books WHERE name = :name")
                .param("name", name)
                .query(String.class)
                .single();

        assertThat(stored).isEqualTo(name);
    }


    @Test
    void create_withBlankName_shouldStoreNothing() throws Exception {
        String name = " ";

        mockMvc.perform(post(URL)
                        .with(asDigitalArchiveDepartment())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(name, physicalBookId)))
                .andExpect(status().isBadRequest());

        assertThat(countByName(name)).isZero();
    }

    @Test
    void create_asReaderShouldStoreNothing() throws Exception {
        String name = uniqueName();

        mockMvc.perform(post(URL)
                        .with(asReader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(name, physicalBookId)))
                .andExpect(status().isForbidden());

        assertThat(countByName(name)).isZero();
    }

    @Test
    void create_withoutPhysicalBookId_shouldReturn400() throws Exception {
        String name = uniqueName();
        mockMvc.perform(post(URL)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(name, null)))
                .andExpect(status().isBadRequest());
        assertThat(countByName(name)).isZero();
    }


    @Test
    void getById_isPublic() throws Exception {
        UUID id = insertDigitizedBook(uniqueName());

        mockMvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getAll_isPublic() throws Exception {
        insertDigitizedBook(uniqueName());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk());
    }

    @Test
    void getById_unknown_shouldReturn404() throws Exception {
        mockMvc.perform(get(URL + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }


    @Test
    void update_shouldChangeRow() throws Exception {
        String originalName = uniqueName();
        UUID id = insertDigitizedBook(originalName);

        String updatedName = uniqueName();

        mockMvc.perform(put(URL + "/{id}", id)
                        .with(asDigitalArchiveDepartment())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(updatedName, null)))
                .andExpect(status().is2xxSuccessful());
        flush();
        String stored = jdbc.sql(
                        "SELECT name FROM digitized_books WHERE id = :id")
                .param("id", id)
                .query(String.class)
                .single();

        assertThat(stored).isEqualTo(updatedName);
    }

    @Test
    void update_withPhysicalBookId_shouldReturn400AndKeepRow()
            throws Exception {
        String originalName = uniqueName();
        UUID id = insertDigitizedBook(originalName);

        mockMvc.perform(put(URL + "/{id}", id)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(uniqueName(), physicalBookId)))
                .andExpect(status().isBadRequest());

        String stored = jdbc.sql(
                        "SELECT name FROM digitized_books WHERE id = :id")
                .param("id", id)
                .query(String.class)
                .single();

        assertThat(stored).isEqualTo(originalName);
    }

    @Test
    void update_unknownId_shouldReturn404() throws Exception {
        mockMvc.perform(put(URL + "/{id}", UUID.randomUUID())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(uniqueName(), null)))
                .andExpect(status().isNotFound());
    }


    @Test
    void delete_shouldRemoveRow() throws Exception {
        UUID id = insertDigitizedBook(uniqueName());

        mockMvc.perform(delete(URL + "/{id}", id).with(asAdmin()))
                .andExpect(status().is2xxSuccessful());
        flush();
        assertThat(countById(id)).isZero();
    }

    @Test
    void delete_unknownId_shouldReturn404() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", UUID.randomUUID()).with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    private static String uniqueName() {
        return "Book " + UUID.randomUUID().toString().substring(0, 9);
    }

    private String bookJson(String name, UUID physicalBookId) throws Exception {
        DigitizedBookRequest request = new DigitizedBookRequest(
                name,
                List.of("Dickinson W. Howship"),
                "The book explores the existence and life of King Arthur",
                "Історична",
                Year.of(1850),
                "eng",
                "Стародрук",
                false,
                "https://digital-lib.com/image",
                "https://digital-lib.com/file",
                physicalBookId
        );
        return objectMapper.writeValueAsString(request);
    }

    private int countById(UUID id) {
        return jdbc.sql("SELECT count(*) FROM digitized_books WHERE id = :id").param("id", id)
                .query(Integer.class).single();
    }

    private int countByName(String name) {
        return jdbc.sql("SELECT count(*) FROM digitized_books WHERE name = :name").param("name", name)
                .query(Integer.class).single();
    }

    private UUID insertDigitizedBook(String name) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO digitized_books
                  (id, name, description, topic, publishing_year, language, resource_type, is_copyrighted, cover_url, file_url)
                VALUES (:id, :name, 'desc', 'Історична', 1850, 'eng', 'Стародрук', false, 'url', 'url')
                """)
                .param("id", id).param("name", name).param("pb", physicalBookId)
                .update();
        return id;
    }

    private UUID insertPhysicalBook() {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO physical_books
                  (id, name, description, topic, publishing_year, language, resource_type)
                VALUES (:id, :name, 'desc', 'Історична', 1850, 'eng', 'Стародрук')
                """)
                .param("id", id)
                .param("name", "Physical " + id)
                .update();
        return id;
    }
}
