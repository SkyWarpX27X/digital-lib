package ua.fictionallibrary.digital_lib.logging;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardAddedEvent;
import ua.fictionallibrary.digital_lib.user.UserAddedEvent;

@Component
public class UserAuditEventListener {

    @ApplicationModuleListener
    public void onUserAdded(UserAddedEvent event) {
        System.out.println("Створено користувача " + event.id() + ": " + event.surname() + " " + event.name() + " " + event.patronymic());
    }
}
