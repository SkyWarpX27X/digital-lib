package ua.fictionallibrary.digital_lib.physicalbook;

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
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookResponse;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/physical-books")
@Tag(name = "Фізичні книги", description = "Керування створенням, редагуванням, видаленням та отриманням фізичних книг")
public class PhysicalBookController {
    private final PhysicalBookService service;

    public PhysicalBookController(PhysicalBookService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати фізичну книгу за ID", description = "Повертає повні дані фізичної книги")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Фізичну книгу знайдено"),
            @ApiResponse(responseCode = "404", description = "Фізичну книгу з вказаним ID не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PhysicalBookResponse> getBookById(@PathVariable UUID id) {
        PhysicalBookResponse book = service.getPhysicalBook(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping
    @Operation(summary = "Отримати список всіх фізичних книг")
    @ApiResponse(responseCode = "200", description = "Список книг успішно отримано")
    public ResponseEntity<List<PhysicalBookResponse>> getBooks() {
        return ResponseEntity.ok(service.getAllPhysicalBooks());
    }

    @PostMapping
    @Operation(summary = "Створити нову фізичну книгу")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Книгу успішно створено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    public ResponseEntity<PhysicalBookResponse> createBook(@Validated(OnCreate.class) @RequestBody PhysicalBookRequest request) {
        PhysicalBookResponse response = service.addPhysicalBook(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити дані фізичної книги")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Книгу успішно створено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Фізичну книгу не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PhysicalBookResponse> updateBook(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody PhysicalBookRequest request) {
        PhysicalBookResponse response = service.updatePhysicalBook(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити фізичну книгу за ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Фізичну книгу успішно видалено"),
            @ApiResponse(responseCode = "404", description = "Фізичну книгу не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PhysicalBookResponse> deleteBook(@PathVariable UUID id) {
        service.deletePhysicalBook(id);
        return ResponseEntity.noContent().build();
    }

}