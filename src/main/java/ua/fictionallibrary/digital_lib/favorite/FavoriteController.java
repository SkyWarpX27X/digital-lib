package ua.fictionallibrary.digital_lib.favorite;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.security.AppUserDetails;
import ua.fictionallibrary.digital_lib.user.LibraryCardService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "Улюблене", description = "Улюблені оцифровані книги поточного читача")
public class FavoriteController {
    private final FavoriteService service;
    private final LibraryCardService libraryCardService;

    public FavoriteController(FavoriteService service, LibraryCardService libraryCardService) {
        this.service = service;
        this.libraryCardService = libraryCardService;
    }

    @GetMapping
    @Operation(summary = "Отримати улюблені книги поточного читача")
    @ApiResponse(responseCode = "200", description = "Список улюблених книг успішно отримано")
    public ResponseEntity<List<DigitizedBookResponse>> getFavorites(@AuthenticationPrincipal AppUserDetails user) {
        return ResponseEntity.ok(service.getFavorites(user.getId(), libraryCardService.exists(user.getId())));
    }

    @PutMapping("/{bookId}")
    @Operation(summary = "Додати книгу в улюблене", description = "Повторне додавання нічого не змінює")
    @ApiResponse(responseCode = "204", description = "Книгу додано в улюблене")
    @ApiResponse(responseCode = "404", description = "Оцифровану книгу з вказаним ID не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> addFavorite(@PathVariable UUID bookId, @AuthenticationPrincipal AppUserDetails user) {
        service.addFavorite(user.getId(), bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{bookId}")
    @Operation(summary = "Прибрати книгу з улюбленого", description = "Якщо книги немає в улюбленому, нічого не змінюється")
    @ApiResponse(responseCode = "204", description = "Книгу прибрано з улюбленого")
    public ResponseEntity<Void> removeFavorite(@PathVariable UUID bookId, @AuthenticationPrincipal AppUserDetails user) {
        service.removeFavorite(user.getId(), bookId);
        return ResponseEntity.noContent().build();
    }
}
