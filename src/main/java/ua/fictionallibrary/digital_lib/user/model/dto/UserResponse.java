package ua.fictionallibrary.digital_lib.user.model.dto;

import ua.fictionallibrary.digital_lib.user.model.UserEntity;
import ua.fictionallibrary.digital_lib.user.model.UserRole;

import java.util.UUID;

public record UserResponse (
    UUID id,
    String name,
    String surname,
    String patronymic,
    UserRole role,
    boolean isActive
) {
    public UserResponse(UserEntity user) {
        this(user.id(), user.name(), user.surname(), user.patronymic(),
                user.role(), user.isActive());
    }
}
