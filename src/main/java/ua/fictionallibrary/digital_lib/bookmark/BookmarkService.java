package ua.fictionallibrary.digital_lib.bookmark;

import ua.fictionallibrary.digital_lib.bookmark.dto.BookmarkResponse;

import java.util.UUID;

public interface BookmarkService {
    BookmarkResponse addBookmark(Bookmark bookmark);
    BookmarkResponse getBookmark(UUID userId, UUID bookId);
    void deleteBookmark(UUID userId, UUID bookId);
    BookmarkResponse updatePageNumber(UUID userId, UUID bookId, UpdatePageNumberCommand command);
}
