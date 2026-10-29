package ua.fictionallibrary.digital_lib.security;

import java.util.List;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String username,
        String name,
        List<String> roles
) {
    public CurrentUserResponse {
        roles = (roles != null) ? List.copyOf(roles) : List.of();
    }
}