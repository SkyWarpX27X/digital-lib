package ua.fictionallibrary.digital_lib.digitizedbook;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.common.OnCreate;
import ua.fictionallibrary.digital_lib.common.OnUpdate;
import ua.fictionallibrary.digital_lib.digitizedbook.model.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.model.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.digitizedbook.model.DigitizedBookEntity;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;
import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBookEntity;
import ua.fictionallibrary.digital_lib.physicalbook.model.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.physicalbook.model.dto.PhysicalBookResponse;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @PostMapping
    public ResponseEntity<DigitizedBookResponse> createBook(@Validated(OnCreate.class) @RequestBody DigitizedBookRequest request) {
        UUID id = UUID.randomUUID();
        DigitizedBookResponse response = service.addDigitizedBook(new DigitizedBookEntity(id, request), request.physicalBookId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DigitizedBookResponse> updateBook(@PathVariable UUID id, @Validated(OnUpdate.class) DigitizedBookRequest request) {
        DigitizedBookResponse response = service.updateDigitizedBook(id, new DigitizedBookEntity(id, request));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DigitizedBookResponse> deleteBook(@PathVariable UUID id) {
        service.deleteDigitizedBook(id);
        return ResponseEntity.noContent().build();
    }
}
