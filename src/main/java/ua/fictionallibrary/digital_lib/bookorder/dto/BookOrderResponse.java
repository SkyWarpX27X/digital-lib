package ua.fictionallibrary.digital_lib.bookorder.dto;

import java.util.UUID;

public record BookOrderResponse(
        UUID id,
        UUID creatorId,
        String emailForDelivery,
        UUID book,
        boolean isOpen
) {
}
