package ua.fictionallibrary.digital_lib.user.model;

import ua.fictionallibrary.digital_lib.user.model.dto.UserRequest;

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
