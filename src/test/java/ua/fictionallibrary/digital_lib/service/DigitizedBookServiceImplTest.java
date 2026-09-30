package ua.fictionallibrary.digital_lib.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ua.fictionallibrary.digital_lib.author.Author;
import ua.fictionallibrary.digital_lib.author.AuthorService;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBook;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookRepository;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.implementations.DigitizedBookServiceImpl;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    @Mock
    AuthorService authorService;
    DigitizedBookServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DigitizedBookServiceImpl(repository, eventPublisher, authorService);
    }

    private DigitizedBookRequest request() {
        return new DigitizedBookRequest("King Arthur in Cornwall", List.of("Dickinson W. Howship"),
                "The book explores the existence and life of King Arthur", "Історична",
                Year.of(1850), "eng", "Стародрук", false, "url", "url",
                UUID.randomUUID());
    }

    private DigitizedBook entity() {
        return new DigitizedBook("King Arthur in Cornwall", Set.of(new Author("Dickinson W. Howship")),
                "The book explores the existence and life of King Arthur", "Історична",
                Year.of(1850), "eng", "Стародрук", false, "url", "url");
    }

    @Test
    void addDigitizedBook_successfullyAddsAndPublishesEvent() {
        DigitizedBookRequest request = request();
        DigitizedBook book = entity();
        when(repository.save(any(DigitizedBook.class))).thenReturn(book);

        DigitizedBookResponse response = service.addDigitizedBook(request, request.physicalBookId());
        assertNotNull(response);
        assertEquals(book.getId(), response.id());
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

        verify(repository).save(any(DigitizedBook.class));
        ArgumentCaptor<BookDigitizedEvent> eventCaptor = ArgumentCaptor.forClass(BookDigitizedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(book.getId(), eventCaptor.getValue().digitizedId());
        assertEquals(request.physicalBookId(), eventCaptor.getValue().physicalId());
    }

    @Test
    void getDigitizedBook_returnsCorrectDTO(){
        DigitizedBook book = entity();
        when(repository.findById(book.getId())).thenReturn(Optional.of(book));

        DigitizedBookResponse response = service.getDigitizedBook(book.getId());
        assertNotNull(response);
        assertEquals(book.getId(), response.id());
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

        verify(repository).findById(book.getId());
    }

    @Test
    void getDigitizedBook_throwsOnUnknownBook(){
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.getDigitizedBook(UUID.randomUUID()));

        verify(repository).findById(any());
    }

    @Test
    void getAllDigitizedBooks_returnsAllBooks() {
        DigitizedBook book1 = entity();
        DigitizedBook book2 = entity();
        when(repository.findAll()).thenReturn(List.of(book1, book2));

        List<DigitizedBookResponse> responses = service.getAllDigitizedBooks();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(book1.getId(), responses.get(0).id());
        assertEquals(book2.getId(), responses.get(1).id());
        assertEquals(book1.getName(), responses.get(0).name());
        assertEquals(book2.getName(), responses.get(1).name());

        verify(repository).findAll();
    }

    @Test
    void getAllDigitizedBooks_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<DigitizedBookResponse> responses = service.getAllDigitizedBooks();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(repository).findAll();
    }

    @Test
    void updateDigitizedBook_throwsOnUnknownBook() {
        DigitizedBook book = entity();
        DigitizedBookRequest request = request();
        when(repository.findById(book.getId())).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.updateDigitizedBook(book.getId(), request));

        verify(repository, never()).save(any());
    }

    @Test
    void deleteDigitizedBook_successfullyDeletesBook() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.deleteDigitizedBook(id);

        verify(repository).existsById(id);
        verify(repository).deleteById(id);
    }

    @Test
    void deleteDigitizedBook_throwsOnUnknownBook() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> service.deleteDigitizedBook(id));

        verify(repository, never()).deleteById(any());
    }

    @Test
    void exists_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        assertTrue(service.exists(id));
        verify(repository).existsById(id);
    }

}
