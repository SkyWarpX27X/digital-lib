package ua.fictionallibrary.digital_lib.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.common.DataNotFoundException;
import ua.fictionallibrary.digital_lib.common.validation.OnCreate;
import ua.fictionallibrary.digital_lib.common.validation.OnUpdate;
import ua.fictionallibrary.digital_lib.model.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.model.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.model.entity.DigitizedBookEntity;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/digitized-books")
public class DigitizedBookController {
    private final Map<UUID, DigitizedBookEntity> digitizedBooks;

    public DigitizedBookController() {
        digitizedBooks = new HashMap<>();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitizedBookResponse> getBookById(@PathVariable UUID id) {
        DigitizedBookEntity book = digitizedBooks.get(id);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new DigitizedBookResponse(book));
    }

    @GetMapping
    public ResponseEntity<List<DigitizedBookResponse>> getBooks() {
        List<DigitizedBookResponse> books = digitizedBooks.values().stream().map(DigitizedBookResponse::new).toList();
        return ResponseEntity.ok(books);
    }

    @PostMapping
    public ResponseEntity<DigitizedBookResponse> createBook(@Validated(OnCreate.class) @RequestBody DigitizedBookRequest request) {
        UUID id = UUID.randomUUID();
        DigitizedBookEntity entity = new DigitizedBookEntity(id, request);
        digitizedBooks.put(id, entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(new DigitizedBookResponse(entity, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DigitizedBookResponse> updateBook(@PathVariable UUID id, @Validated(OnUpdate.class) DigitizedBookRequest request) {
        if (!digitizedBooks.containsKey(id))
            throw new DataNotFoundException("Failed to update, not found digitized book with id " + id);
        DigitizedBookEntity newValue = new DigitizedBookEntity(id, request);
        digitizedBooks.put(id, newValue);
        return ResponseEntity.ok(new DigitizedBookResponse(newValue));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DigitizedBookResponse> deleteBook(@PathVariable UUID id) {
        if (!digitizedBooks.containsKey(id))
            throw new DataNotFoundException("Failed to delete, not found digitized book with id " + id);
        digitizedBooks.remove(id);
        return ResponseEntity.noContent().build();
    }
}
