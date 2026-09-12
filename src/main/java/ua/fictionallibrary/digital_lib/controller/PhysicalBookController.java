package ua.fictionallibrary.digital_lib.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.common.DataNotFoundException;
import ua.fictionallibrary.digital_lib.common.validation.OnCreate;
import ua.fictionallibrary.digital_lib.common.validation.OnUpdate;
import ua.fictionallibrary.digital_lib.model.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.model.dto.PhysicalBookResponse;
import ua.fictionallibrary.digital_lib.model.entity.PhysicalBookEntity;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/physical-books")
public class PhysicalBookController {
    private Map<UUID, PhysicalBookEntity> physicalBooks;

    public PhysicalBookController() {
        physicalBooks = new HashMap<>();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhysicalBookResponse> getBookById(@PathVariable UUID id) {
        PhysicalBookEntity book = physicalBooks.get(id);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new PhysicalBookResponse(book));
    }

    @GetMapping
    public ResponseEntity<List<PhysicalBookResponse>> getBooks() {
        List<PhysicalBookResponse> books = physicalBooks.values().stream().map(PhysicalBookResponse::new).toList();
        return ResponseEntity.ok(books);
    }

    @PostMapping
    public ResponseEntity<PhysicalBookResponse> createBook(@Validated(OnCreate.class) @RequestBody PhysicalBookRequest request) {
        UUID id = UUID.randomUUID();
        PhysicalBookEntity entity = new PhysicalBookEntity(request);
        physicalBooks.put(id, entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(new PhysicalBookResponse(entity, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhysicalBookResponse> updateBook(@PathVariable UUID id, @Validated(OnUpdate.class) PhysicalBookRequest request) {
        if (!physicalBooks.containsKey(id))
            throw new DataNotFoundException("Failed to update, not found physical book with id " + id);
        PhysicalBookEntity newValue = new PhysicalBookEntity(request);
        physicalBooks.put(id, newValue);
        return ResponseEntity.ok(new PhysicalBookResponse(newValue));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PhysicalBookResponse> deleteBook(@PathVariable UUID id) {
        if (!physicalBooks.containsKey(id))
            throw new DataNotFoundException("Failed to delete, not found physical book with id " + id);
        physicalBooks.remove(id);
        return ResponseEntity.noContent().build();
    }
}