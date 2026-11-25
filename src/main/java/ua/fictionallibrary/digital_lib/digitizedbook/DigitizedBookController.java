package ua.fictionallibrary.digital_lib.digitizedbook;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.common.BookFilter;
import ua.fictionallibrary.digital_lib.common.OnCreate;
import ua.fictionallibrary.digital_lib.common.OnUpdate;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;

import ua.fictionallibrary.digital_lib.digitizedbook.dto.StoredFile;
import ua.fictionallibrary.digital_lib.security.AppUserDetails;
import ua.fictionallibrary.digital_lib.user.LibraryCardService;
import ua.fictionallibrary.digital_lib.user.UserRole;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/digitized-books")
@Tag(name = "Оцифровані книги", description = "Керування створенням, редагуванням, видаленням та отриманням оцифрованих книг")
public class DigitizedBookController {
    private final DigitizedBookService service;
    private final LibraryCardService libraryCardService;

    public DigitizedBookController(DigitizedBookService service, LibraryCardService libraryCardService) {
        this.service = service;
        this.libraryCardService = libraryCardService;
    }

    private boolean canReadCopyrighted(AppUserDetails user) {
        if (user == null) return false;
        boolean reader = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.READER.name()));
        return !reader || libraryCardService.exists(user.getId());
    }

    //TODO: add pdf/cover updates

    @GetMapping("/{id}")
    @Operation(summary = "Отримати оцифровану книгу за ID", description = "Повертає метадані книги. Посилання на PDF (pdfUrl) є лише тоді, " +
            "коли користувач може читати книгу (canRead): книга вільного доступу, або користувач — працівник чи читач із читацьким квитком")
    @ApiResponse(responseCode = "200", description = "Оцифровану книгу знайдено")
    @ApiResponse(responseCode = "404", description = "Оцифровану книгу з вказаним ID не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<DigitizedBookResponse> getBookById(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails user) {
        DigitizedBookResponse book = service.getDigitizedBook(id, canReadCopyrighted(user));
        return ResponseEntity.ok(book);
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Отримати PDF-файл оцифрованої книги", description = "Віддає PDF для перегляду в браузері")
    @ApiResponse(responseCode = "200", description = "PDF-файл книги", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE))
    @ApiResponse(responseCode = "401", description = "Книга під авторським правом: потрібно увійти", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Книга під авторським правом доступна лише читачам із читацьким квитком і працівникам", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Оцифровану книгу з вказаним ID не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<InputStreamResource> getPdf(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails user) {
        if (!service.getDigitizedBook(id, canReadCopyrighted(user)).canRead())
            throw new AccessDeniedException("Copyrighted book is available only to staff and readers with a library card");
        return inline(service.getPdf(id));
    }

    @GetMapping("/{id}/cover")
    @Operation(summary = "Отримати обкладинку оцифрованої книги", description = "Віддає зображення обкладинки (JPEG або PNG)")
    @ApiResponse(responseCode = "200", description = "Зображення обкладинки", content = @Content(mediaType = "image/*"))
    @ApiResponse(responseCode = "404", description = "Книгу не знайдено або в неї немає обкладинки", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<InputStreamResource> getCover(@PathVariable UUID id) {
        return inline(service.getCover(id));
    }

    private static ResponseEntity<InputStreamResource> inline(StoredFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().build().toString())
                .body(new InputStreamResource(file.data()));
    }

    @GetMapping
    @Operation(summary = "Отримати список оцифрованих книг",
            description = "Усі параметри необов'язкові. Без параметрів повертає всі книги")
    @ApiResponse(responseCode = "200", description = "Список книг успішно отримано")
    public ResponseEntity<List<DigitizedBookResponse>> getBooks(@ParameterObject BookFilter filter,
                                                                @AuthenticationPrincipal AppUserDetails user) {
        return ResponseEntity.ok(service.getAllDigitizedBooks(filter, canReadCopyrighted(user)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Створити нову оцифровану книгу", description = "Реєструє нову оцифровану книгу на основі існуючої фізичної книги")
    @ApiResponse(responseCode = "201", description = "Книгу успішно створено")
    @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<DigitizedBookResponse> createBook(
            @RequestPart("data") @Validated(OnCreate.class) DigitizedBookRequest data,
            @RequestPart("pdf") MultipartFile pdf,
            @RequestPart(value = "cover", required = false) MultipartFile cover) {
        DigitizedBookResponse response = service.addDigitizedBook (data, data.physicalBookId(), pdf, cover);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }


    @PutMapping("/{id}")
    @Operation(summary = "Оновити дані оцифрованої книги")
    @ApiResponse(responseCode = "200", description = "Книгу успішно оновлено")
    @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Оцифровану книгу не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<DigitizedBookResponse> updateBook(@PathVariable UUID id, @Validated(OnUpdate.class)
    @RequestBody DigitizedBookRequest request) {
        DigitizedBookResponse response = service.updateDigitizedBook(id, request);
        return ResponseEntity.ok(response);
    }



    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити оцифровану книгу за ID")
    @ApiResponse(responseCode = "204", description = "Оцифровану книгу успішно видалено")
    @ApiResponse(responseCode = "404", description = "Оцифровану книгу не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<DigitizedBookResponse> deleteBook(@PathVariable UUID id) {
        service.deleteDigitizedBook(id);
        return ResponseEntity.noContent().build();
    }
}
