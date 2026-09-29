package ua.fictionallibrary.digital_lib.digitizedbook.implementations;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.author.Author;
import ua.fictionallibrary.digital_lib.author.AuthorService;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBook;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookRepository;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookService;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;

import java.util.List;
import java.util.UUID;

@Service
public class DigitizedBookServiceImpl implements DigitizedBookService {

    private final DigitizedBookRepository repository;
    private final ApplicationEventPublisher publisher;
    private final AuthorService authorService;

    public DigitizedBookServiceImpl(DigitizedBookRepository repository, ApplicationEventPublisher publisher, AuthorService authorService) {
        this.repository = repository;
        this.publisher = publisher;
        this.authorService = authorService;
    }

    @Override
    public DigitizedBookResponse getDigitizedBook(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Not found digitized book with id " + id));
    }

    @Override
    public List<DigitizedBookResponse> getAllDigitizedBooks() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public DigitizedBookResponse addDigitizedBook(DigitizedBookRequest request, UUID physicalBookId) {
        DigitizedBook book = toEntity(request);
        if (repository.existsById(book.getId()))
            throw new DuplicateException("Digitized book with id " + book.getId() + " already exists");
        DigitizedBook digitizedBook = repository.save(book);
        publisher.publishEvent(new BookDigitizedEvent(
                digitizedBook.getId(),
                physicalBookId
        ));
        return toResponse(digitizedBook);
    }

    @Override
    public DigitizedBookResponse updateDigitizedBook(UUID id, DigitizedBookRequest request) {
        if (!repository.existsById(id))
            throw new DataNotFoundException("Failed to update, not found digitized book with id " + id);
        DigitizedBook book = toEntity(request);
        return toResponse(repository.save(book));
    }

    @Override
    public void deleteDigitizedBook(UUID id) {
        if (!repository.existsById(id))
            throw new DataNotFoundException("Failed to delete, not found digitized book with id " + id);
        repository.deleteById(id);
    }

    @Override
    public boolean exists(UUID id) {
        return repository.existsById(id);
    }

    private DigitizedBook toEntity(DigitizedBookRequest request) {
        return new DigitizedBook(request.name(), authorService.findOrCreate(request.authors()), request.description(),
                request.topic(), request.publishingYear(), request.language(), request.resourceType(),
                request.isCopyrighted(), request.coverUrl(), request.fileUrl());
    }

    private DigitizedBookResponse toResponse(DigitizedBook entity) {
        List<String> authors = entity.getAuthors().stream().map(Author::getName).toList();
        return new DigitizedBookResponse(entity.getId(), entity.getName(), authors, entity.getDescription(),
                entity.getTopic(), entity.getPublishingYear(), entity.getLanguage(), entity.getResourceType(),
                entity.isCopyrighted(), entity.getCoverUrl(), entity.getFileUrl());
    }

}
