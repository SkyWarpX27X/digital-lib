package ua.fictionallibrary.digital_lib.digitizedbook;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DigitizedBookRepository extends JpaRepository<DigitizedBook, UUID> {
    @Override
    @NullMarked
    @Query("SELECT DISTINCT b FROM DigitizedBook b JOIN FETCH b.authors")
    List<DigitizedBook> findAll();
    @Override
    @NullMarked
    @Query("SELECT DISTINCT b FROM DigitizedBook b JOIN FETCH b.authors WHERE b.id = :digitizedBookId")
    Optional<DigitizedBook> findById(UUID digitizedBookId);
}
