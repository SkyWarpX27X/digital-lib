package ua.fictionallibrary.digital_lib.service;

import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.exception.InvalidOrderUpdateException;
import ua.fictionallibrary.digital_lib.model.dto.BookOrderResponse;
import ua.fictionallibrary.digital_lib.model.entity.BookOrderEntity;
import ua.fictionallibrary.digital_lib.repository.BookOrderRepository;

import java.util.List;
import java.util.UUID;

@Service
public class BookOrderServiceImpl implements BookOrderService {

    private final BookOrderRepository repository;

    public BookOrderServiceImpl(BookOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public BookOrderResponse addBookOrder(BookOrderEntity order) {
        //TODO Check if creator has library card when user and library card services are ready
        if (repository.existsById(order.id()))
            throw new DuplicateException("Order with id " + order.id() + " already exists");
        if (repository.existsByBook(order.book()))
            throw new DuplicateException("Book with id" + order.book() + " already ordered");
        return toResponse(repository.saveBookOrder(order));
    }

    @Override
    public List<BookOrderResponse> getBookOrders(boolean onlyOpen) {
        return repository.getBookOrders(onlyOpen).stream().map(this::toResponse).toList();
    }

    @Override
    public BookOrderResponse getBookOrder(UUID id) {
        return repository.getBookOrder(id)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Not found order with id " + id));
    }

    @Override
    public BookOrderResponse updateBookOrder(BookOrderEntity newValue) {
        BookOrderEntity oldValue = repository
                .getBookOrder(newValue.id())
                .orElseThrow(() -> new DataNotFoundException("Failed to update, not found order with id " + newValue.id()));
        if (!oldValue.creatorId().equals(newValue.creatorId()))
            throw new InvalidOrderUpdateException("Order creator cannot be changed");
        if (!oldValue.book().equals(newValue.book()))
            throw new InvalidOrderUpdateException("Ordered book cannot be changed");
        //TODO Add creators email if email is empty when user and library card services are ready
        return toResponse(repository.saveBookOrder(newValue));
    }

    private BookOrderResponse toResponse(BookOrderEntity entity) {
        return new BookOrderResponse(entity);
    }
}
