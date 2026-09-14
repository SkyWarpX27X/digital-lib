package ua.fictionallibrary.digital_lib.model.entity;

import ua.fictionallibrary.digital_lib.model.dto.LibraryCardRequest;

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
    public LibraryCardEntity(UUID ownerId, LibraryCardRequest request) {
        this(ownerId, request.email(), request.postcode(), request.birthYear(), request.livingAddress(),
                request.workOrStudyAddress(), request.organisation(), request.workPosition());
    }
}
