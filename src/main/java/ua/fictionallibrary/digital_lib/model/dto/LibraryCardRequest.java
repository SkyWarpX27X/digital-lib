package ua.fictionallibrary.digital_lib.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Year;

public record LibraryCardRequest(
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email must not be empty")
        String email,
        @NotBlank(message = "Postcode must not be empty")
        String postcode,
        @NotBlank(message = "Birth year must be specified")
        Year birthYear,
        @NotBlank(message = "Living address must not be empty")
        String livingAddress,
        String workOrStudyAddress,
        String organisation,
        String workPosition
) {
}
