package ua.fictionallibrary.digital_lib.physicalbook;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.fictionallibrary.digital_lib.common.BookFilter;

import java.time.Year;
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

    @Query("""
            SELECT DISTINCT b FROM PhysicalBook b LEFT JOIN FETCH b.authors
            WHERE (:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', CAST(:name AS String), '%')))
              AND (:author IS NULL OR EXISTS (
                    SELECT 1 FROM PhysicalBook b2 JOIN b2.authors a
                    WHERE b2 = b AND LOWER(a.name) LIKE LOWER(CONCAT('%', CAST(:author AS String), '%'))))
              AND (:topic IS NULL OR LOWER(b.topic) LIKE LOWER(CONCAT('%', CAST(:topic AS String), '%')))
              AND (:yearFrom IS NULL OR b.publishingYear >= :yearFrom)
              AND (:yearTo IS NULL OR b.publishingYear <= :yearTo)
              AND (:language IS NULL OR LOWER(b.language) LIKE LOWER(CONCAT('%', CAST(:language AS String), '%')))
              AND (:resourceType IS NULL OR b.resourceType = :resourceType)
            """)
    List<PhysicalBook> search(String name, String author, String topic, Year yearFrom, Year yearTo,
                              String language, String resourceType);

    default List<PhysicalBook> search(BookFilter f) {
        return search(f.name(), f.author(), f.topic(), f.yearFrom(), f.yearTo(), f.language(), f.resourceType());
    }
}
