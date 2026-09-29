package ua.fictionallibrary.digital_lib.bookorder.implementations;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderCreatedEvent;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderRepository;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderService;
import ua.fictionallibrary.digital_lib.bookorder.UpdateStatusCommand;
import ua.fictionallibrary.digital_lib.bookorder.BookOrder;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.exception.InvalidOrderUpdateException;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderResponse;
import ua.fictionallibrary.digital_lib.exception.LibraryCardNotFoundException;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardService;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;
import ua.fictionallibrary.digital_lib.user.User;
import ua.fictionallibrary.digital_lib.user.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class BookOrderServiceImpl implements BookOrderService {

    private final BookOrderRepository bookOrderRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PhysicalBookService physicalBookService;
    private final LibraryCardService libraryCardService;

    public BookOrderServiceImpl(BookOrderRepository repository, UserRepository userRepository, ApplicationEventPublisher eventPublisher,
                                PhysicalBookService physicalBookService, LibraryCardService libraryCardService) {
        this.bookOrderRepository = repository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.physicalBookService = physicalBookService;
        this.libraryCardService = libraryCardService;
    }

    @Override
    public BookOrderResponse addBookOrder(BookOrderRequest request) {
        UUID creatorId = request.creatorId();
        if (!libraryCardService.exists(creatorId))
            throw new LibraryCardNotFoundException("User with id " + creatorId + " do not have library card and cannot place book orders");
        if (bookOrderRepository.existsByBook(request.book()))
            throw new DuplicateException("Book with id" + request.book() + " already ordered");
        if (!physicalBookService.exists(request.book()))
            throw new DataNotFoundException("Failed to create order, not found book with id " + request.book());
        User user = userRepository.getReferenceById(creatorId);
        BookOrder order = toEntity(request, user);
        if (order.getEmailForDeliver() == null)
            order.setEmailForDeliver(libraryCardService.getEmail(creatorId));
        BookOrder savedOrder = bookOrderRepository.save(order);
        eventPublisher.publishEvent(new BookOrderCreatedEvent(
                savedOrder.getId(),
                user.getId(),
                savedOrder.getEmailForDeliver(),
                savedOrder.getBook()
        ));
        return toResponse(savedOrder);
    }

    @Override
    public List<BookOrderResponse> getBookOrders(boolean onlyOpen) {
        return bookOrderRepository.findBooks(onlyOpen).stream().map(this::toResponse).toList();
    }

    @Override
    public BookOrderResponse getBookOrder(UUID id) {
        return bookOrderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Not found order with id " + id));
    }

    @Override
    public BookOrderResponse updateStatus(UUID id, UpdateStatusCommand command) {
        BookOrder order = bookOrderRepository
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("Failed to update, not found order with id " + id));
        if (command.isOpen() && !order.isOpen())
            throw new InvalidOrderUpdateException("Closed order cannot be opened again");
        order.setOpen(command.isOpen());
        return toResponse(bookOrderRepository.save(order));
    }

    @ApplicationModuleListener
    public void onBookDigitized(BookDigitizedEvent event) {
        bookOrderRepository.findByBook(event.physicalId())
                .ifPresent(order -> updateStatus(order.getId(), new UpdateStatusCommand(false)));
    }

    private BookOrderResponse toResponse(BookOrder order) {
        return new BookOrderResponse(order.getId(), order.getUser().getId(), order.getEmailForDeliver(), order.getBook(), order.isOpen());
    }

    private BookOrder toEntity(BookOrderRequest request, User user){
        return new BookOrder(user, request.emailForDelivery(), request.book(), request.isOpen());
    }
}
