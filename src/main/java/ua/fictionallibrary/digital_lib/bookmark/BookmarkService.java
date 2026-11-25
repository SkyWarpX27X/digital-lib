package ua.fictionallibrary.digital_lib.bookmark;

import ua.fictionallibrary.digital_lib.bookmark.dto.BookmarkResponse;

import java.util.List;
import java.util.UUID;

public interface BookmarkService {
    List<BookmarkResponse> getUserBookmarks(UUID userId);
    void deleteBookmarksForBook(UUID bookId);
    BookmarkResponse addBookmark(Bookmark bookmark);
    BookmarkResponse getBookmark(UUID userId, UUID bookId);
    void deleteBookmark(UUID userId, UUID bookId);
    BookmarkResponse updatePageNumber(UUID userId, UUID bookId, UpdatePageNumberCommand command);
}
