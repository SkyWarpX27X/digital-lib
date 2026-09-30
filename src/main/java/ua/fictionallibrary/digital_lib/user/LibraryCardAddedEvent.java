package ua.fictionallibrary.digital_lib.user;

import java.util.UUID;

public record LibraryCardAddedEvent(
        UUID userId,
        String email
) {
}
