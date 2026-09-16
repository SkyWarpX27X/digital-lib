package ua.fictionallibrary.digital_lib.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.common.DataNotFoundException;
import ua.fictionallibrary.digital_lib.common.validation.OnCreate;
import ua.fictionallibrary.digital_lib.common.validation.OnUpdate;
import ua.fictionallibrary.digital_lib.model.dto.BookOrderRequest;
import ua.fictionallibrary.digital_lib.model.dto.BookOrderResponse;
import ua.fictionallibrary.digital_lib.model.entity.BookOrderEntity;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/book-orders")
public class BookOrderController {

    private final Map<UUID, BookOrderEntity> bookOrders;

    public BookOrderController(){
        this.bookOrders = new HashMap<>();
    }

    @PostMapping
    public ResponseEntity<BookOrderResponse> createBookOrder(@Validated(OnCreate.class) @RequestBody BookOrderRequest request){
        UUID id = UUID.randomUUID();
        BookOrderEntity entity = new BookOrderEntity(id, request);
        bookOrders.put(id, entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<List<BookOrderResponse>> getBookOrders(@RequestParam(defaultValue = "true") boolean onlyOpen){
        List<BookOrderResponse> bookOrderResponses = bookOrders.values().stream()
                .filter(order -> !onlyOpen || order.isOpen()).map(BookOrderResponse::new).toList();
        return ResponseEntity.ok(bookOrderResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookOrderResponse> getBookOrder(@PathVariable UUID id){
        BookOrderEntity entity = bookOrders.get(id);
        if(entity == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new BookOrderResponse(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookOrderResponse> updateBookOrder(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody BookOrderRequest request){
        if(!bookOrders.containsKey(id))
            throw new DataNotFoundException("Failed to update, not found book order with id " + id);
        BookOrderEntity newValue = new BookOrderEntity(id, request);
        bookOrders.put(id, newValue);
        return ResponseEntity.ok(new BookOrderResponse(newValue));
    }
}
