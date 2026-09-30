package ua.fictionallibrary.digital_lib.physicalbook;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhysicalBookRepository extends JpaRepository<PhysicalBook, UUID> {
    @Override
    @NullMarked
    @Query("SELECT DISTINCT b FROM PhysicalBook b LEFT JOIN FETCH b.authors")
    List<PhysicalBook> findAll();
    @Override
    @NullMarked
    @Query("SELECT DISTINCT b FROM PhysicalBook b LEFT JOIN FETCH b.authors WHERE b.id = :physicalBookId")
    Optional<PhysicalBook> findById(UUID physicalBookId);
}
