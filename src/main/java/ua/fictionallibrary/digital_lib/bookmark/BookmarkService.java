package ua.fictionallibrary.digital_lib.bookmark;

import ua.fictionallibrary.digital_lib.bookmark.model.BookmarkEntity;
import ua.fictionallibrary.digital_lib.bookmark.model.dto.BookmarkResponse;

import java.util.UUID;

public interface BookmarkService {
    BookmarkResponse addBookmark(BookmarkEntity bookmarkEntity);
    BookmarkResponse getBookmark(UUID userId, UUID bookId);
    void deleteBookmark(UUID userId, UUID bookId);
    BookmarkResponse updatePageNumber(UUID userId, UUID bookId, UpdatePageNumberCommand command);
}
