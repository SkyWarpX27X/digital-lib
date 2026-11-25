package ua.fictionallibrary.digital_lib.favorite;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookDeletedEvent;

@Component
public class FavoriteBookDeletionListener {
    private final FavoriteService service;

    public FavoriteBookDeletionListener(FavoriteService service) {
        this.service = service;
    }

    @ApplicationModuleListener
    public void onBookDeleted(DigitizedBookDeletedEvent event) {
        service.removeBookFromFavorites(event.digitizedId());
    }
}
