package ua.fictionallibrary.digital_lib.librarycard;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.common.OnCreate;
import ua.fictionallibrary.digital_lib.common.OnUpdate;
import ua.fictionallibrary.digital_lib.librarycard.model.LibraryCardEntity;
import ua.fictionallibrary.digital_lib.librarycard.model.dto.LibraryCardRequest;
import ua.fictionallibrary.digital_lib.librarycard.model.dto.LibraryCardResponse;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/library-card")
public class LibraryCardController {

    private final LibraryCardService libraryCardService;

    public LibraryCardController(LibraryCardService libraryCardService) {
        this.libraryCardService = libraryCardService;
    }

    @PostMapping
    public ResponseEntity<LibraryCardResponse> createLibraryCard(@PathVariable UUID userId, @Validated(OnCreate.class) @RequestBody LibraryCardRequest libraryCardRequest) {
        LibraryCardResponse response = libraryCardService.addLibraryCard(userId, toEntity(userId, libraryCardRequest));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand(userId).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<LibraryCardResponse> getLibraryCard(@PathVariable UUID userId) {
        LibraryCardResponse response = libraryCardService.getLibraryCard(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<LibraryCardResponse> updateLibraryCard(@PathVariable UUID userId, @Validated(OnUpdate.class) @RequestBody LibraryCardRequest libraryCardRequest) {
        LibraryCardResponse response = libraryCardService.updateLibraryCard(userId, toEntity(userId, libraryCardRequest));
        return ResponseEntity.ok(response);
    }

    private LibraryCardEntity toEntity(UUID userId, LibraryCardRequest request) {
        return new LibraryCardEntity(userId, request);
    }
}
