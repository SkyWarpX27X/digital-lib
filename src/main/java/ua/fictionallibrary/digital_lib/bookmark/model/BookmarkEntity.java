package ua.fictionallibrary.digital_lib.bookmark.model;

import ua.fictionallibrary.digital_lib.bookmark.model.dto.BookmarkRequest;
import java.util.UUID;

public record BookmarkEntity(
        UUID userId,
        UUID bookId,
        int pageNumber
) {
    public BookmarkEntity(BookmarkRequest request) {
        this(request.userId(), request.bookId(), request.pageNumber());
    }
}
