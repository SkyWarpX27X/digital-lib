package ua.fictionallibrary.digital_lib.bookorder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ProblemDetail;
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
    @Operation(summary = "Створити нове замовлення", description = "Створити замовлення на основі картки читача та фізичної книги")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Замовлення успішно створено"),
            @ApiResponse(responseCode = "400", description = "Некоректні вхідні дані запиту", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Фізичну книгу не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Замовлення на книгу вже було створено", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "У користувача відсутня картка читача", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<BookOrderResponse> createBookOrder(@Validated(OnCreate.class) @RequestBody BookOrderRequest request){
        BookOrderResponse response = service.addBookOrder(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Отримати список всіх замовлень", description = "Отримати інформацію всіх замовлень з можливістю фільтрування за статусом відкритості")
    @ApiResponse(responseCode = "200", description = "Список замовлень успішно отримано")
    public ResponseEntity<List<BookOrderResponse>> getBookOrders(@RequestParam(defaultValue = "true") boolean onlyOpen){
        return ResponseEntity.ok(service.getBookOrders(onlyOpen));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати замовлення за ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Замовлення знайдено"),
            @ApiResponse(responseCode = "404", description = "Замовлення з вказаним ID не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<BookOrderResponse> getBookOrder(@PathVariable UUID id){
        return ResponseEntity.ok(service.getBookOrder(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Оновити статус замовлення за ID", description = "Оновлення статусу з відкритого на закрите (протилежне видає помилку)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус замовлення оновлено"),
            @ApiResponse(responseCode = "404", description = "Замовлення з вказаним ID не знайдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Нелегальний перехід статусу замовлення", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<BookOrderResponse> updateOrderStatus(@PathVariable UUID id, @RequestBody UpdateStatusCommand command){
        BookOrderResponse response = service.updateStatus(id, command);
        return ResponseEntity.ok(response);
    }
}
