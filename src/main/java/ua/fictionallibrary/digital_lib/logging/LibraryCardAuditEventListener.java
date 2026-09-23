package ua.fictionallibrary.digital_lib.logging;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardAddedEvent;

@Component
public class LibraryCardAuditEventListener {

    @ApplicationModuleListener
    public void onLibraryCardCreated(LibraryCardAddedEvent event) {
        System.out.println("Створено бібліотечну картку для користувача " + event.userId() + " з поштою " + event.email());
    }
}
