package ua.fictionallibrary.digital_lib.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Year;
import java.util.UUID;

public record LibraryCardResponse(
        UUID ownerId,
        String email,
        String postcode,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy")
        Year birthYear,
        String livingAddress,
        String workOrStudyAddress,
        String organisation,
        String workPosition
) {
}
