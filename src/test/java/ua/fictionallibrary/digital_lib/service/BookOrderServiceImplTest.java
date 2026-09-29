package ua.fictionallibrary.digital_lib.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderCreatedEvent;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderRepository;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderService;
import ua.fictionallibrary.digital_lib.bookorder.UpdateStatusCommand;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderRequest;
import ua.fictionallibrary.digital_lib.bookorder.implementations.BookOrderServiceImpl;
import ua.fictionallibrary.digital_lib.bookorder.BookOrder;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderResponse;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.exception.InvalidOrderUpdateException;
import ua.fictionallibrary.digital_lib.exception.LibraryCardNotFoundException;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardService;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBook;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookRepository;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;
import ua.fictionallibrary.digital_lib.user.User;
import ua.fictionallibrary.digital_lib.user.UserRepository;
import ua.fictionallibrary.digital_lib.user.UserRole;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookOrderServiceImplTest {
    @Mock
    BookOrderRepository repository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    PhysicalBookService physicalBookService;
    @Mock
    LibraryCardService libraryCardService;
    @Mock
    UserRepository userRepository;
    @Mock
    PhysicalBookRepository bookRepository;
    User user;
    PhysicalBook book;
    BookOrderService service;

    @BeforeEach
    public void setUp() {
        service = new BookOrderServiceImpl(repository, userRepository, bookRepository, eventPublisher, physicalBookService, libraryCardService);
        user = new User("john", "123", "John", "Doe", null, UserRole.READER, true);
        book = new PhysicalBook("Book", List.of("John"), "Desc", "topic", Year.of(2020), "eng", "resourceType");
    }

    @Test
    void successfullyCreateOrder() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BookOrderRequest request = new BookOrderRequest(userId, "123@gmail.com", bookId, true);
        when(repository.existsByBookId(bookId)).thenReturn(false);
        when(libraryCardService.exists(userId)).thenReturn(true);
        when(physicalBookService.exists(bookId)).thenReturn(true);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(bookRepository.getReferenceById(bookId)).thenReturn(book);
        when(repository.save(any(BookOrder.class))).thenAnswer(i -> i.getArgument(0));

        BookOrderResponse response = service.addBookOrder(request);
        assertNotNull(response);
        ArgumentCaptor<BookOrder> orderCaptor = ArgumentCaptor.forClass(BookOrder.class);
        verify(repository).save(orderCaptor.capture());
        BookOrder saved = orderCaptor.getValue();
        assertEquals(book, saved.getBook());
        assertSame(user, saved.getUser());
        assertSame(book, saved.getBook());
        assertEquals("123@gmail.com", saved.getEmailForDeliver());
        assertTrue(saved.isOpen());

        ArgumentCaptor<BookOrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BookOrderCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
    }

    @Test
    void tryToOrderAlreadyOrderedBook() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BookOrderRequest request = new BookOrderRequest(userId, "123@gmail.com", bookId, true);
        when(libraryCardService.exists(userId)).thenReturn(true);
        when(repository.existsByBookId(bookId)).thenReturn(true);

        assertThrows(DuplicateException.class, () -> service.addBookOrder(request));
        verify(repository, never()).save(any(BookOrder.class));
    }

    @Test
    void tryToOrderWithoutLibraryCard() {
        UUID userId = UUID.randomUUID();
        BookOrderRequest request = new BookOrderRequest(userId, "123@gmail.com", UUID.randomUUID(), true);
        when(libraryCardService.exists(userId)).thenReturn(false);

        assertThrows(LibraryCardNotFoundException.class, () -> service.addBookOrder(request));
    }

    @Test
    void successfullyCloseOrder() {
        UUID orderId = UUID.randomUUID();
        when(repository.findById(orderId)).thenReturn(Optional.of(new BookOrder(user, "123@gmail.com", book, true)));
        when(repository.save(any(BookOrder.class))).thenAnswer(i -> i.getArgument(0));

        BookOrderResponse response = service.updateStatus(orderId, new UpdateStatusCommand(false));
        assertNotNull(response);
        assertFalse(response.isOpen());
    }

    @Test
    void tryToOpenClosedOrder() {
        UUID orderId = UUID.randomUUID();
        when(repository.findById(orderId)).thenReturn(Optional.of(new BookOrder(user, "123@gmail.com", book, false)));

        assertThrows(InvalidOrderUpdateException.class, () -> service.updateStatus(orderId, new UpdateStatusCommand(true)));
    }
}
