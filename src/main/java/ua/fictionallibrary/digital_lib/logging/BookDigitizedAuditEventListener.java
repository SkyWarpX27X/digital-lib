package ua.fictionallibrary.digital_lib.logging;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;

@Component
public class BookDigitizedAuditEventListener {
    @ApplicationModuleListener
    public void onBookDigitized(BookDigitizedEvent event) {
        System.out.println("Оцифровано фізичну книгу з id: " + event.physicalId() + ". ID оцифрованої книги: "
                + event.digitizedId());
    }
}
