package ua.fictionallibrary.digital_lib.security;

public record AuthResponse(
        String token,
        String type,
        long expiresInSeconds
) {
}
