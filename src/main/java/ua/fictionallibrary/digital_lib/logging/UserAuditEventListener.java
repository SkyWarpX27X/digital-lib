package ua.fictionallibrary.digital_lib.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.user.UserAddedEvent;

@Component
public class UserAuditEventListener {
    private static final Logger log = LoggerFactory.getLogger(UserAuditEventListener.class);

    @ApplicationModuleListener
    public void onUserAdded(UserAddedEvent event) {
        log.info("Створено користувача ({})", event.message());
    }
}
