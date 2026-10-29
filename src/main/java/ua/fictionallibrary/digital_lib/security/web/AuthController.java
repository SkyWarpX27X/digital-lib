package ua.fictionallibrary.digital_lib.security.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.fictionallibrary.digital_lib.security.AppUserDetails;
import ua.fictionallibrary.digital_lib.security.CurrentUserResponse;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Автентифікація", description = "Дані поточного користувача")
public class AuthController {

    @GetMapping("/me")
    @Operation(summary = "Отримати дані поточного користувача", description = "Повертає логін, ім'я та ролі власника токена")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Дані користувача успішно отримано"),
            @ApiResponse(responseCode = "401", description = "Користувач не автентифікований", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal AppUserDetails user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .toList();
        return ResponseEntity.ok(new CurrentUserResponse(user.getId(), user.getUsername(), user.getName(), roles));
    }
}