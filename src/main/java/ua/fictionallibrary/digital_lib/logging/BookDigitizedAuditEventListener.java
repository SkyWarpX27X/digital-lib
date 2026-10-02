package ua.fictionallibrary.digital_lib.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;

@Component
public class BookDigitizedAuditEventListener {
    private static final Logger log = LoggerFactory.getLogger(BookDigitizedAuditEventListener.class);

    @ApplicationModuleListener
    public void onBookDigitized(BookDigitizedEvent event) {
        log.info("Оцифровано фізичну книгу з id: {}. ID оцифрованої книги: {}", event.physicalId(), event.digitizedId());
    }
}
