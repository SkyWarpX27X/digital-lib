package ua.fictionallibrary.digital_lib.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.user.UserRole;

import org.springframework.http.HttpHeaders;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        createBook(name, physicalBookId, asDigitalArchiveDepartment()).andExpect(status().isCreated());
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

        createBook(name, physicalBookId, asDigitalArchiveDepartment()).andExpect(status().isBadRequest());

        assertThat(countByName(name)).isZero();
    }

    @Test
    void create_asReaderShouldStoreNothing() throws Exception {
        String name = uniqueName();

        createBook(name, physicalBookId, asReader()).andExpect(status().isForbidden());

        assertThat(countByName(name)).isZero();
    }

    @Test
    void create_withoutPhysicalBookId_shouldReturn400() throws Exception {
        String name = uniqueName();
        createBook(name, null, asAdmin()).andExpect(status().isBadRequest());
        assertThat(countByName(name)).isZero();
    }


    @Test
    void getById_isPublic() throws Exception {
        UUID id = insertDigitizedBook(uniqueName());

        mockMvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    enum Viewer { ANONYMOUS, READER_WITHOUT_CARD, READER_WITH_CARD, DIGITAL_ARCHIVE, BOOK_DELIVERY, ADMIN }

    @ParameterizedTest(name = "{0}, copyrighted={1} -> canRead={2}, pdf {3}")
    @CsvSource({
            "ANONYMOUS,           true,  false, 401",
            "READER_WITHOUT_CARD, true,  false, 403",
            "READER_WITH_CARD,    true,  true,  200",
            "DIGITAL_ARCHIVE,     true,  true,  200",
            "BOOK_DELIVERY,       true,  true,  200",
            "ADMIN,               true,  true,  200",
            "ANONYMOUS,           false, true,  200",
            "READER_WITHOUT_CARD, false, true,  200",
            "READER_WITH_CARD,    false, true,  200",
            "DIGITAL_ARCHIVE,     false, true,  200",
    })
    void copyrightAccess(Viewer viewer, boolean copyrighted, boolean canRead, int pdfStatus) throws Exception {
        UUID id = insertDigitizedBook(uniqueName(), copyrighted);
        RequestPostProcessor auth = as(viewer);
        when(fileStorageService.load(any())).thenAnswer(i -> new ByteArrayInputStream(TestFiles.samplePdf()));

        mockMvc.perform(get(URL + "/{id}", id).with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.canRead").value(canRead))
                .andExpect(canRead ? jsonPath("$.pdfUrl").value(URL + "/" + id + "/pdf") : jsonPath("$.pdfUrl").doesNotExist());

        mockMvc.perform(get(URL + "/{id}/pdf", id).with(auth))
                .andExpect(status().is(pdfStatus));
        if (pdfStatus != 200)
            verify(fileStorageService, never()).load(any());
    }

    @Test
    void getAll_anonymous_shouldHidePdfOfCopyrightedBooksOnly() throws Exception {
        UUID copyrighted = insertDigitizedBook(uniqueName(), true);
        UUID free = insertDigitizedBook(uniqueName(), false);

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')].pdfUrl", copyrighted).isEmpty())
                .andExpect(jsonPath("$[?(@.id == '%s')].canRead", copyrighted).value(false))
                .andExpect(jsonPath("$[?(@.id == '%s')].pdfUrl", free).value(URL + "/" + free + "/pdf"));
    }

    @Test
    void getAll_readerWithCard_shouldSeePdfOfCopyrightedBooks() throws Exception {
        UUID copyrighted = insertDigitizedBook(uniqueName(), true);

        mockMvc.perform(get(URL).with(as(Viewer.READER_WITH_CARD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')].pdfUrl", copyrighted).value(URL + "/" + copyrighted + "/pdf"));
    }

    private RequestPostProcessor as(Viewer viewer) {
        return switch (viewer) {
            case ANONYMOUS -> request -> request;
            case READER_WITHOUT_CARD -> asReader();
            case READER_WITH_CARD -> readerWithCard();
            case DIGITAL_ARCHIVE -> asDigitalArchiveDepartment();
            case BOOK_DELIVERY -> asBookDeliveryDepartment();
            case ADMIN -> asAdmin();
        };
    }

    private RequestPostProcessor readerWithCard() {
        String username = createUser(UserRole.READER);
        UUID userId = jdbc.sql("SELECT id FROM users WHERE username = :username").param("username", username)
                .query(UUID.class).single();
        jdbc.sql("""
                INSERT INTO library_cards (owner_id, email, post_code, birth_year, living_address)
                VALUES (:ownerId, 'reader@digital-lib.com', '01001', 1990, 'Kyiv, Khreshchatyk 1')
                """)
                .param("ownerId", userId)
                .update();
        return bearerFor(username);
    }

    @Test
    void getPdf_shouldStreamPdfInline() throws Exception {
        UUID id = insertDigitizedBook(uniqueName());
        byte[] pdf = TestFiles.samplePdf();
        when(fileStorageService.load("digitized-books/" + id + "/book.pdf")).thenReturn(new ByteArrayInputStream(pdf));

        mockMvc.perform(get(URL + "/{id}/pdf", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline"))
                .andExpect(content().bytes(pdf));
    }

    @Test
    void getPdf_unknown_shouldReturn404() throws Exception {
        mockMvc.perform(get(URL + "/{id}/pdf", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCover_shouldReturnImageWithStoredType() throws Exception {
        UUID id = insertDigitizedBook(uniqueName());
        String coverKey = "digitized-books/" + id + "/cover.png";
        jdbc.sql("UPDATE digitized_books SET cover_key = :key, cover_content_type = 'image/png' WHERE id = :id")
                .param("key", coverKey).param("id", id).update();
        byte[] cover = TestFiles.coverPng();
        when(fileStorageService.load(coverKey)).thenReturn(new ByteArrayInputStream(cover));

        mockMvc.perform(get(URL + "/{id}/cover", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(cover));
    }

    @Test
    void getCover_bookWithoutCover_shouldReturn404() throws Exception {
        UUID id = insertDigitizedBook(uniqueName());

        mockMvc.perform(get(URL + "/{id}/cover", id))
                .andExpect(status().isNotFound());
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
        return insertDigitizedBook(name, false);
    }

    private UUID insertDigitizedBook(String name, boolean copyrighted) {
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

    private ResultActions createBook(String name, UUID physicalId, RequestPostProcessor auth) throws Exception {
        return mockMvc.perform(multipart(URL)
                .file(new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE,
                        bookJson(name, physicalId).getBytes(StandardCharsets.UTF_8)))
                .file(new MockMultipartFile("pdf", "book.pdf", "application/pdf", TestFiles.samplePdf()))
                .file(new MockMultipartFile("cover", "cover.png", "image/png", TestFiles.coverPng()))
                .with(auth));
    }
}
