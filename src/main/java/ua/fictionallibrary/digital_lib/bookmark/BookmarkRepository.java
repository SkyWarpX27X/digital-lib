package ua.fictionallibrary.digital_lib.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkId> {
    List<Bookmark> findByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.bookId = :bookId")
    void deleteByBookId(UUID bookId);
}
