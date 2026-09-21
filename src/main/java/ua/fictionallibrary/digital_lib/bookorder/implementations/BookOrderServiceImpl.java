package ua.fictionallibrary.digital_lib.bookorder.implementations;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderCreatedEvent;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderRepository;
import ua.fictionallibrary.digital_lib.bookorder.BookOrderService;
import ua.fictionallibrary.digital_lib.bookorder.UpdateStatusCommand;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.exception.InvalidOrderUpdateException;
import ua.fictionallibrary.digital_lib.bookorder.model.dto.BookOrderResponse;
import ua.fictionallibrary.digital_lib.bookorder.model.BookOrderEntity;

import java.util.List;
import java.util.UUID;

@Service
public class BookOrderServiceImpl implements BookOrderService {

    private final BookOrderRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public BookOrderServiceImpl(BookOrderRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public BookOrderResponse addBookOrder(BookOrderEntity order) {
        //TODO Check if creator has library card and add creators email if email is empty when user and library card services are ready
        if (repository.existsById(order.id()))
            throw new DuplicateException("Order with id " + order.id() + " already exists");
        if (repository.existsByBook(order.book()))
            throw new DuplicateException("Book with id" + order.book() + " already ordered");
        BookOrderEntity savedOrder = repository.saveBookOrder(order);
        eventPublisher.publishEvent(new BookOrderCreatedEvent(
                savedOrder.id(),
                savedOrder.creatorId(),
                savedOrder.emailForDelivery(),
                savedOrder.book()
        ));
        return toResponse(savedOrder);
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
    public BookOrderResponse updateStatus(UUID id, UpdateStatusCommand command) {
        BookOrderEntity order = repository
                .getBookOrder(id)
                .orElseThrow(() -> new DataNotFoundException("Failed to update, not found order with id " + id));
        if (command.isOpen() && !order.isOpen())
            throw new InvalidOrderUpdateException("Closed order cannot be opened again");
        BookOrderEntity updatedOrder = new BookOrderEntity(order.id(), order.creatorId(), order.emailForDelivery(),
                order.book(), command.isOpen());
        repository.saveBookOrder(updatedOrder);
        return toResponse(updatedOrder);
    }

    private BookOrderResponse toResponse(BookOrderEntity order) {
        return new BookOrderResponse(order);
    }
}
