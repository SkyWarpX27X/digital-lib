package ua.fictionallibrary.digital_lib.bookorder;

import ua.fictionallibrary.digital_lib.bookorder.dto.BookOrderResponse;

import java.util.List;
import java.util.UUID;

public interface BookOrderService {
    BookOrderResponse addBookOrder(BookOrder order);
    List<BookOrderResponse> getBookOrders(boolean onlyOpen);
    BookOrderResponse getBookOrder(UUID id);
    BookOrderResponse updateStatus(UUID id, UpdateStatusCommand command);
}
