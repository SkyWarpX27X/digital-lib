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
import ua.fictionallibrary.digital_lib.bookorder.implementations.BookOrderServiceImpl;
import ua.fictionallibrary.digital_lib.bookorder.model.BookOrderEntity;
import ua.fictionallibrary.digital_lib.bookorder.model.dto.BookOrderResponse;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.exception.InvalidOrderUpdateException;
import ua.fictionallibrary.digital_lib.exception.LibraryCardNotFoundException;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardService;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;

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
    BookOrderService service;

    @BeforeEach
    public void setUp() {
        service = new BookOrderServiceImpl(repository, eventPublisher, physicalBookService, libraryCardService);
    }

    @Test
    void successfullyCreateOrder() {
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BookOrderEntity order = new BookOrderEntity(orderId, userId, "123@gmail.com", bookId, true);
        when(repository.existsById(orderId)).thenReturn(false);
        when(repository.existsByBook(bookId)).thenReturn(false);
        when(libraryCardService.exists(userId)).thenReturn(true);
        when(physicalBookService.exists(bookId)).thenReturn(true);
        when(repository.saveBookOrder(any(BookOrderEntity.class))).thenAnswer(i -> i.getArgument(0));

        BookOrderResponse response = service.addBookOrder(order);
        assertNotNull(response);
        assertEquals(orderId, order.id());
        assertEquals(bookId, order.book());
        assertEquals(userId, order.creatorId());
        assertEquals("123@gmail.com", order.emailForDelivery());
        assertTrue(order.isOpen());

        verify(repository).saveBookOrder(any(BookOrderEntity.class));
        ArgumentCaptor<BookOrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BookOrderCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(orderId, eventCaptor.getValue().id());
    }

    @Test
    void tryToAddDuplicateIdOrder() {
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BookOrderEntity order = new BookOrderEntity(orderId, userId, "123@gmail.com", bookId, true);
        when(libraryCardService.exists(userId)).thenReturn(true);
        when(repository.existsById(orderId)).thenReturn(true);

        assertThrows(DuplicateException.class, () -> service.addBookOrder(order));
        verify(repository, never()).saveBookOrder(any(BookOrderEntity.class));
    }

    @Test
    void tryToOrderAlreadyOrderedBook() {
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BookOrderEntity order = new BookOrderEntity(orderId, userId, "123@gmail.com", bookId, true);
        when(libraryCardService.exists(userId)).thenReturn(true);
        when(repository.existsById(orderId)).thenReturn(false);
        when(repository.existsByBook(bookId)).thenReturn(true);

        assertThrows(DuplicateException.class, () -> service.addBookOrder(order));
        verify(repository, never()).saveBookOrder(any(BookOrderEntity.class));
    }

    @Test
    void tryToOrderWithoutLibraryCard() {
        UUID userId = UUID.randomUUID();
        BookOrderEntity order = new BookOrderEntity(UUID.randomUUID(), userId, "123@gmail.com", UUID.randomUUID(), true);
        when(libraryCardService.exists(userId)).thenReturn(false);

        assertThrows(LibraryCardNotFoundException.class, () -> service.addBookOrder(order));
    }

    @Test
    void successfullyCloseOrder() {
        UUID orderId = UUID.randomUUID();
        when(repository.getBookOrder(orderId)).thenReturn(Optional.of(new BookOrderEntity(orderId,
                UUID.randomUUID(), "123@gmail.com", UUID.randomUUID(), true)));

        BookOrderResponse response = service.updateStatus(orderId, new UpdateStatusCommand(false));
        assertNotNull(response);
        assertEquals(orderId, response.id());
        assertFalse(response.isOpen());
    }

    @Test
    void tryToOpenClosedOrder() {
        UUID orderId = UUID.randomUUID();
        when(repository.getBookOrder(orderId)).thenReturn(Optional.of(new BookOrderEntity(orderId,
                UUID.randomUUID(), "123@gmail.com", UUID.randomUUID(), false)));

        assertThrows(InvalidOrderUpdateException.class, () -> service.updateStatus(orderId, new UpdateStatusCommand(true)));
    }
}
