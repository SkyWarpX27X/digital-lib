package ua.fictionallibrary.digital_lib.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.user.LibraryCardAddedEvent;

@Component
public class LibraryCardAuditEventListener {
    private static final Logger log = LoggerFactory.getLogger(LibraryCardAuditEventListener.class);

    @ApplicationModuleListener
    public void onLibraryCardCreated(LibraryCardAddedEvent event) {
        log.info("Створено бібліотечну картку ({})", event.message());
    }
}
