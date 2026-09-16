package ua.fictionallibrary.digital_lib.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ua.fictionallibrary.digital_lib.model.enums.UserRole;


public record UserRequest(
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
