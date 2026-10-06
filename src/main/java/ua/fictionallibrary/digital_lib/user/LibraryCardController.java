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
import ua.fictionallibrary.digital_lib.user.dto.LibraryCardRequest;
import ua.fictionallibrary.digital_lib.user.dto.LibraryCardResponse;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/library-card")
@Tag(name = "Картки читачів", description = "Керування створенням, редагуванням та отриманням карток читачів (видалення не підтримується)")
public class LibraryCardController {

    private final LibraryCardService libraryCardService;

    public LibraryCardController(LibraryCardService libraryCardService) {
        this.libraryCardService = libraryCardService;
    }

    @PostMapping
    @Operation(summary = "Створити нову картку читача", description = "Створення новох картки читача на основі існуючого запису про користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Картку читача успішно створено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Картка читача вказаного користувача уже існує", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<LibraryCardResponse> createLibraryCard(@PathVariable UUID userId, @Validated(OnCreate.class) @RequestBody LibraryCardRequest libraryCardRequest) {
        LibraryCardResponse response = libraryCardService.addLibraryCard(userId, libraryCardRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand(userId).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Отримати картку читача за ID користувача", description = "Повертає повні не конфіденційні дані картки читача")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Картку читача знайдено"),
            @ApiResponse(responseCode = "404", description = "Користувача з вказаним ID не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<LibraryCardResponse> getLibraryCard(@PathVariable UUID userId) {
        LibraryCardResponse response = libraryCardService.getLibraryCard(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "Оновити дані картки читача за ID користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Картку читача успішно оновлено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<LibraryCardResponse> updateLibraryCard(@PathVariable UUID userId, @Validated(OnUpdate.class) @RequestBody LibraryCardRequest libraryCardRequest) {
        LibraryCardResponse response = libraryCardService.updateLibraryCard(userId, libraryCardRequest);
        return ResponseEntity.ok(response);
    }
}
