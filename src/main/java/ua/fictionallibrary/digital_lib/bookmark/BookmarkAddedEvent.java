package ua.fictionallibrary.digital_lib.bookmark;

import java.util.UUID;

public record BookmarkAddedEvent (
        UUID userId,
        UUID bookId,
        int pageNumber
){

}
