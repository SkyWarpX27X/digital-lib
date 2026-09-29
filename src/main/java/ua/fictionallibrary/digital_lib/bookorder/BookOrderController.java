package ua.fictionallibrary.digital_lib.bookorder;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.common.OnCreate;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderRequest;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderResponse;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/book-orders")
public class BookOrderController {
    private final BookOrderService service;

    public BookOrderController(BookOrderService service){
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BookOrderResponse> createBookOrder(@Validated(OnCreate.class) @RequestBody BookOrderRequest request){
        BookOrderResponse response = service.addBookOrder(toEntity(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BookOrderResponse>> getBookOrders(@RequestParam(defaultValue = "true") boolean onlyOpen){
        return ResponseEntity.ok(service.getBookOrders(onlyOpen));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookOrderResponse> getBookOrder(@PathVariable UUID id){
        return ResponseEntity.ok(service.getBookOrder(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookOrderResponse> updateOrderStatus(@PathVariable UUID id, @RequestBody UpdateStatusCommand command){
        BookOrderResponse response = service.updateStatus(id, command);
        return ResponseEntity.ok(response);
    }

    private BookOrder toEntity(BookOrderRequest request){
        return new BookOrder(request.creatorId(), request.emailForDelivery(), request.book(), request.isOpen());
    }
}
