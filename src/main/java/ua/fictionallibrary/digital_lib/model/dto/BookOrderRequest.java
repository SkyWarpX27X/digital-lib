package ua.fictionallibrary.digital_lib.model.dto;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotNull;
import ua.fictionallibrary.digital_lib.common.validation.OnCreate;

import java.util.UUID;

public record BookOrderRequest(
        @NotNull(message = "User who placed order must be specified")
        UUID creatorId,
        String emailForBook,
        @NotNull(message = "Book must be specified")
        UUID book,
        @AssertFalse(groups = OnCreate.class, message = "Order cannot be created already finished")
        boolean isOpen
) {
}
