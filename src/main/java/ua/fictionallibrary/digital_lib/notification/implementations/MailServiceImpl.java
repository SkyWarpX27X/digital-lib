package ua.fictionallibrary.digital_lib.notification.implementations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.notification.MailService;

import java.net.URL;
import java.util.UUID;

@Service
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    private final String from;
    private final String baseUrl;

    public MailServiceImpl(JavaMailSender mailSender, @Value("${app.mail.from}") String from,
                           @Value("${app.base-url}") String baseUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendBookDigitized(String to, String bookName, UUID digitizedBookId, URL pdfUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Книгу «" + bookName + "» оцифровано");
        message.setText("""
                Вітаємо!

                Замовлена вами книга «%s» тепер доступна в електронній бібліотеці.

                Завантажити PDF (посилання дійсне 7 днів):
                %s

                Сторінка книги в каталозі:
                %s/digitized-books/%s
                """.formatted(bookName, pdfUrl, baseUrl, digitizedBookId));
        mailSender.send(message);
    }
}
