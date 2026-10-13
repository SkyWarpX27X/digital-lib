package ua.fictionallibrary.digital_lib.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.fictionallibrary.digital_lib.starter.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.user.User;
import ua.fictionallibrary.digital_lib.user.UserRepository;
import ua.fictionallibrary.digital_lib.user.UserService;
import ua.fictionallibrary.digital_lib.user.implementation.UserServiceImpl;
import ua.fictionallibrary.digital_lib.user.UserRole;
import ua.fictionallibrary.digital_lib.user.dto.UserRequest;

import java.util.Optional;
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
    @Mock
    PasswordEncoder passwordEncoder;

    private UserService service;

    @BeforeEach
    public void setup() {
        service = new UserServiceImpl(userRepository, eventPublisher, passwordEncoder);
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
        final var user = new User(userRequest.username(), userRequest.password(), userRequest.name(), userRequest.surname(),
                userRequest.patronymic(), userRequest.role(), userRequest.isActive());
        when(passwordEncoder.encode(userRequest.password())).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.existsById(user.getId())).thenReturn(false);

        final var response = service.addUser(user);
        when(userRepository.existsById(user.getId())).thenReturn(true);
        assertNotNull(response);
        assertEquals(response.name(), user.getName());
        assertEquals(response.surname(), user.getSurname());
        assertEquals(response.patronymic(), user.getPatronymic());
        assertEquals(response.role(), user.getRole());
        assertEquals("encoded", user.getPassword());
        assert(service.existsById(response.id()));

        verify(userRepository).save(user);
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
        final var user = new User(userRequest.username(), userRequest.password(), userRequest.name(), userRequest.surname(),
                userRequest.patronymic(), userRequest.role(), userRequest.isActive());
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> service.updateUser(user.getId(), user));
    }

    @Test
    void deletingNonExistentThrows() {
        final var uuid = UUID.randomUUID();
        when(userRepository.existsById(uuid)).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> service.deleteUser(uuid));
    }
}
