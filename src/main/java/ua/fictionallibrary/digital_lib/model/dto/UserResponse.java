package ua.fictionallibrary.digital_lib.model.dto;

import ua.fictionallibrary.digital_lib.model.entity.UserEntity;
import ua.fictionallibrary.digital_lib.model.enums.UserRole;

import java.util.UUID;

public record UserResponse (
    UUID id,
    String login,
    String name,
    String surname,
    String patronymic,
    UserRole role,
    boolean isActive
) {
    public UserResponse(UserEntity user) {
        this(user.id(), user.login(), user.name(), user.surname(), user.patronymic(),
                user.role(), user.isActive());
    }
}
