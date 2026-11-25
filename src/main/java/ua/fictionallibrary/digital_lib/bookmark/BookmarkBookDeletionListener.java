package ua.fictionallibrary.digital_lib.bookmark;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookDeletedEvent;

@Component
public class BookmarkBookDeletionListener {
    private final BookmarkService service;

    public BookmarkBookDeletionListener(BookmarkService service) {
        this.service = service;
    }

    @ApplicationModuleListener
    public void onBookDeleted(DigitizedBookDeletedEvent event) {
        service.deleteBookmarksForBook(event.digitizedId());
    }
}
