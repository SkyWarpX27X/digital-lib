package ua.fictionallibrary.digital_lib.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.common.OnCreate;
import ua.fictionallibrary.digital_lib.common.OnUpdate;
import ua.fictionallibrary.digital_lib.user.dto.UserRequest;
import ua.fictionallibrary.digital_lib.user.dto.UserResponse;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Користувачі", description = "Керування створенням, редагуванням та отриманням користувачів (видалення не підтримується)")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Створити нового користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Користувача успішно створено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Користувач з вказаним логіном уже існує", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponse> createUser(@Validated(OnCreate.class) @RequestBody UserRequest userRequest) {
        final var response = userService.addUser(toEntity(userRequest));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати користувача за ID", description = "Повертає повні не конфіденційні дані користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Користувача знайдено"),
            @ApiResponse(responseCode = "404", description = "Користувача з вказаним ID не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        final var response = userService.getUser(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Отримати список всіх користувачів")
    @ApiResponse(responseCode = "200", description = "Список книг успішно отримано")
    public ResponseEntity<List<UserResponse>> getUsers() {
        final var response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити дані користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Користувача успішно оновлено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody UserRequest userRequest) {
        final var response = userService.updateUser(id, toEntity(userRequest));
        return ResponseEntity.ok(response);
    }
    private User toEntity(UserRequest request) {
        return new User(request.login(), request.password(), request.name(), request.surname(),
                request.patronymic(), request.role(), request.isActive());
    }
}
