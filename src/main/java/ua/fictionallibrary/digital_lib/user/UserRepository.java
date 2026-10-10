package ua.fictionallibrary.digital_lib.user;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Override
    @NullMarked
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.libraryCard")
    List<User> findAll();
    @Override
    @NullMarked
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.libraryCard WHERE u.id = :userId")
    Optional<User> findById(UUID userId);
    boolean existsByUsername(String login);
}
