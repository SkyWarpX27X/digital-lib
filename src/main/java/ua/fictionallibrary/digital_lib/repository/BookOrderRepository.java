package ua.fictionallibrary.digital_lib.repository;

import ua.fictionallibrary.digital_lib.model.entity.BookOrderEntity;

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
