package ua.fictionallibrary.digital_lib.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import ua.fictionallibrary.digital_lib.common.validation.OnCreate;
import ua.fictionallibrary.digital_lib.common.validation.OnUpdate;

import java.time.Year;
import java.util.UUID;

public record LibraryCardRequest(
        @Null(groups = OnCreate.class, message = "ID must be null when creating")
        @NotNull(groups = OnUpdate.class, message = "ID must be specified when updating")
        UUID id,
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
