package ua.fictionallibrary.digital_lib.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.common.validation.OnCreate;
import ua.fictionallibrary.digital_lib.common.validation.OnUpdate;
import ua.fictionallibrary.digital_lib.model.dto.*;
import ua.fictionallibrary.digital_lib.model.entity.UserEntity;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final Map<UUID, UserEntity> users;

    public UserController() {
        users = new HashMap<>();
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Validated(OnCreate.class) @RequestBody UserRequest userRequest) {
        UUID id = UUID.randomUUID();
        UserEntity entity = new UserEntity(id, userRequest);
        users.put(id, entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(new UserResponse(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserEntity entity = users.get(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new UserResponse(entity));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> userList = users.values().stream().map(UserResponse::new).toList();
        return ResponseEntity.ok(userList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody UserRequest userRequest) {
        if (!users.containsKey(id))
            throw new DataNotFoundException("Failed to update, not user found with id: " + id);
        UserEntity newValue = new UserEntity(id, userRequest);
        users.put(id, newValue);
        return ResponseEntity.ok(new UserResponse(newValue));
    }
}
