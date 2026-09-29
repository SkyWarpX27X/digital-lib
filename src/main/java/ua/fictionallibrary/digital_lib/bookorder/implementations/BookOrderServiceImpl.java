package ua.fictionallibrary.digital_lib.bookorder.implementations;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderCreatedEvent;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderRepository;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderService;
import ua.fictionallibrary.digital_lib.bookorder.UpdateStatusCommand;
import ua.fictionallibrary.digital_lib.bookorder.BookOrder;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.exception.InvalidOrderUpdateException;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderResponse;
import ua.fictionallibrary.digital_lib.exception.LibraryCardNotFoundException;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardService;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;

import java.util.List;
import java.util.UUID;

@Service
public class BookOrderServiceImpl implements BookOrderService {

    private final BookOrderRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final PhysicalBookService physicalBookService;
    private final LibraryCardService libraryCardService;

    public BookOrderServiceImpl(BookOrderRepository repository, ApplicationEventPublisher eventPublisher,
                                PhysicalBookService physicalBookService, LibraryCardService libraryCardService) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.physicalBookService = physicalBookService;
        this.libraryCardService = libraryCardService;
    }

    @Override
    public BookOrderResponse addBookOrder(BookOrder order) {
        if (!libraryCardService.exists(order.getCreatorId()))
            throw new LibraryCardNotFoundException("User with id " + order.getCreatorId() + " do not have library card and cannot place book orders");
        if (order.getEmailForDeliver() == null)
            order.setEmailForDeliver(libraryCardService.getEmail(order.getCreatorId()));
        if (repository.existsByBook(order.getBook()))
            throw new DuplicateException("Book with id" + order.getBook() + " already ordered");
        if (!physicalBookService.exists(order.getBook()))
            throw new DataNotFoundException("Failed to create order, not found book with id " + order.getBook());
        BookOrder savedOrder = repository.save(order);
        eventPublisher.publishEvent(new BookOrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getCreatorId(),
                savedOrder.getEmailForDeliver(),
                savedOrder.getBook()
        ));
        return toResponse(savedOrder);
    }

    @Override
    public List<BookOrderResponse> getBookOrders(boolean onlyOpen) {
        return repository.findBooks(onlyOpen).stream().map(this::toResponse).toList();
    }

    @Override
    public BookOrderResponse getBookOrder(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Not found order with id " + id));
    }

    @Override
    public BookOrderResponse updateStatus(UUID id, UpdateStatusCommand command) {
        BookOrder order = repository
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("Failed to update, not found order with id " + id));
        if (command.isOpen() && !order.isOpen())
            throw new InvalidOrderUpdateException("Closed order cannot be opened again");
        order.setOpen(command.isOpen());
        return toResponse(repository.save(order));
    }

    @ApplicationModuleListener
    public void onBookDigitized(BookDigitizedEvent event) {
        repository.findByBook(event.physicalId())
                .ifPresent(order -> updateStatus(order.getId(), new UpdateStatusCommand(false)));
    }

    private BookOrderResponse toResponse(BookOrder order) {
        return new BookOrderResponse(order.getId(), order.getCreatorId(), order.getEmailForDeliver(), order.getBook(), order.isOpen());
    }
}
