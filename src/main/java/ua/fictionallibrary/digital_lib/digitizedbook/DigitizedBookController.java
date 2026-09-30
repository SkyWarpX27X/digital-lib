package ua.fictionallibrary.digital_lib.digitizedbook;

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
public class DigitizedBookController {
    private final DigitizedBookService service;

    public DigitizedBookController(DigitizedBookService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitizedBookResponse> getBookById(@PathVariable UUID id) {
        DigitizedBookResponse book = service.getDigitizedBook(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping
    public ResponseEntity<List<DigitizedBookResponse>> getBooks() {
        return ResponseEntity.ok(service.getAllDigitizedBooks());
    }

    @GetMapping("/search")
    public ResponseEntity<List<DigitizedBookResponse>> searchBooksByName(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(service.findDigitizedBooksByName(name));
    }

    @PostMapping
    public ResponseEntity<DigitizedBookResponse> createBook(@Validated(OnCreate.class) @RequestBody DigitizedBookRequest request) {
        DigitizedBookResponse response = service.addDigitizedBook (request, request.physicalBookId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DigitizedBookResponse> updateBook(@PathVariable UUID id, @Validated(OnUpdate.class)
    @RequestBody DigitizedBookRequest request) {
        DigitizedBookResponse response = service.updateDigitizedBook(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DigitizedBookResponse> deleteBook(@PathVariable UUID id) {
        service.deleteDigitizedBook(id);
        return ResponseEntity.noContent().build();
    }
}
