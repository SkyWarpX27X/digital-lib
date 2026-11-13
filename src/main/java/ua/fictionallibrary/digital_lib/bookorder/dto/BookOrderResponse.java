package ua.fictionallibrary.digital_lib.bookorder.dto;

import java.util.UUID;

public record BookOrderResponse(
        UUID id,
        UUID creatorId,
        String creatorName,
        String emailForDelivery,
        UUID book,
        String bookName,
        String bookAuthors,
        boolean isOpen
) {
}
