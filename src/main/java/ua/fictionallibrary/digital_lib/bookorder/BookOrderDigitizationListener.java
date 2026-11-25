package ua.fictionallibrary.digital_lib.bookorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookService;
import ua.fictionallibrary.digital_lib.notification.MailService;

import java.net.URL;
import java.time.Duration;
import java.util.List;

@Component
public class BookOrderDigitizationListener {
    private static final Duration PDF_LINK_TTL = Duration.ofDays(7);
    private static final Logger log = LoggerFactory.getLogger(BookOrderDigitizationListener.class);

    private final BookOrderService bookOrderService;
    private final BookOrderRepository bookOrderRepository;
    private final DigitizedBookService digitizedBookService;
    private final MailService mailService;

    public BookOrderDigitizationListener(BookOrderService bookOrderService, BookOrderRepository bookOrderRepository,
                                         DigitizedBookService digitizedBookService, MailService mailService) {
        this.bookOrderService = bookOrderService;
        this.bookOrderRepository = bookOrderRepository;
        this.digitizedBookService = digitizedBookService;
        this.mailService = mailService;
    }

    @ApplicationModuleListener
    public void onBookDigitized(BookDigitizedEvent event) {
        List<BookOrder> orders = bookOrderRepository.findByBookIdAndIsOpenTrue(event.physicalId());
        if (orders.isEmpty())
            return;
        URL pdfUrl = digitizedBookService.getPdfDownloadUrl(event.digitizedId(), PDF_LINK_TTL);
        for (BookOrder order : orders) {
            try {
                mailService.sendBookDigitized(order.getEmailForDeliver(), event.bookName(), event.digitizedId(), pdfUrl);
            }
            catch (Exception e) {
                log.warn("Could not send digitized book email for order {}", order.getId(), e);
            }
            bookOrderService.updateStatus(order.getId(), new UpdateStatusCommand(false));
        }
    }
}
