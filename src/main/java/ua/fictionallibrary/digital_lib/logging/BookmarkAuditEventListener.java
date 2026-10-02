package ua.fictionallibrary.digital_lib.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.bookmark.BookmarkAddedEvent;

@Component
public class BookmarkAuditEventListener {
    private static final Logger log = LoggerFactory.getLogger(BookmarkAuditEventListener.class);

    @ApplicationModuleListener
    public void onBookDigitized(BookmarkAddedEvent event) {
        log.info("Додано закладку для книги з id {} користувачем з id {} на сторінці{}", event.bookId(), event.userId(), event.pageNumber());
    }
}
