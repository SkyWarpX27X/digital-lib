package ua.fictionallibrary.digital_lib.bookmark.dto;


import java.util.UUID;

public record BookmarkResponse(
        UUID userId,
        UUID bookId,
        int pageNumber
) {
}
