package ua.fictionallibrary.digital_lib.user.implementation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.user.UserAddedEvent;
import ua.fictionallibrary.digital_lib.user.UserRepository;
import ua.fictionallibrary.digital_lib.user.UserService;
import ua.fictionallibrary.digital_lib.user.model.UserEntity;
import ua.fictionallibrary.digital_lib.user.model.dto.UserResponse;

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
        return userRepository.getUser(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("User " + userId + " not found"));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.getAllUsers().stream().map(this::toResponse).toList();
    }

    @Override
    public UserResponse addUser(UserEntity user) {
        if (userRepository.exists(user.id()))
            throw new DuplicateException("User with id " + user.id() + " already exists");
        eventPublisher.publishEvent(new UserAddedEvent(
                user.id(),
                user.name(),
                user.surname(),
                user.patronymic()
        ));
        return toResponse(userRepository.saveUser(user));
    }

    @Override
    public UserResponse updateUser(UUID userId, UserEntity user) {
        if (!userRepository.exists(userId))
            throw new DataNotFoundException("Can't update non-existent user " + userId);
        return toResponse(userRepository.saveUser(user));
    }

    @Override
    public void deleteUser(UUID userId) {
        if (!userRepository.exists(userId))
            throw new DataNotFoundException("Can't delete non-existent user " + userId);
        userRepository.deleteUser(userId);
    }

    @Override
    public boolean exists(UUID userId) {
        return userRepository.exists(userId);
    }

    private UserResponse toResponse(UserEntity entity) {
        return new UserResponse(entity);
    }
}
