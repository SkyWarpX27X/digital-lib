package ua.fictionallibrary.digital_lib.bookorder;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;

@Component
public class BookOrderDigitizationListener {
    private final BookOrderService bookOrderService;
    private final BookOrderRepository bookOrderRepository;

    public BookOrderDigitizationListener(BookOrderService bookOrderService, BookOrderRepository bookOrderRepository) {
        this.bookOrderService = bookOrderService;
        this.bookOrderRepository = bookOrderRepository;
    }

    @ApplicationModuleListener
    public void onBookDigitized(BookDigitizedEvent event) {
        bookOrderRepository.findByBookIdAndIsOpenTrue(event.physicalId())
                .forEach(order -> bookOrderService.updateStatus(order.getId(), new UpdateStatusCommand(false)));
    }
}
