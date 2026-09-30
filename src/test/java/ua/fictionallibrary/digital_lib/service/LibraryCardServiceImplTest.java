package ua.fictionallibrary.digital_lib.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.user.*;
import ua.fictionallibrary.digital_lib.user.implementation.LibraryCardServiceImpl;
import ua.fictionallibrary.digital_lib.user.dto.LibraryCardRequest;
import ua.fictionallibrary.digital_lib.user.implementation.UserServiceImpl;

import java.time.Year;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LibraryCardServiceImplTest {

    @Mock
    private LibraryCardRepository libraryCardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    UserService userService;

    private LibraryCardService libraryCardService;

    @BeforeEach
    public void setup() {
        userService = new UserServiceImpl(userRepository, eventPublisher);
        libraryCardService = new LibraryCardServiceImpl(libraryCardRepository, userRepository, eventPublisher, userService);
    }

    private LibraryCardRequest request() {
        return new LibraryCardRequest(
                "wwww@wwww.com",
                "04210",
                Year.of(2006),
                "Hahahahahaha",
                "AAAAA",
                "Meme org",
                "Senior idiot"
        );
    }

    @Test
    void successfullyAddedLibraryCardForExistentUser() {
        final var userId = UUID.randomUUID();
        final var user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        final var libraryCard = new LibraryCard(userId, "wwww@wwww.com", "04210", Year.of(2006),
                "Hahahahahaha", "AAAAA", "Meme org", "Senior idiot", user);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(libraryCardRepository.existsById(userId)).thenReturn(false);
        when(libraryCardRepository.save(any(LibraryCard.class))).thenReturn(libraryCard);


        final var response = libraryCardService.addLibraryCard(userId, request());
        assertNotNull(response);
        assertEquals(userId, response.ownerId());
        assertEquals("wwww@wwww.com", response.email());

        verify(libraryCardRepository).save(any(LibraryCard.class));
        verify(eventPublisher).publishEvent(any(LibraryCardAddedEvent.class));
    }

    @Test
    void throwsOnAddedLibraryCardForNonExistentUser() {
        final var userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> libraryCardService.addLibraryCard(userId, request()));

        verify(libraryCardRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(LibraryCardAddedEvent.class));
    }

    @Test
    void throwsOnAddedDuplicateLibraryCard() {
        final var userId = UUID.randomUUID();
        final var user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(libraryCardRepository.existsById(userId)).thenReturn(true);
        assertThrows(DuplicateException.class, () -> libraryCardService.addLibraryCard(userId, request()));
        verify(libraryCardRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(LibraryCardAddedEvent.class));
    }

    @Test
    void updatingNonExistentLibraryCardThrows() {
        final var userId = UUID.randomUUID();
        when(libraryCardRepository.existsById(userId)).thenReturn(false);
        assertThrows(DataNotFoundException.class, () -> libraryCardService.updateLibraryCard(userId, request()));
        verify(libraryCardRepository, never()).save(any());
    }

    @Test
    void deletingNonExistentThrows() {
        final var userId = UUID.randomUUID();
        when(libraryCardRepository.existsByOwnerId(userId)).thenReturn(false);
        assertThrows(DataNotFoundException.class, () -> libraryCardService.deleteLibraryCard(userId));
        verify(libraryCardRepository, never()).deleteById(any());
    }
}
