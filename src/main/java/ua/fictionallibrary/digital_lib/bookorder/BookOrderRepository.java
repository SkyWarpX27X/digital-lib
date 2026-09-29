package ua.fictionallibrary.digital_lib.bookorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookOrderRepository extends JpaRepository<BookOrder, UUID> {

    boolean existsByBook(UUID bookId);
    Optional<BookOrder> findByBook(UUID bookId);
    @Query("SELECT e FROM BookOrder e WHERE :onlyOpen = false OR (e.isOpen = true)")
    List<BookOrder> findBooks(boolean onlyOpen);
}
