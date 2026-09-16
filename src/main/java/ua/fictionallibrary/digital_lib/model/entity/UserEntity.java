package ua.fictionallibrary.digital_lib.model.entity;

import ua.fictionallibrary.digital_lib.model.dto.UserRequest;
import ua.fictionallibrary.digital_lib.model.enums.UserRole;

import java.util.UUID;

public record UserEntity (
    UUID id,
    String login,
    String password,
    String name,
    String surname,
    String patronymic,
    UserRole role,
    boolean isActive
) {
    public UserEntity(UUID id, UserRequest request) {
        this(id, request.login(), request.password(), request.name(), request.surname(),
                request.patronymic(), request.role(), request.isActive());
    }
}
