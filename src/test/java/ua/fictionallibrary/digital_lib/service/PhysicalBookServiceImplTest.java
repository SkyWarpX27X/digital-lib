package ua.fictionallibrary.digital_lib.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookAddedEvent;
import ua.fictionallibrary.digital_lib.physicalbook.implementations.ModernBookValidationStrategy;
import ua.fictionallibrary.digital_lib.physicalbook.implementations.OldBookValidationStrategy;
import ua.fictionallibrary.digital_lib.physicalbook.ResourceTypeValidationStrategy;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.exception.IllegalResourceTypeException;
import ua.fictionallibrary.digital_lib.physicalbook.model.dto.PhysicalBookResponse;
import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBookEntity;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookRepository;
import ua.fictionallibrary.digital_lib.physicalbook.implementations.PhysicalBookServiceImpl;

import java.time.Year;
import java.util.List;
import java.util.Optional;
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
    PhysicalBookServiceImpl service;

    @BeforeEach
    void setUp() {
        List<ResourceTypeValidationStrategy> strategies = List.of(
                new OldBookValidationStrategy(),
                new ModernBookValidationStrategy()
        );
        service = new PhysicalBookServiceImpl(repository, strategies, eventPublisher);
    }

    @Test
    void successfullyCreateBook() {
        UUID bookId = UUID.randomUUID();
        PhysicalBookEntity book = new PhysicalBookEntity(bookId, "Idea medicinae philosophicae", List.of("Petro Severino"),
                "description", "Історія медицини", Year.of(1660), "lat", "Стародрук");
        when(repository.exists(bookId)).thenReturn(false);
        when(repository.savePhysicalBook(any(PhysicalBookEntity.class))).thenAnswer(i -> i.getArgument(0));

        PhysicalBookResponse response = service.addPhysicalBook(book);
        assertNotNull(response);
        assertEquals(bookId, response.id());
        assertEquals("Idea medicinae philosophicae", response.name());
        assertEquals(List.of("Petro Severino"), response.authors());
        assertEquals("description", response.description());
        assertEquals("Історія медицини", response.topic());
        assertEquals(Year.of(1660), response.publishingYear());
        assertEquals("lat", response.language());
        assertEquals("Стародрук", response.resourceType());

        verify(repository).savePhysicalBook(any(PhysicalBookEntity.class));
        ArgumentCaptor<PhysicalBookAddedEvent> eventCaptor = ArgumentCaptor.forClass(PhysicalBookAddedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(bookId, eventCaptor.getValue().id());
    }

    @Test
    void tryToAddDuplicateIdBook() {
        UUID bookId = UUID.randomUUID();
        PhysicalBookEntity book = new PhysicalBookEntity(bookId, "Idea medicinae philosophicae", List.of("Petro Severino"),
                "description", "Історія медицини", Year.of(1660), "lat", "Стародрук");
        when(repository.exists(bookId)).thenReturn(true);

        assertThrows(DuplicateException.class, () -> service.addPhysicalBook(book));
        verify(repository, never()).savePhysicalBook(any());
    }

    @Test
    void tryToAddBookWithInvalidResourceType() {
        UUID bookId = UUID.randomUUID();
        PhysicalBookEntity book = new PhysicalBookEntity(bookId, "Idea medicinae philosophicae", List.of("Petro Severino"),
                "description", "Історія медицини", Year.of(2013), "lat", "Стародрук");
        when(repository.exists(bookId)).thenReturn(false);

        assertThrows(IllegalResourceTypeException.class, () -> service.addPhysicalBook(book));
        verify(repository, never()).savePhysicalBook(any());
    }

    @Test
    void successfullyGetBook(){
        UUID bookId = UUID.randomUUID();
        when(repository.getPhysicalBook(bookId)).thenReturn(Optional.of(new PhysicalBookEntity(bookId, "Idea medicinae philosophicae",
                List.of("Petro Severino"), "description", "Історія медицини", Year.of(1660), "lat", "Стародрук")));

        PhysicalBookResponse response = service.getPhysicalBook(bookId);
        assertNotNull(response);
        assertEquals(bookId, response.id());
        assertEquals("Idea medicinae philosophicae", response.name());
        assertEquals(List.of("Petro Severino"), response.authors());
        assertEquals("description", response.description());
        assertEquals("Історія медицини", response.topic());
        assertEquals(Year.of(1660), response.publishingYear());
        assertEquals("lat", response.language());
        assertEquals("Стародрук", response.resourceType());

        verify(repository).getPhysicalBook(bookId);
    }

    @Test
    void tryToGetNonExistentBook(){
        when(repository.getPhysicalBook(any())).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.getPhysicalBook(UUID.randomUUID()));

        verify(repository).getPhysicalBook(any());
    }
}