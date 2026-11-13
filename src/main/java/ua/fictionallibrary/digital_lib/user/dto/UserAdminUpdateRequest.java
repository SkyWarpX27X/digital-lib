package ua.fictionallibrary.digital_lib.user.dto;

import ua.fictionallibrary.digital_lib.user.UserRole;

public record UserAdminUpdateRequest(
        UserRole role,
        Boolean isActive
) {
}