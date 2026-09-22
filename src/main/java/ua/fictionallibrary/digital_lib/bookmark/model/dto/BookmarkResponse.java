package ua.fictionallibrary.digital_lib.bookmark.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ua.fictionallibrary.digital_lib.bookmark.model.BookmarkEntity;

import java.util.UUID;

public record BookmarkResponse(
        UUID userId,
        UUID bookId,
        int pageNumber
) {
    public BookmarkResponse(BookmarkEntity entity) {
        this(entity.userId(), entity.bookId(), entity.pageNumber());
    }
}
