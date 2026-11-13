package ua.fictionallibrary.digital_lib.bookorder.implementations;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderCreatedEvent;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderRepository;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderService;
import ua.fictionallibrary.digital_lib.bookorder.UpdateStatusCommand;
import ua.fictionallibrary.digital_lib.bookorder.BookOrder;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.starter.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.starter.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.exception.InvalidOrderUpdateException;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderResponse;
import ua.fictionallibrary.digital_lib.exception.LibraryCardNotFoundException;
import ua.fictionallibrary.digital_lib.user.LibraryCardService;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBook;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookRepository;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;
import ua.fictionallibrary.digital_lib.user.User;
import ua.fictionallibrary.digital_lib.user.UserRepository;
import ua.fictionallibrary.digital_lib.author.Author;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookOrderServiceImpl implements BookOrderService {

    private final BookOrderRepository bookOrderRepository;
    private final UserRepository userRepository;
    private final PhysicalBookRepository physicalBookRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LibraryCardService libraryCardService;

    public BookOrderServiceImpl(BookOrderRepository repository, UserRepository userRepository, PhysicalBookRepository physicalBookRepository, ApplicationEventPublisher eventPublisher,
                                PhysicalBookService physicalBookService, LibraryCardService libraryCardService) {
        this.bookOrderRepository = repository;
        this.userRepository = userRepository;
        this.physicalBookRepository = physicalBookRepository;
        this.eventPublisher = eventPublisher;
        this.libraryCardService = libraryCardService;
    }

    @Override
    public BookOrderResponse addBookOrder(BookOrderRequest request, UUID creatorId) {
        if (!libraryCardService.exists(creatorId))
            throw new LibraryCardNotFoundException("User with id " + creatorId + " do not have library card and cannot place book orders");
        if (bookOrderRepository.existsByUserIdAndBookIdAndIsOpenTrue(creatorId, request.book()))
            throw new DuplicateException("User already has an open order for book with id " + request.book());
        PhysicalBook book = physicalBookRepository.findById(request.book())
                .orElseThrow(() -> new DataNotFoundException(
                        "Failed to create order, not found book with id " + request.book()));
        User user = userRepository.getReferenceById(creatorId);
        String email = request.emailForDelivery() == null || request.emailForDelivery().isBlank() ? null : request.emailForDelivery();
        BookOrder order = new BookOrder(user, email, request.book(), request.isOpen());
        order.setBook(book.getName(), book.getAuthors().stream().map(Author::getName).sorted().collect(Collectors.joining(", ")));
        if (order.getEmailForDeliver() == null)
            order.setEmailForDeliver(libraryCardService.getEmail(creatorId));
        BookOrder savedOrder = bookOrderRepository.save(order);
        eventPublisher.publishEvent(new BookOrderCreatedEvent(
                savedOrder.getId(),
                user.getId(),
                savedOrder.getEmailForDeliver(),
                savedOrder.getBookId()
        ));
        return toResponse(savedOrder);
    }

    @Override
    public List<BookOrderResponse> getBookOrders(boolean onlyOpen) {
        return bookOrderRepository.findBooks(onlyOpen).stream().map(this::toResponse).toList();
    }

    @Override
    public List<BookOrderResponse> getUserBookOrders(UUID userId) {
        return bookOrderRepository.findByUserId(userId).stream().map(this::toResponse).toList();
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
        bookOrderRepository.findByBookIdAndIsOpenTrue(event.physicalId())
                .forEach(order -> updateStatus(order.getId(), new UpdateStatusCommand(false)));
    }

    private BookOrderResponse toResponse(BookOrder order) {
        User user = order.getUser();
        return new BookOrderResponse(order.getId(), user.getId(), user.getName() + " " + user.getSurname(),
                order.getEmailForDeliver(), order.getBookId(), order.getBookName(), order.getBookAuthors(), order.isOpen());
    }
}
