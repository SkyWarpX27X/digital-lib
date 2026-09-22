package ua.fictionallibrary.digital_lib.bookorder.model.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import ua.fictionallibrary.digital_lib.common.OnCreate;

import java.util.UUID;

public record BookOrderRequest(
        @NotNull(message = "User who placed order must be specified")
        UUID creatorId,
        // Email not yet validated due to the existence of a default option for no input (taken from creatorId).
        // Will be handled on service level later.
        String emailForDelivery,
        @NotNull(message = "Book must be specified")
        UUID book,
        @AssertTrue(groups = OnCreate.class, message = "Order cannot be created already finished")
        boolean isOpen
) {
}
