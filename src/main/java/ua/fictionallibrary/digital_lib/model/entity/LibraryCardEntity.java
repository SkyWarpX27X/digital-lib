package ua.fictionallibrary.digital_lib.model.entity;

import java.time.Year;
import java.util.UUID;

public record LibraryCardEntity(
        UUID ownerId,
        String email,
        String postcode,
        Year birthYear,
        String livingAddress,
        String workOrStudyAddress,
        String organisation,
        String workPosition
) {
}
