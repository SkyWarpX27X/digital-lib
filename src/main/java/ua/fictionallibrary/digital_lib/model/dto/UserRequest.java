package ua.fictionallibrary.digital_lib.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import ua.fictionallibrary.digital_lib.common.validation.OnCreate;
import ua.fictionallibrary.digital_lib.common.validation.OnUpdate;
import ua.fictionallibrary.digital_lib.model.enums.UserRole;

import java.util.UUID;

public record UserRequest(
        @Null(groups = OnCreate.class, message = "ID must be null when creating")
        @NotNull(groups = OnUpdate.class, message = "ID must be specified when updating")
        UUID id,

        @NotBlank(message = "Login must not be empty")
        String login,

        @NotBlank(message = "Password must not be empty")
        String password,

        @NotBlank(message = "Name must not be empty")
        String name,

        @NotBlank(message = "Surname must not be empty")
        String surname,

        String patronymic,

        @NotNull(message = "User role must not be empty")
        UserRole role,

        boolean isActive
) {

}
