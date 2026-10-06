package ua.fictionallibrary.digital_lib.digitizedbook;

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
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/digitized-books")
@Tag(name = "Оцифровані книги", description = "Керування створенням, редагуванням, видаленням та отриманням оцифрованих книг")
public class DigitizedBookController {
    private final DigitizedBookService service;

    public DigitizedBookController(DigitizedBookService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати оцифровану книгу за ID", description = "Повертає повні дані оцифрованої книги")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Оцифровану книгу знайдено"),
            @ApiResponse(responseCode = "404", description = "Оцифровану книгу з вказаним ID не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<DigitizedBookResponse> getBookById(@PathVariable UUID id) {
        DigitizedBookResponse book = service.getDigitizedBook(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping
    @Operation(summary = "Отримати список всіх оцифрованих книг")
    @ApiResponse(responseCode = "200", description = "Список книг успішно отримано")
    public ResponseEntity<List<DigitizedBookResponse>> getBooks() {
        return ResponseEntity.ok(service.getAllDigitizedBooks());
    }

    @GetMapping("/search")
    @Operation(summary = "Отримати список оцифрованих книг за назвою", description = "Повертає список усіх книг за параметром пошукового запиту назви (wildcard пошук)")
    @ApiResponse(responseCode = "200", description = "Список книг успішно отримано")
    public ResponseEntity<List<DigitizedBookResponse>> searchBooksByName(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(service.findDigitizedBooksByName(name));
    }

    @PostMapping
    @Operation(summary = "Створити нову оцифровану книгу", description = "Реєструє нову оцифровану книгу на основі існуючої фізичної книги")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Книгу успішно створено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    public ResponseEntity<DigitizedBookResponse> createBook(@Validated(OnCreate.class) @RequestBody DigitizedBookRequest request) {
        DigitizedBookResponse response = service.addDigitizedBook (request, request.physicalBookId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }



    @PutMapping("/{id}")
    @Operation(summary = "Оновити дані оцифрованої книги")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книгу успішно оновлено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Оцифровану книгу не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<DigitizedBookResponse> updateBook(@PathVariable UUID id, @Validated(OnUpdate.class)
    @RequestBody DigitizedBookRequest request) {
        DigitizedBookResponse response = service.updateDigitizedBook(id, request);
        return ResponseEntity.ok(response);
    }



    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити оцифровану книгу за ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Оцифровану книгу успішно видалено"),
            @ApiResponse(responseCode = "404", description = "Оцифровану книгу не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<DigitizedBookResponse> deleteBook(@PathVariable UUID id) {
        service.deleteDigitizedBook(id);
        return ResponseEntity.noContent().build();
    }
}
