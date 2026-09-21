package ua.fictionallibrary.digital_lib.bookorder;

import java.util.UUID;

public record BookOrderCreatedEvent(
    UUID id,
    UUID creatorId,
    String emailForDelivery,
    UUID book
) {
}
