package ua.fictionallibrary.digital_lib.security;

import jakarta.validation.constraints.NotNull;

public record LoginUserRequest(
        @NotNull
        String username,
        @NotNull
        String password
) {
}
