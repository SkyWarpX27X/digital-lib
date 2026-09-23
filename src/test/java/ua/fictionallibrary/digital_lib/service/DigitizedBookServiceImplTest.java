package ua.fictionallibrary.digital_lib.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookRepository;
import ua.fictionallibrary.digital_lib.digitizedbook.implementations.DigitizedBookServiceImpl;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.digitizedbook.model.DigitizedBookEntity;
import ua.fictionallibrary.digital_lib.digitizedbook.model.dto.DigitizedBookResponse;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DigitizedBookServiceImplTest {
    @Mock
    DigitizedBookRepository repository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    DigitizedBookServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DigitizedBookServiceImpl(repository, eventPublisher);
    }

    private DigitizedBookEntity entity() {
        return new DigitizedBookEntity(UUID.randomUUID(), "King Arthur in Cornwall", List.of("Dickinson W. Howship"),
                "The book explores the existence and life of King Arthur", "Історична",
                Year.of(1850), "eng", "Стародрук", false, "url", "url");
    }

    @Test
    void addDigitizedBook_successfullyAddsAndPublishesEvent() {
        UUID physicalBookId = UUID.randomUUID();
        DigitizedBookEntity book = entity();
        when(repository.exists(book.id())).thenReturn(false);
        when(repository.saveDigitizedBook(any(DigitizedBookEntity.class))).thenAnswer(i -> i.getArgument(0));

        DigitizedBookResponse response = service.addDigitizedBook(book, physicalBookId);
        assertNotNull(response);
        assertEquals(book.id(), response.id());
        assertEquals("King Arthur in Cornwall", response.name());
        assertEquals(List.of("Dickinson W. Howship"), response.authors());
        assertEquals("The book explores the existence and life of King Arthur", response.description());
        assertEquals("Історична", response.topic());
        assertEquals(Year.of(1850), response.publishingYear());
        assertEquals("eng", response.language());
        assertEquals("Стародрук", response.resourceType());
        assertFalse(response.isCopyrighted());
        assertEquals("url", response.coverUrl());
        assertEquals("url", response.fileUrl());

        verify(repository).saveDigitizedBook(any(DigitizedBookEntity.class));
        ArgumentCaptor<BookDigitizedEvent> eventCaptor = ArgumentCaptor.forClass(BookDigitizedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(book.id(), eventCaptor.getValue().digitizedId());
        assertEquals(physicalBookId, eventCaptor.getValue().physicalId());
    }

    @Test
    void addDigitizedBook_throwsOnDuplicate() {
        DigitizedBookEntity book = entity();
        UUID physicalBookId = UUID.randomUUID();
        when(repository.exists(book.id())).thenReturn(true);

        assertThrows(DuplicateException.class, () -> service.addDigitizedBook(book, physicalBookId));
        verify(repository, never()).saveDigitizedBook(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getDigitizedBook_returnsCorrectDTO(){
        DigitizedBookEntity book = entity();
        when(repository.getDigitizedBook(book.id())).thenReturn(Optional.of(book));

        DigitizedBookResponse response = service.getDigitizedBook(book.id());
        assertNotNull(response);
        assertEquals(book.id(), response.id());
        assertEquals("King Arthur in Cornwall", response.name());
        assertEquals(List.of("Dickinson W. Howship"), response.authors());
        assertEquals("The book explores the existence and life of King Arthur", response.description());
        assertEquals("Історична", response.topic());
        assertEquals(Year.of(1850), response.publishingYear());
        assertEquals("eng", response.language());
        assertEquals("Стародрук", response.resourceType());
        assertFalse(response.isCopyrighted());
        assertEquals("url", response.coverUrl());
        assertEquals("url", response.fileUrl());

        verify(repository).getDigitizedBook(book.id());
    }

    @Test
    void getDigitizedBook_throwsOnUnknownBook(){
        when(repository.getDigitizedBook(any())).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.getDigitizedBook(UUID.randomUUID()));

        verify(repository).getDigitizedBook(any());
    }

    @Test
    void getAllDigitizedBooks_returnsAllBooks() {
        DigitizedBookEntity book1 = entity();
        DigitizedBookEntity book2 = entity();
        when(repository.getAllDigitizedBooks()).thenReturn(List.of(book1, book2));

        List<DigitizedBookResponse> responses = service.getAllDigitizedBooks();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(book1.id(), responses.get(0).id());
        assertEquals(book2.id(), responses.get(1).id());
        assertEquals(book1.name(), responses.get(0).name());
        assertEquals(book2.name(), responses.get(1).name());

        verify(repository).getAllDigitizedBooks();
    }

    @Test
    void getAllDigitizedBooks_returnsEmptyList() {
        when(repository.getAllDigitizedBooks()).thenReturn(List.of());

        List<DigitizedBookResponse> responses = service.getAllDigitizedBooks();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(repository).getAllDigitizedBooks();
    }

    @Test
    void updateDigitizedBook_successfullyUpdates() {
        DigitizedBookEntity book = entity();
        when(repository.exists(book.id())).thenReturn(true);
        when(repository.saveDigitizedBook(book)).thenReturn(book);

        DigitizedBookResponse response = service.updateDigitizedBook(book.id(), book);

        assertNotNull(response);
        assertEquals(book.id(), response.id());
        assertEquals("King Arthur in Cornwall", response.name());
        assertEquals(List.of("Dickinson W. Howship"), response.authors());
        assertEquals("The book explores the existence and life of King Arthur", response.description());
        assertEquals("Історична", response.topic());
        assertEquals(Year.of(1850), response.publishingYear());
        assertEquals("eng", response.language());
        assertEquals("Стародрук", response.resourceType());
        assertFalse(response.isCopyrighted());
        assertEquals("url", response.coverUrl());
        assertEquals("url", response.fileUrl());

        verify(repository).exists(book.id());
        verify(repository).saveDigitizedBook(book);
    }

    @Test
    void updateDigitizedBook_throwsOnUnknownBook() {
        DigitizedBookEntity book = entity();
        when(repository.exists(book.id())).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> service.updateDigitizedBook(book.id(), book));

        verify(repository, never()).saveDigitizedBook(any());
    }

    @Test
    void deleteDigitizedBook_successfullyDeletesBook() {
        UUID id = UUID.randomUUID();
        when(repository.exists(id)).thenReturn(true);

        service.deleteDigitizedBook(id);

        verify(repository).exists(id);
        verify(repository).deleteDigitizedBook(id);
    }

    @Test
    void deleteDigitizedBook_throwsOnUnknownBook() {
        UUID id = UUID.randomUUID();
        when(repository.exists(id)).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> service.deleteDigitizedBook(id));

        verify(repository, never()).deleteDigitizedBook(any());
    }

    @Test
    void exists_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        when(repository.exists(id)).thenReturn(true);

        assertTrue(service.exists(id));
        verify(repository).exists(id);
    }

}
