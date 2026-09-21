package ua.fictionallibrary.digital_lib.bookorder;

import ua.fictionallibrary.digital_lib.bookorder.model.BookOrderEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookOrderRepository {
    BookOrderEntity saveBookOrder(BookOrderEntity bookOrderEntity);
    Optional<BookOrderEntity> getBookOrder(UUID id);
    List<BookOrderEntity> getBookOrders(boolean onlyOpen);
    boolean existsById(UUID id);
    boolean existsByBook(UUID bookId);
}
