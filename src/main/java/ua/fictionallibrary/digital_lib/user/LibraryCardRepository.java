package ua.fictionallibrary.digital_lib.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LibraryCardRepository extends JpaRepository<LibraryCard, UUID> {
    Optional<LibraryCard> findByOwnerId(UUID userId);
    boolean existsByOwnerId(UUID userId);

}
