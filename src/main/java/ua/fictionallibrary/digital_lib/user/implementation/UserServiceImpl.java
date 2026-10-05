package ua.fictionallibrary.digital_lib.user.implementation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.user.*;
import ua.fictionallibrary.digital_lib.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserServiceImpl(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
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
        if (userRepository.existsByLogin(user.getLogin()))
            throw new DuplicateException("User with login " + user.getLogin() + " already exists");
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserAddedEvent(
                saved.toString()
        ));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, User user) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Can't update non-existent user " + userId));
        if (!existing.getLogin().equals(user.getLogin()) && userRepository.existsByLogin(user.getLogin()))
            throw new DuplicateException("User with login " + user.getLogin() + " already exists");
        existing.setLogin(user.getLogin());
        existing.setPassword(user.getPassword());
        existing.setName(user.getName());
        existing.setSurname(user.getSurname());
        existing.setPatronymic(user.getPatronymic());
        existing.setRole(user.getRole());
        existing.setActive(user.isActive());
        return toResponse(userRepository.save(existing));
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
        return userRepository.existsByLogin(login);
    }


    private UserResponse toResponse(User entity) {
        return new UserResponse(entity.getId(), entity.getName(), entity.getSurname(), entity.getPatronymic(), entity.getRole(), entity.isActive());
    }
}
