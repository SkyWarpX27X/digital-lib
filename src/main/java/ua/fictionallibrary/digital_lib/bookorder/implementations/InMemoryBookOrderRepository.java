package ua.fictionallibrary.digital_lib.bookorder.implementations;

import org.springframework.stereotype.Repository;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderRepository;
import ua.fictionallibrary.digital_lib.bookorder.model.BookOrderEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBookOrderRepository implements BookOrderRepository {

    private final Map<UUID, BookOrderEntity> bookOrders = new ConcurrentHashMap<>();

    @Override
    public BookOrderEntity saveBookOrder(BookOrderEntity order) {
        bookOrders.put(order.id(), order);
        return order;
    }

    @Override
    public Optional<BookOrderEntity> getBookOrder(UUID id) {
        return Optional.ofNullable(bookOrders.get(id));
    }

    @Override
    public List<BookOrderEntity> getBookOrders(boolean onlyOpen) {
        return bookOrders.values().stream()
                .filter(order -> !onlyOpen || order.isOpen()).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return bookOrders.containsKey(id);
    }

    @Override
    public boolean existsByBook(UUID bookId) {
        return bookOrders.values().stream().anyMatch(order -> order.book().equals(bookId));
    }
}
