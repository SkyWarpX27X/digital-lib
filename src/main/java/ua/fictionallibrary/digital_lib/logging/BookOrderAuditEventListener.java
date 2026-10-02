package ua.fictionallibrary.digital_lib.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderCreatedEvent;

@Component
public class BookOrderAuditEventListener {
    private static final Logger log = LoggerFactory.getLogger(BookOrderAuditEventListener.class);

    @ApplicationModuleListener
    public void onOrderCreated(BookOrderCreatedEvent event) {
        log.info("Створено замовлення з id: {} для книги з id: {} email: {} користувачем з id: {}", event.id(), event.book(), event.emailForDelivery(), event.creatorId());
    }
}
