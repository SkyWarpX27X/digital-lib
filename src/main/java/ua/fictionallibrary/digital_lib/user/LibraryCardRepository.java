package ua.fictionallibrary.digital_lib.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LibraryCardRepository extends JpaRepository<LibraryCard, UUID> {
    Optional<LibraryCard> findByOwnerId(UUID userId);
    boolean existsByOwnerId(UUID userId);

    @Query("SELECT c FROM LibraryCard c JOIN FETCH c.user u ORDER BY u.surname, u.name")
    List<LibraryCard> findAllWithOwner();

}
