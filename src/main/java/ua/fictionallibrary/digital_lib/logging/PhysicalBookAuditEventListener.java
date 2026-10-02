package ua.fictionallibrary.digital_lib.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookAddedEvent;

@Component
public class PhysicalBookAuditEventListener {
    private static final Logger log = LoggerFactory.getLogger(PhysicalBookAuditEventListener.class);

    @ApplicationModuleListener
    public void onPhysicalBookAdded(PhysicalBookAddedEvent event) {
        log.info("Додано неоцифровану книгу ({}) з id: {}, назвою {}", event.resourceType(), event.id(), event.bookName());
    }
}
