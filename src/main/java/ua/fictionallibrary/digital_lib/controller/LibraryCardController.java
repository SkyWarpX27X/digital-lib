package ua.fictionallibrary.digital_lib.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.common.DataNotFoundException;
import ua.fictionallibrary.digital_lib.common.validation.OnCreate;
import ua.fictionallibrary.digital_lib.common.validation.OnUpdate;
import ua.fictionallibrary.digital_lib.model.dto.LibraryCardRequest;
import ua.fictionallibrary.digital_lib.model.dto.LibraryCardResponse;
import ua.fictionallibrary.digital_lib.model.entity.LibraryCardEntity;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/library-card")
public class LibraryCardController {

    private final Map<UUID, LibraryCardEntity> libraryCards;

    public LibraryCardController() {
        libraryCards = new HashMap<>();
    }

    @PostMapping
    public ResponseEntity<LibraryCardResponse> createLibraryCard(@PathVariable String userId, @Validated(OnCreate.class) @RequestBody LibraryCardRequest libraryCardRequest) {
        LibraryCardEntity entity = new LibraryCardEntity(UUID.fromString(userId), libraryCardRequest);
        libraryCards.put(UUID.fromString(userId), entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand(userId).toUri();
        return ResponseEntity.created(location).body(new LibraryCardResponse(entity));
    }

    @GetMapping
    public ResponseEntity<LibraryCardResponse> getLibraryCard(@PathVariable String userId) {
        LibraryCardEntity entity = libraryCards.get(UUID.fromString(userId));
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new LibraryCardResponse(entity));
    }

    @PutMapping
    public ResponseEntity<LibraryCardResponse> updateLibraryCard(@PathVariable String userId, @Validated(OnUpdate.class) @RequestBody LibraryCardRequest libraryCardRequest) {
        if (!libraryCards.containsKey(UUID.fromString(userId)))
            throw new DataNotFoundException("Failed to update, not found library card of user with id: " + userId);
        LibraryCardEntity newValue = new LibraryCardEntity(UUID.fromString(userId), libraryCardRequest);
        libraryCards.put(UUID.fromString(userId), newValue);
        return ResponseEntity.ok(new LibraryCardResponse(newValue));
    }
}
