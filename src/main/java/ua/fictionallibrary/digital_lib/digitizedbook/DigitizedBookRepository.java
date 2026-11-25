package ua.fictionallibrary.digital_lib.digitizedbook;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.fictionallibrary.digital_lib.common.BookFilter;

import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DigitizedBookRepository extends JpaRepository<DigitizedBook, UUID> {
    @Override
    @NullMarked
    @Query("SELECT DISTINCT b FROM DigitizedBook b LEFT JOIN FETCH b.authors")
    List<DigitizedBook> findAll();

    @Override
    @NullMarked
    @Query("SELECT DISTINCT b FROM DigitizedBook b LEFT JOIN FETCH b.authors WHERE b.id = :digitizedBookId")
    Optional<DigitizedBook> findById(UUID digitizedBookId);

    @Query("""
            SELECT DISTINCT b FROM DigitizedBook b LEFT JOIN FETCH b.authors
            WHERE (:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', CAST(:name AS String), '%')))
              AND (:author IS NULL OR EXISTS (
                    SELECT 1 FROM DigitizedBook b2 JOIN b2.authors a
                    WHERE b2 = b AND LOWER(a.name) LIKE LOWER(CONCAT('%', CAST(:author AS String), '%'))))
              AND (:topic IS NULL OR LOWER(b.topic) LIKE LOWER(CONCAT('%', CAST(:topic AS String), '%')))
              AND (:yearFrom IS NULL OR b.publishingYear >= :yearFrom)
              AND (:yearTo IS NULL OR b.publishingYear <= :yearTo)
              AND (:language IS NULL OR LOWER(b.language) LIKE LOWER(CONCAT('%', CAST(:language AS String), '%')))
              AND (:resourceType IS NULL OR b.resourceType = :resourceType)
            """)
    List<DigitizedBook> search(String name, String author, String topic, Year yearFrom, Year yearTo,
                               String language, String resourceType);

    @Query("SELECT DISTINCT b FROM DigitizedBook b LEFT JOIN FETCH b.authors WHERE b.id IN :ids")
    List<DigitizedBook> findAllWithAuthorsByIdIn(Collection<UUID> ids);

    @Query("SELECT b.pdfKey FROM DigitizedBook b")
    List<String> findAllPdfKeys();

    @Query("SELECT b.coverKey FROM DigitizedBook b WHERE b.coverKey IS NOT NULL")
    List<String> findAllCoverKeys();

    default List<DigitizedBook> search(BookFilter f) {
        return search(f.name(), f.author(), f.topic(), f.yearFrom(), f.yearTo(), f.language(), f.resourceType());
    }
}
