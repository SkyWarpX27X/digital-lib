package ua.fictionallibrary.digital_lib.user;

import java.util.UUID;

public record UserAddedEvent(
        UUID id,
        String name,
        String surname,
        String patronymic
) {
}
