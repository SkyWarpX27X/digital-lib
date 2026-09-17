package ua.fictionallibrary.digital_lib.service;

import ua.fictionallibrary.digital_lib.model.dto.BookOrderResponse;
import ua.fictionallibrary.digital_lib.model.entity.BookOrderEntity;

import java.util.List;
import java.util.UUID;

public interface BookOrderService {
    BookOrderResponse addBookOrder(BookOrderEntity bookOrderEntity);
    List<BookOrderResponse> getBookOrders(boolean onlyOpen);
    BookOrderResponse getBookOrder(UUID id);
    BookOrderResponse updateBookOrder(BookOrderEntity bookOrderEntity);
}
