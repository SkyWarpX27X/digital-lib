package ua.fictionallibrary.digital_lib.physicalbook;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;

@Component
public class PhysicalBookDigitizationListener {
    private final PhysicalBookService physicalBookService;

    public PhysicalBookDigitizationListener(PhysicalBookService physicalBookService) {
        this.physicalBookService = physicalBookService;
    }

    @ApplicationModuleListener
    public void onBookDigitized(BookDigitizedEvent event) {
        physicalBookService.deletePhysicalBook(event.physicalId());
    }
}
