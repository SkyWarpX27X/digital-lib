package ua.fictionallibrary.digital_lib.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookRequest;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PhysicalBookCRUDTest extends AbstractIT {

    private static final String URL = "/api/v1/physical-books";

    @Test
    void create_shouldStoreRowAndAuthors() throws Exception {
        String name = uniqueName();
        String author = "Author " + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post(URL)
                        .with(asBookDeliveryDepartment())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(name, List.of(author), Year.of(1950), "Книга")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value(name));
        flush();

        Map<String, Object> row = jdbc.sql("SELECT * FROM physical_books WHERE name = :name")
                .param("name", name)
                .query()
                .singleRow();

        assertThat(row.get("description")).isEqualTo("A book about integration testing");
        assertThat(row.get("topic")).isEqualTo("Наукова");
        assertThat(((Number) row.get("publishing_year")).intValue()).isEqualTo(1950);
        assertThat(row.get("language")).isEqualTo("ukr");
        assertThat(row.get("resource_type")).isEqualTo("Книга");

        List<String> authors = jdbc.sql("""
                        SELECT a.name FROM authors a
                        JOIN physical_book_author pba ON pba.author_id = a.author_id
                        WHERE pba.book_id = :bookId
                        """)
                .param("bookId", row.get("id"))
                .query(String.class)
                .list();

        assertThat(authors).containsExactly(author);
    }

    @Test
    void create_withBlankName_shouldFail() throws Exception {
        String name = " ";

        mockMvc.perform(post(URL)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(name, List.of("Some Author"), Year.of(1950), "Книга")))
                .andExpect(status().isBadRequest());

        assertThat(countByName(name)).isZero();
    }

    @Test
    void create_withoutAuthors_shouldFail() throws Exception {
        String name = uniqueName();

        mockMvc.perform(post(URL)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(name, List.of(), Year.of(1950), "Книга")))
                .andExpect(status().isBadRequest());

        assertThat(countByName(name)).isZero();
    }

    @Test
    void create_earlyPrintedBookAfter1830_shouldFail() throws Exception {
        String name = uniqueName();

        mockMvc.perform(post(URL)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(name, List.of("Some Author"), Year.of(1900), "Стародрук")))
                .andExpect(status().isUnprocessableContent());
        flush();

        assertThat(countByName(name)).isZero();
    }

    @Test
    void create_asReader_shouldReturn403() throws Exception {
        String name = uniqueName();

        mockMvc.perform(post(URL)
                        .with(asReader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(name, List.of("Some Author"), Year.of(1950), "Книга")))
                .andExpect(status().isForbidden());

        assertThat(countByName(name)).isZero();
    }

    @Test
    void create_unauthenticated_shouldReturn401() throws Exception {
        String name = uniqueName();

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(name, List.of("Some Author"), Year.of(1950), "Книга")))
                .andExpect(status().isUnauthorized());

        assertThat(countByName(name)).isZero();
    }

    @Test
    void getById_shouldReturnStoredRow() throws Exception {
        String name = uniqueName();
        UUID id = insertPhysicalBook(name);

        mockMvc.perform(get(URL + "/{id}", id).with(asReader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.resourceType").value("Стародрук"));
    }

    @Test
    void getById_unauthenticated_shouldReturn401() throws Exception {
        UUID id = insertPhysicalBook(uniqueName());

        mockMvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_unknown_shouldReturn404() throws Exception {
        mockMvc.perform(get(URL + "/{id}", UUID.randomUUID()).with(asReader()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldContainStoredRows() throws Exception {
        UUID first = insertPhysicalBook(uniqueName());
        UUID second = insertPhysicalBook(uniqueName());

        mockMvc.perform(get(URL).with(asReader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(first)).exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(second)).exists());
    }

    @Test
    void update_shouldChangeRow() throws Exception {
        UUID id = insertPhysicalBook(uniqueName());
        String updatedName = uniqueName();

        mockMvc.perform(put(URL + "/{id}", id)
                        .with(asBookDeliveryDepartment())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(updatedName, List.of("New Author"), Year.of(1950), "Книга")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedName));
        flush();

        Map<String, Object> row = jdbc.sql("SELECT * FROM physical_books WHERE id = :id")
                .param("id", id)
                .query()
                .singleRow();

        assertThat(row.get("name")).isEqualTo(updatedName);
        assertThat(((Number) row.get("publishing_year")).intValue()).isEqualTo(1950);
        assertThat(row.get("resource_type")).isEqualTo("Книга");
        assertThat(countAuthorLinks(id)).isEqualTo(1);
    }

    @Test
    void update_withInvalidResourceType_shouldReturn422AndKeepRow() throws Exception {
        String originalName = uniqueName();
        UUID id = insertPhysicalBook(originalName);

        mockMvc.perform(put(URL + "/{id}", id)
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(uniqueName(), List.of("Some Author"), Year.of(1950), "Стародрук")))
                .andExpect(status().isUnprocessableContent());

        assertThat(nameById(id)).isEqualTo(originalName);
    }

    @Test
    void update_asReader_shouldReturn403AndKeepRow() throws Exception {
        String originalName = uniqueName();
        UUID id = insertPhysicalBook(originalName);

        mockMvc.perform(put(URL + "/{id}", id)
                        .with(asReader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(uniqueName(), List.of("Some Author"), Year.of(1950), "Книга")))
                .andExpect(status().isForbidden());

        assertThat(nameById(id)).isEqualTo(originalName);
    }

    @Test
    void update_unknownId_shouldReturn404() throws Exception {
        mockMvc.perform(put(URL + "/{id}", UUID.randomUUID())
                        .with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(uniqueName(), List.of("Some Author"), Year.of(1950), "Книга")))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldRemoveRow() throws Exception {
        UUID id = insertPhysicalBook(uniqueName());

        mockMvc.perform(delete(URL + "/{id}", id).with(asAdmin()))
                .andExpect(status().isNoContent());
        flush();

        assertThat(countById(id)).isZero();
    }

    @Test
    void delete_asBookDeliveryDepartment_shouldReturn403AndKeepRow() throws Exception {
        UUID id = insertPhysicalBook(uniqueName());

        mockMvc.perform(delete(URL + "/{id}", id).with(asBookDeliveryDepartment()))
                .andExpect(status().isForbidden());

        assertThat(countById(id)).isOne();
    }

    @Test
    void delete_unknownId_shouldReturn404() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", UUID.randomUUID()).with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    private static String uniqueName() {
        return "Book " + UUID.randomUUID().toString().substring(0, 9);
    }

    private String bookJson(String name, List<String> authors, Year year, String resourceType) throws Exception {
        PhysicalBookRequest request = new PhysicalBookRequest(
                name,
                authors,
                "A book about integration testing",
                "Наукова",
                year,
                "ukr",
                resourceType
        );
        return objectMapper.writeValueAsString(request);
    }

    private String nameById(UUID id) {
        return jdbc.sql("SELECT name FROM physical_books WHERE id = :id").param("id", id)
                .query(String.class).single();
    }

    private int countById(UUID id) {
        return jdbc.sql("SELECT count(*) FROM physical_books WHERE id = :id").param("id", id)
                .query(Integer.class).single();
    }

    private int countByName(String name) {
        return jdbc.sql("SELECT count(*) FROM physical_books WHERE name = :name").param("name", name)
                .query(Integer.class).single();
    }

    private int countAuthorLinks(UUID bookId) {
        return jdbc.sql("SELECT count(*) FROM physical_book_author WHERE book_id = :bookId").param("bookId", bookId)
                .query(Integer.class).single();
    }

    private UUID insertPhysicalBook(String name) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO physical_books
                  (id, name, description, topic, publishing_year, language, resource_type)
                VALUES (:id, :name, 'desc', 'Історична', 1800, 'eng', 'Стародрук')
                """)
                .param("id", id)
                .param("name", name)
                .update();
        return id;
    }
}
