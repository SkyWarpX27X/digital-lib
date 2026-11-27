package ua.fictionallibrary.digital_lib.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Year;
import java.util.UUID;

public record LibraryCardOverviewResponse(
        UUID ownerId,
        String surname,
        String name,
        String patronymic,
        String email,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy")
        Year birthYear,
        String livingAddress,
        String workOrStudyAddress
) {
}
