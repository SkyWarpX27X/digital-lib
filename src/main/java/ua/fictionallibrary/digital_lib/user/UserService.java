package ua.fictionallibrary.digital_lib.user;

import ua.fictionallibrary.digital_lib.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse getUser(UUID userId);
    List<UserResponse> getAllUsers();
    UserResponse addUser(User user);
    UserResponse updateUser(UUID userId, User user);
    void deleteUser(UUID userId);
    boolean existsById(UUID userId);
    boolean existsByLogin(String login);
}
