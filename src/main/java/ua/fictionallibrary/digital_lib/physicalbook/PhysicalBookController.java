package ua.fictionallibrary.digital_lib.physicalbook;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.common.OnCreate;
import ua.fictionallibrary.digital_lib.common.OnUpdate;
import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBook;
import ua.fictionallibrary.digital_lib.physicalbook.model.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.physicalbook.model.dto.PhysicalBookResponse;
import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBookEntity;

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
        PhysicalBookResponse response = service.addPhysicalBook(toEntity(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhysicalBookResponse> updateBook(@PathVariable UUID id, @Validated(OnUpdate.class) PhysicalBookRequest request) {
        PhysicalBookResponse response = service.updatePhysicalBook(id, toEntity(request));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PhysicalBookResponse> deleteBook(@PathVariable UUID id) {
        service.deletePhysicalBook(id);
        return ResponseEntity.noContent().build();
    }

    private PhysicalBook toEntity(PhysicalBookRequest request) {
        return new PhysicalBook(request.name(), request.authors(), request.description(), request.topic(),
                request.publishingYear(), request.language(), request.resourceType());
    }
}