package ua.fictionallibrary.digital_lib.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.user.UserRepository;
import ua.fictionallibrary.digital_lib.user.UserService;
import ua.fictionallibrary.digital_lib.user.implementation.UserServiceImpl;
import ua.fictionallibrary.digital_lib.user.model.UserEntity;
import ua.fictionallibrary.digital_lib.user.model.UserRole;
import ua.fictionallibrary.digital_lib.user.model.dto.UserRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;

    private UserService service;

    @BeforeEach
    public void setup() {
        service = new UserServiceImpl(userRepository, eventPublisher);
    }

    @Test
    void successfullyAddedUser() {
        final var userRequest = new UserRequest(
                "wwnothasmile",
                "09254a@!_m~~",
                "Illia",
                "Illienko",
                "Illiovych",
                UserRole.READER,
                true);
        final var user = new UserEntity(UUID.randomUUID(), userRequest);
        when(userRepository.saveUser(user)).thenReturn(user);
        when(userRepository.exists(user.id())).thenReturn(false);

        final var response = service.addUser(user);
        assertNotNull(response);
        assertEquals(response.name(), user.name());
        assertEquals(response.surname(), user.surname());
        assertEquals(response.patronymic(), user.patronymic());
        assertEquals(response.role(), user.role());
        assert(service.exists(response.id()));

        verify(userRepository).saveUser(user);
    }

    @Test
    void updatingNonExistentUserThrows() {
        final var userRequest = new UserRequest(
                "wwnothasmile",
                "09254a@!_m~~",
                "Illia",
                "Illienko",
                "Illiovych",
                UserRole.READER,
                true);
        final var user = new UserEntity(UUID.randomUUID(), userRequest);
        when(userRepository.exists(user.id())).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> service.updateUser(user.id(), user));
    }

    @Test
    void deletingNonExistentThrows() {
        final var uuid = UUID.randomUUID();
        when(userRepository.exists(uuid)).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> service.deleteUser(uuid));
    }
}
