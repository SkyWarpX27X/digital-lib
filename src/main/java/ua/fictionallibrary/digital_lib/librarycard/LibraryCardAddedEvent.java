package ua.fictionallibrary.digital_lib.librarycard;

import java.util.UUID;

public record LibraryCardAddedEvent(
        UUID userId,
        String email
) {
}
