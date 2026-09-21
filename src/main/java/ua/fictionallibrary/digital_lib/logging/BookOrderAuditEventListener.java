package ua.fictionallibrary.digital_lib.logging;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderCreatedEvent;

@Component
public class BookOrderAuditEventListener {

    @ApplicationModuleListener
    public void onOrderCreated(BookOrderCreatedEvent event) {
        System.out.println("Створено замовлення з id: " + event.id() + " для книги з id: " + event.book()
                + " email: " + event.emailForDelivery() + " користувачем з id: " + event.creatorId());
    }
}
