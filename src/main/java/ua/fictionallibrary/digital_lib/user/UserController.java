package ua.fictionallibrary.digital_lib.user;

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
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Validated(OnCreate.class) @RequestBody UserRequest userRequest) {
        final var response = userService.addUser(toEntity(userRequest));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        final var response = userService.getUser(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        final var response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody UserRequest userRequest) {
        final var response = userService.updateUser(id, toEntity(userRequest));
        return ResponseEntity.ok(response);
    }
    private User toEntity(UserRequest request) {
        return new User(request.login(), request.password(), request.name(), request.surname(),
                request.patronymic(), request.role(), request.isActive());
    }
}
