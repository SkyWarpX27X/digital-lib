package ua.fictionallibrary.digital_lib.bookmark.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


import java.util.UUID;

public record BookmarkRequest(
        @NotNull(message = "User who made the bookmark must be specified")
        UUID userId,
        @NotNull(message = "Book must be specified")
        UUID bookId,
        @Positive(message = "Page number must be positive")
        int pageNumber
) {
}
