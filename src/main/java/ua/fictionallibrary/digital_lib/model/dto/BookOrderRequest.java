package ua.fictionallibrary.digital_lib.model.dto;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotNull;
import ua.fictionallibrary.digital_lib.common.validation.OnCreate;

import java.util.UUID;

public record BookOrderRequest(
        // Email not yet validated due to the existence of a default option for no input (taken from creatorId).
        // Will be handled on service level later.
        String emailForBook,
        @NotNull(message = "Book must be specified")
        UUID book,
        @AssertFalse(groups = OnCreate.class, message = "Order cannot be created already finished")
        boolean isOpen
) {
}
