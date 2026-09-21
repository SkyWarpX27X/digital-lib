package ua.fictionallibrary.digital_lib.logging;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookAddedEvent;

@Component
public class PhysicalBookAuditEventListener {

    @ApplicationModuleListener
    public void onPhysicalBookAdded(PhysicalBookAddedEvent event) {
        System.out.println("Додано неоцифровану книгу (" + event.resourceType() + ") з id: " + event.id() + ", назвою " + event.bookName());
    }
}
