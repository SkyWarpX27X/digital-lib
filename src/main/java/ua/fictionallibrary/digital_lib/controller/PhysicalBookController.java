package ua.fictionallibrary.digital_lib.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.common.validation.OnCreate;
import ua.fictionallibrary.digital_lib.common.validation.OnUpdate;
import ua.fictionallibrary.digital_lib.model.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.model.dto.PhysicalBookResponse;
import ua.fictionallibrary.digital_lib.model.entity.PhysicalBookEntity;
import ua.fictionallibrary.digital_lib.service.PhysicalBookService;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/physical-books")
public class PhysicalBookController {
    private final PhysicalBookService service;

    public PhysicalBookController(PhysicalBookService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhysicalBookResponse> getBookById(@PathVariable UUID id) {
        PhysicalBookResponse book = service.getPhysicalBook(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping
    public ResponseEntity<List<PhysicalBookResponse>> getBooks() {
        return ResponseEntity.ok(service.getAllPhysicalBooks());
    }

    @PostMapping
    public ResponseEntity<PhysicalBookResponse> createBook(@Validated(OnCreate.class) @RequestBody PhysicalBookRequest request) {
        UUID id = UUID.randomUUID();
        PhysicalBookResponse response = service.addPhysicalBook(new PhysicalBookEntity(id, request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhysicalBookResponse> updateBook(@PathVariable UUID id, @Validated(OnUpdate.class) PhysicalBookRequest request) {
        PhysicalBookResponse response = service.updatePhysicalBook(id, new PhysicalBookEntity(id, request));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PhysicalBookResponse> deleteBook(@PathVariable UUID id) {
        service.deletePhysicalBook(id);
        return ResponseEntity.noContent().build();
    }
}