package ua.fictionallibrary.digital_lib.logging;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.bookmark.BookmarkAddedEvent;

@Component
public class BookmarkAuditEventListener {
    @ApplicationModuleListener
    public void onBookDigitized(BookmarkAddedEvent event) {
        System.out.println("Додано закладку для книги з id " + event.bookId() + " користувачем з id " + event.userId() +
                " на сторінці" + event.pageNumber());
    }
}
