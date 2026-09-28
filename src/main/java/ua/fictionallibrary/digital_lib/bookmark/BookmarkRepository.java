package ua.fictionallibrary.digital_lib.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkId> {
    //BookmarkEntity saveBookmark(BookmarkEntity bookmark);
    //Optional<BookmarkEntity> getBookmark(UUID userId, UUID bookId);
    //void deleteBookmark(UUID userId, UUID bookId);
    //boolean exists(UUID userId, UUID bookId);
}
