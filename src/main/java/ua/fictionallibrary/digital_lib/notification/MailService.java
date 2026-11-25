package ua.fictionallibrary.digital_lib.notification;

import java.net.URL;
import java.util.UUID;

public interface MailService {
    void sendBookDigitized(String to, String bookName, UUID digitizedBookId, URL pdfUrl);
}
