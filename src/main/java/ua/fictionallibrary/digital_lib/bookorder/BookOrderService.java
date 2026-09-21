package ua.fictionallibrary.digital_lib.bookorder;

import ua.fictionallibrary.digital_lib.bookorder.model.dto.BookOrderResponse;
import ua.fictionallibrary.digital_lib.bookorder.model.BookOrderEntity;

import java.util.List;
import java.util.UUID;

public interface BookOrderService {
    BookOrderResponse addBookOrder(BookOrderEntity bookOrderEntity);
    List<BookOrderResponse> getBookOrders(boolean onlyOpen);
    BookOrderResponse getBookOrder(UUID id);
    BookOrderResponse updateStatus(UUID id, UpdateStatusCommand command);
}
