package ua.fictionallibrary.digital_lib.user.implementation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.fictionallibrary.digital_lib.exception.SelfDeactivationException;
import ua.fictionallibrary.digital_lib.starter.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.starter.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.user.*;
import ua.fictionallibrary.digital_lib.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, ApplicationEventPublisher eventPublisher, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        return userRepository.findById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("User " + userId + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public UserResponse addUser(User user) {
        if (userRepository.existsByUsername(user.getUsername()))
            throw new DuplicateException("User with username " + user.getUsername() + " already exists");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserAddedEvent(
                saved.toString()
        ));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, User user, UUID actorId, boolean byAdmin) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Can't update non-existent user " + userId));
        if (!existing.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(user.getUsername()))
            throw new DuplicateException("User with username " + user.getUsername() + " already exists");
        existing.setUsername(user.getUsername());
        existing.setPassword(passwordEncoder.encode(user.getPassword()));
        existing.setName(user.getName());
        existing.setSurname(user.getSurname());
        existing.setPatronymic(user.getPatronymic());
        if (byAdmin) {
            requireNotSelfDeactivation(userId, user.isActive(), actorId);
            existing.setRole(user.getRole());
            existing.setActive(user.isActive());
        }
        return toResponse(userRepository.save(existing));
    }

    @Override
    @Transactional
    public UserResponse updateRoleAndStatus(UUID userId, UserRole role, Boolean isActive, UUID actorId) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Can't update non-existent user " + userId));
        if (isActive != null) {
            requireNotSelfDeactivation(userId, isActive, actorId);
            existing.setActive(isActive);
        }
        if (role != null) existing.setRole(role);
        return toResponse(userRepository.save(existing));
    }

    private static void requireNotSelfDeactivation(UUID userId, boolean isActive, UUID actorId) {
        if (!isActive && userId.equals(actorId))
            throw new SelfDeactivationException("Адміністратор не може деактивувати власний обліковий запис");
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId))
            throw new DataNotFoundException("Can't delete non-existent user " + userId);
        userRepository.deleteById(userId);
    }

    @Override
    public boolean existsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public boolean existsByLogin(String login) {
        return userRepository.existsByUsername(login);
    }


    private UserResponse toResponse(User entity) {
        return new UserResponse(entity.getId(), entity.getName(), entity.getSurname(), entity.getPatronymic(), entity.getRole(), entity.isActive());
    }
}
