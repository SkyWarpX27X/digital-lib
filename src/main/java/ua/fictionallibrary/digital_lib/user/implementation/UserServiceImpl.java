package ua.fictionallibrary.digital_lib.user.implementation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
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
    public UserResponse getUser(UUID userId) {
        return userRepository.findById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("User " + userId + " not found"));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public UserResponse addUser(User user) {
        if (userRepository.existsById(user.getId()))
            throw new DuplicateException("User with id " + user.getId() + " already exists");
        eventPublisher.publishEvent(new UserAddedEvent(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getPatronymic()
        ));
        return toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(UUID userId, User user) {
        if (!userRepository.existsById(userId))
            throw new DataNotFoundException("Can't update non-existent user " + userId);
        return toResponse(userRepository.save(user));
    }

    @Override
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
