package ua.fictionallibrary.digital_lib.bookorder;

import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderRequest;
import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderResponse;

import java.util.List;
import java.util.UUID;

public interface BookOrderService {
    BookOrderResponse addBookOrder(BookOrderRequest request, UUID creatorId);
    List<BookOrderResponse> getBookOrders(boolean onlyOpen);
    List<BookOrderResponse> getUserBookOrders(UUID userId);
    BookOrderResponse getBookOrder(UUID id);
    BookOrderResponse updateStatus(UUID id, UpdateStatusCommand command);
}
