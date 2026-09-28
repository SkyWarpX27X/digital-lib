package ua.fictionallibrary.digital_lib.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    //UserEntity saveUser(UserEntity entity);
    //Optional<UserEntity> getUser(UUID userId);
    //List<UserEntity> getAllUsers();
    //void deleteUser(UUID userId);
    //boolean existsById(UUID userId);
    boolean existsByLogin(String login);
}
