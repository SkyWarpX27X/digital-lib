package ua.fictionallibrary.digital_lib.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardRepository;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardService;
import ua.fictionallibrary.digital_lib.librarycard.implementation.LibraryCardServiceImpl;
import ua.fictionallibrary.digital_lib.librarycard.model.LibraryCardEntity;
import ua.fictionallibrary.digital_lib.librarycard.model.dto.LibraryCardRequest;
import ua.fictionallibrary.digital_lib.user.UserRepository;
import ua.fictionallibrary.digital_lib.user.UserService;
import ua.fictionallibrary.digital_lib.user.implementation.UserServiceImpl;
import ua.fictionallibrary.digital_lib.user.model.UserEntity;
import ua.fictionallibrary.digital_lib.user.model.UserRole;
import ua.fictionallibrary.digital_lib.user.model.dto.UserRequest;

import java.time.Year;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryCardServiceImplTest {

    @Mock
    private LibraryCardRepository libraryCardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;

    private LibraryCardService libraryCardService;
    private UserService userService;

    @BeforeEach
    public void setup() {
        userService = new UserServiceImpl(userRepository, eventPublisher);
        libraryCardService = new LibraryCardServiceImpl(libraryCardRepository, eventPublisher, userService);
    }

    @Test
    void successfullyAddedLibraryCardForExistentUser() {
        final var userId = UUID.randomUUID();
        final var request = new LibraryCardRequest(
            "wwww@wwww.com",
                "04210",
                Year.of(2006),
                "Hahahahahaha",
                "AAAAA",
                "Meme org",
                "Senior idiot"
        );
        final var libraryCard = new LibraryCardEntity(userId, request);
        when(userRepository.exists(userId)).thenReturn(true);
        when(libraryCardRepository.exists(userId)).thenReturn(false);
        when(libraryCardRepository.saveLibraryCard(libraryCard)).thenReturn(libraryCard);

        final var response = libraryCardService.addLibraryCard(userId, libraryCard);
        assertNotNull(response);
        assertEquals(response.ownerId(), libraryCard.ownerId());

        verify(libraryCardRepository).saveLibraryCard(libraryCard);
    }

    @Test
    void throwsOnAddedLibraryCardForNonExistentUser() {
        final var userId = UUID.randomUUID();
        final var request = new LibraryCardRequest(
                "wwww@wwww.com",
                "04210",
                Year.of(2006),
                "Hahahahahaha",
                "AAAAA",
                "Meme org",
                "Senior idiot"
        );
        final var libraryCard = new LibraryCardEntity(userId, request);
        when(userRepository.exists(userId)).thenReturn(false);
        assertThrows(DataNotFoundException.class, () -> libraryCardService.addLibraryCard(userId, libraryCard));
    }

    @Test
    void throwsOnAddedDuplicateLibraryCard() {
        final var userId = UUID.randomUUID();
        final var request = new LibraryCardRequest(
                "wwww@wwww.com",
                "04210",
                Year.of(2006),
                "Hahahahahaha",
                "AAAAA",
                "Meme org",
                "Senior idiot"
        );
        final var libraryCard = new LibraryCardEntity(userId, request);
        when(userRepository.exists(userId)).thenReturn(true);
        when(libraryCardService.exists(userId)).thenReturn(true);
        assertThrows(DuplicateException.class, () -> libraryCardService.addLibraryCard(userId, libraryCard));
    }

    @Test
    void updatingNonExistentLibraryCardThrows() {
        final var userId = UUID.randomUUID();
        final var request = new LibraryCardRequest(
                "wwww@wwww.com",
                "04210",
                Year.of(2006),
                "Hahahahahaha",
                "AAAAA",
                "Meme org",
                "Senior idiot"
        );
        final var libraryCard = new LibraryCardEntity(userId, request);
        when(libraryCardService.exists(userId)).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> libraryCardService.updateLibraryCard(userId, libraryCard));
    }

    @Test
    void deletingNonExistentThrows() {
        final var userId = UUID.randomUUID();
        when(libraryCardService.exists(userId)).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> libraryCardService.deleteLibraryCard(userId));
    }
}
