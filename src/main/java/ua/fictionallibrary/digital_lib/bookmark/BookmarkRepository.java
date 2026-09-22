package ua.fictionallibrary.digital_lib.bookmark;

import ua.fictionallibrary.digital_lib.bookmark.model.BookmarkEntity;
import java.util.Optional;
import java.util.UUID;

public interface BookmarkRepository {
    BookmarkEntity saveBookmark(BookmarkEntity bookmark);
    Optional<BookmarkEntity> getBookmark(UUID userId, UUID bookId);
    void deleteBookmark(UUID userId, UUID bookId);
    boolean exists(UUID userId, UUID bookId);
}
