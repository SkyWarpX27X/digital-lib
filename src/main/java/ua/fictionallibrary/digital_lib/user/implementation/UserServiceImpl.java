package ua.fictionallibrary.digital_lib.user.implementation;

import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.user.UserRepository;
import ua.fictionallibrary.digital_lib.user.UserService;
import ua.fictionallibrary.digital_lib.user.model.UserEntity;
import ua.fictionallibrary.digital_lib.user.model.dto.UserRequest;
import ua.fictionallibrary.digital_lib.user.model.dto.UserResponse;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    public UserResponse addUser(UserRequest user) {
        UUID uuid = UUID.randomUUID();
        return toResponse(userRepository.saveUser(toEntity(uuid, user)));
    }

    @Override
    public UserResponse updateUser(UUID userId, UserRequest user) {
        if (!userRepository.exists(userId))
            throw new DataNotFoundException("Can't update non-existent user " + userId);
        return toResponse(userRepository.saveUser(toEntity(userId, user)));
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

    private UserEntity toEntity(UUID userId, UserRequest request) {
        return new UserEntity(userId, request);
    }

    private UserResponse toResponse(UserEntity entity) {
        return new UserResponse(entity);
    }
}
