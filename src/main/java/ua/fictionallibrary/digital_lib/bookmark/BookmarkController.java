package ua.fictionallibrary.digital_lib.bookmark;

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
import ua.fictionallibrary.digital_lib.bookmark.dto.BookmarkRequest;
import ua.fictionallibrary.digital_lib.bookmark.dto.BookmarkResponse;
import ua.fictionallibrary.digital_lib.common.OnCreate;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookmark")
@Tag(name = "Закладки", description = "Керування створенням, редагуванням та отриманням закладок (видалення не підтримується)")
public class BookmarkController {
    private final BookmarkService service;

    public BookmarkController(BookmarkService service){
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Створити нову закладку", description = "Створює нову закладку для користувача для вказаної книги")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Закладку успішно створено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Закладка в книзі вже була створена користувачем", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    public ResponseEntity<BookmarkResponse> createBookmark(@Validated(OnCreate.class) @RequestBody BookmarkRequest request){
        BookmarkResponse response = service.addBookmark(new Bookmark(request.userId(), request.bookId(), request.pageNumber()));
        UUID userId = request.userId();
        UUID bookId = request.bookId();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{userId}/{bookId}").buildAndExpand(userId, bookId).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{userId}/{bookId}")
    @Operation(summary = "Отримати закладку за ID користувача та книги")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Закладку знайдено"),
            @ApiResponse(responseCode = "404", description = "Закладку з вказаним ID користувача та книги не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<BookmarkResponse> getBookmark(@PathVariable UUID userId, @PathVariable UUID bookId){
        return ResponseEntity.ok(service.getBookmark(userId, bookId));
    }

    @PatchMapping("/{userId}/{bookId}")
    @Operation(summary = "Оновити сторінку закладки за ID користувача та книги")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Закладку оновлено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Закладку з вказаним ID користувача та книги не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    public ResponseEntity<BookmarkResponse> updatePageNumber(@Validated(OnCreate.class) @PathVariable UUID userId, @PathVariable UUID bookId,
                                                             @RequestBody UpdatePageNumberCommand command){
        BookmarkResponse response = service.updatePageNumber(userId, bookId, command);
        return ResponseEntity.ok(response);
    }
}
