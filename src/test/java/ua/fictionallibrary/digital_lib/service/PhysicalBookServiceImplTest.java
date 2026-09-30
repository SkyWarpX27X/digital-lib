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
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookAddedEvent;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.physicalbook.implementations.ModernBookValidationStrategy;
import ua.fictionallibrary.digital_lib.physicalbook.implementations.OldBookValidationStrategy;
import ua.fictionallibrary.digital_lib.physicalbook.ResourceTypeValidationStrategy;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.IllegalResourceTypeException;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBook;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookResponse;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookRepository;
import ua.fictionallibrary.digital_lib.physicalbook.implementations.PhysicalBookServiceImpl;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhysicalBookServiceImplTest {
    @Mock
    PhysicalBookRepository repository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    AuthorService authorService;
    PhysicalBookServiceImpl service;

    @BeforeEach
    void setUp() {
        List<ResourceTypeValidationStrategy> strategies = List.of(
                new OldBookValidationStrategy(),
                new ModernBookValidationStrategy()
        );
        service = new PhysicalBookServiceImpl(repository, strategies, eventPublisher, authorService);
    }

    private PhysicalBookRequest request(Year year) {
        return new PhysicalBookRequest("Idea medicinae philosophicae", List.of("Petro Severino"),
                "description", "Історія медицини", year, "lat", "Стародрук");
    }

    private PhysicalBook entity() {
        return new PhysicalBook("Idea medicinae philosophicae", Set.of(new Author("Petro Severino")),
                "description", "Історія медицини", Year.of(1660), "lat", "Стародрук");
    }

    @Test
    void successfullyCreateBook() {
        PhysicalBook book = entity();
        UUID bookId = book.getId();
        when(authorService.findOrCreate(any())).thenReturn(Set.of(new Author("Petro Severino")));
        when(repository.save(any(PhysicalBook.class))).thenAnswer(i -> i.getArgument(0));

        PhysicalBookResponse response = service.addPhysicalBook(request(Year.of(1660)));
        assertNotNull(response);
        assertEquals(bookId, response.id());
        assertEquals("Idea medicinae philosophicae", response.name());
        assertEquals(List.of("Petro Severino"), response.authors());
        assertEquals("description", response.description());
        assertEquals("Історія медицини", response.topic());
        assertEquals(Year.of(1660), response.publishingYear());
        assertEquals("lat", response.language());
        assertEquals("Стародрук", response.resourceType());

        verify(repository).save(any(PhysicalBook.class));
        ArgumentCaptor<PhysicalBookAddedEvent> eventCaptor = ArgumentCaptor.forClass(PhysicalBookAddedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(bookId, eventCaptor.getValue().id());
    }

    @Test
    void tryToAddBookWithInvalidResourceType() {
        when(authorService.findOrCreate(any())).thenReturn(Set.of(new Author("Petro Severino")));

        assertThrows(IllegalResourceTypeException.class, () -> service.addPhysicalBook(request(Year.of(2013))));
        verify(repository, never()).save(any());
    }

    @Test
    void successfullyGetBook(){
        PhysicalBook book = entity();
        UUID bookId = book.getId();
        when(repository.findById(bookId)).thenReturn(Optional.of(book));

        PhysicalBookResponse response = service.getPhysicalBook(bookId);
        assertNotNull(response);
        assertEquals("Idea medicinae philosophicae", response.name());
        assertEquals(List.of("Petro Severino"), response.authors());
        assertEquals("description", response.description());
        assertEquals("Історія медицини", response.topic());
        assertEquals(Year.of(1660), response.publishingYear());
        assertEquals("lat", response.language());
        assertEquals("Стародрук", response.resourceType());

        verify(repository).findById(bookId);
    }

    @Test
    void tryToGetNonExistentBook(){
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.getPhysicalBook(UUID.randomUUID()));

        verify(repository).findById(any());
    }
}