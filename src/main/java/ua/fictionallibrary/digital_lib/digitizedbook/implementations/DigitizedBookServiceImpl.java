package ua.fictionallibrary.digital_lib.digitizedbook.implementations;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookRepository;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookService;
import ua.fictionallibrary.digital_lib.digitizedbook.model.DigitizedBookEntity;
import ua.fictionallibrary.digital_lib.digitizedbook.model.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;

import java.util.List;
import java.util.UUID;

@Service
public class DigitizedBookServiceImpl implements DigitizedBookService {

    private final DigitizedBookRepository repository;
    private final ApplicationEventPublisher publisher;

    public DigitizedBookServiceImpl(DigitizedBookRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    public DigitizedBookResponse getDigitizedBook(UUID id) {
        return repository.getDigitizedBook(id)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Not found digitized book with id " + id));
    }

    @Override
    public List<DigitizedBookResponse> getAllDigitizedBooks() {
        return repository.getAllDigitizedBooks().stream().map(this::toResponse).toList();
    }

    @Override
    public DigitizedBookResponse addDigitizedBook(DigitizedBookEntity book, UUID physicalBookId) {
        if (repository.exists(book.id()))
            throw new DuplicateException("Digitized book with id " + book.id() + " already exists");
        DigitizedBookEntity digitizedBook = repository.saveDigitizedBook(book);
        publisher.publishEvent(new BookDigitizedEvent(
                digitizedBook.id(),
                physicalBookId
        ));
        return toResponse(digitizedBook);
    }

    @Override
    public DigitizedBookResponse updateDigitizedBook(UUID id, DigitizedBookEntity book) {
        if (!repository.exists(id))
            throw new DataNotFoundException("Failed to update, not found digitized book with id " + id);
        return toResponse(repository.saveDigitizedBook(book));
    }

    @Override
    public void deleteDigitizedBook(UUID id) {
        if (!repository.exists(id))
            throw new DataNotFoundException("Failed to delete, not found digitized book with id " + id);
        repository.deleteDigitizedBook(id);
    }

    @Override
    public boolean exists(UUID id) {
        return repository.exists(id);
    }

    private DigitizedBookResponse toResponse(DigitizedBookEntity entity) {
        return new DigitizedBookResponse(entity);
    }

}
