package ua.fictionallibrary.digital_lib.user;

import ua.fictionallibrary.digital_lib.user.model.dto.UserRequest;
import ua.fictionallibrary.digital_lib.user.model.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse getUser(UUID userId);
    List<UserResponse> getAllUsers();
    UserResponse addUser(UserRequest user);
    UserResponse updateUser(UUID userId, UserRequest user);
    void deleteUser(UUID userId);
    boolean exists(UUID userId);
}
