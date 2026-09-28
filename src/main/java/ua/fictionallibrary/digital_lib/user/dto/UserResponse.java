package ua.fictionallibrary.digital_lib.user.dto;

import ua.fictionallibrary.digital_lib.user.UserRole;

import java.util.UUID;

public record UserResponse (
    UUID id,
    String name,
    String surname,
    String patronymic,
    UserRole role,
    boolean isActive
) {
}
