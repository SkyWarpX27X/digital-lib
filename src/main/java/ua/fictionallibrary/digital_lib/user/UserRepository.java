package ua.fictionallibrary.digital_lib.user;

import ua.fictionallibrary.digital_lib.user.model.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    UserEntity saveUser(UserEntity entity);
    Optional<UserEntity> getUser(UUID userId);
    List<UserEntity> getAllUsers();
    void deleteUser(UUID userId);
    boolean exists(UUID userId);
}
