package ua.fictionallibrary.digital_lib.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.user.UserRole;

import java.net.URI;
import java.time.Duration;
import java.time.Year;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookDeliveryMailIT extends AbstractFlowIT {

    private static final String PDF_URL = "http://storage.test/digitized-books/x/book.pdf?signed";

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void digitizing_emailsEveryOpenOrder_andClosesThem() throws Exception {
        UUID physicalId = createPhysicalBook();
        UUID open1 = insertOrder(physicalId, "first@example.com", true);
        UUID open2 = insertOrder(physicalId, "second@example.com", true);
        insertOrder(physicalId, "closed@example.com", false);
        UUID otherBookOrder = insertOrder(createPhysicalBook(), "other@example.com", true);

        UUID digitizedId = digitize(physicalId);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(mailSender, times(2)).send(captor.capture());
            assertThat(isOpen(open1)).isFalse();
            assertThat(isOpen(open2)).isFalse();
        });
        assertThat(captor.getAllValues()).flatMap(m -> List.of(m.getTo()))
                .containsExactlyInAnyOrder("first@example.com", "second@example.com");
        assertThat(captor.getValue().getText())
                .contains(PDF_URL)
                .contains("/digitized-books/" + digitizedId);
        assertThat(isOpen(otherBookOrder)).isTrue();
    }

    @Test
    void failedEmail_doesNotBlockOthers_andOrdersAreClosed() throws Exception {
        UUID physicalId = createPhysicalBook();
        UUID open1 = insertOrder(physicalId, "first@example.com", true);
        UUID open2 = insertOrder(physicalId, "second@example.com", true);
        doThrow(new MailSendException("SMTP down")).doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        digitize(physicalId);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
            assertThat(isOpen(open1)).isFalse();
            assertThat(isOpen(open2)).isFalse();
        });
    }

    private UUID createPhysicalBook() throws Exception {
        String name = "Книга " + UUID.randomUUID().toString().substring(0, 8);
        PhysicalBookRequest book = new PhysicalBookRequest(name, List.of("Автор"), "Опис", "Тема",
                Year.of(1944), "Українська", "Книга");
        mockMvc.perform(post("/api/v1/physical-books").with(asBookDeliveryDepartment())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isCreated());
        return jdbc.sql("SELECT id FROM physical_books WHERE name = :n").param("n", name).query(UUID.class).single();
    }

    private UUID insertOrder(UUID physicalId, String email, boolean open) {
        String username = createUser(UserRole.READER);
        UUID userId = jdbc.sql("SELECT id FROM users WHERE username = :u").param("u", username).query(UUID.class).single();
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO book_orders (id, user_id, email_for_deliver, book_id, is_open, book_name, book_authors)
                VALUES (:id, :userId, :email, :bookId, :open, 'Книга', 'Автор')
                """)
                .param("id", id).param("userId", userId).param("email", email)
                .param("bookId", physicalId).param("open", open)
                .update();
        return id;
    }

    private UUID digitize(UUID physicalId) throws Exception {
        given(fileStorageService.presignedUrl(anyString(), any())).willReturn(URI.create(PDF_URL).toURL());
        DigitizedBookRequest meta = new DigitizedBookRequest("Тигролови", List.of("Іван Багряний"), "Опис", "Тема",
                Year.of(1944), "Українська", "Книга", false, physicalId);
        String body = mockMvc.perform(multipart("/api/v1/digitized-books").with(asDigitalArchiveDepartment())
                        .file(new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsBytes(meta)))
                        .file(new MockMultipartFile("pdf", "book.pdf", "application/pdf", TestFiles.samplePdf())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    private boolean isOpen(UUID orderId) {
        return jdbc.sql("SELECT is_open FROM book_orders WHERE id = :id").param("id", orderId).query(Boolean.class).single();
    }
}
