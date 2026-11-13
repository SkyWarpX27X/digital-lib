package ua.fictionallibrary.digital_lib.bookorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BookOrderRepository extends JpaRepository<BookOrder, UUID> {

    boolean existsByUserIdAndBookIdAndIsOpenTrue(UUID userId, UUID bookId);
    List<BookOrder> findByBookIdAndIsOpenTrue(UUID bookId);
    @Query("SELECT e FROM BookOrder e JOIN FETCH e.user WHERE :onlyOpen = false OR (e.isOpen = true)")
    List<BookOrder> findBooks(boolean onlyOpen);
    @Query("SELECT e FROM BookOrder e JOIN FETCH e.user WHERE e.user.id = :userId")
    List<BookOrder> findByUserId(UUID userId);
}
