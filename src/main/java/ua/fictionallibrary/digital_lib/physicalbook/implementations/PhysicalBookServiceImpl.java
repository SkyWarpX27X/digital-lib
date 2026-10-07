package ua.fictionallibrary.digital_lib.physicalbook.implementations;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.fictionallibrary.digital_lib.author.Author;
import ua.fictionallibrary.digital_lib.author.AuthorService;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.starter.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookAddedEvent;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookRepository;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;
import ua.fictionallibrary.digital_lib.physicalbook.ResourceTypeValidationStrategy;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBook;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PhysicalBookServiceImpl implements PhysicalBookService {

    private final PhysicalBookRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthorService authorService;
    private final Map<String, ResourceTypeValidationStrategy> strategies;

    public PhysicalBookServiceImpl(PhysicalBookRepository repository, List<ResourceTypeValidationStrategy> strategies, ApplicationEventPublisher eventPublisher, AuthorService authorService) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(ResourceTypeValidationStrategy::getResourceType, Function.identity()));
        this.authorService = authorService;
    }

    @Override
    @Transactional
    public PhysicalBookResponse addPhysicalBook(PhysicalBookRequest request) {
        PhysicalBook book = toEntity(request);
        ResourceTypeValidationStrategy strategy = strategies.get(book.getResourceType());
        if (strategy == null)
            strategies.get("Книга").validateResourceType(book);
        else
            strategy.validateResourceType(book);
        PhysicalBook savedBook = repository.save(book);
        eventPublisher.publishEvent(new PhysicalBookAddedEvent(
                savedBook.getId(),
                savedBook.getName(),
                savedBook.getResourceType()
        ));
        return toResponse(savedBook);
    }

    @Override
    @Transactional
    public PhysicalBookResponse updatePhysicalBook(UUID id, PhysicalBookRequest request) {
        PhysicalBook book = repository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Failed to update, not found physical book with id " + id));
        book.setName(request.name());
        book.setAuthors(authorService.findOrCreate(request.authors()));
        book.setDescription(request.description());
        book.setTopic(request.topic());
        book.setPublishingYear(request.publishingYear());
        book.setLanguage(request.language());
        book.setResourceType(request.resourceType());
        validateResourceType(book);
        return toResponse(repository.save(book));
    }

    @Override
    @Transactional(readOnly = true)
    public PhysicalBookResponse getPhysicalBook(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Not found physical book with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhysicalBookResponse> getAllPhysicalBooks() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deletePhysicalBook(UUID id) {
        if (!repository.existsById(id))
            throw new DataNotFoundException("Failed to delete, not found physical book with id " + id);
        repository.deleteById(id);
    }

    @Override
    public boolean exists(UUID id) {
        return repository.existsById(id);
    }

    @ApplicationModuleListener
    public void onBookDigitized(BookDigitizedEvent event) {
        deletePhysicalBook(event.physicalId());
    }

    private PhysicalBook toEntity(PhysicalBookRequest request) {
        return new PhysicalBook(request.name(), authorService.findOrCreate(request.authors()), request.description(),
                request.topic(), request.publishingYear(), request.language(), request.resourceType());
    }

    private PhysicalBookResponse toResponse(PhysicalBook entity) {
        List<String> authors = entity.getAuthors().stream().map(Author::getName).toList();
        return new PhysicalBookResponse(entity.getId(), entity.getName(), authors, entity.getDescription(),
                entity.getTopic(), entity.getPublishingYear(), entity.getLanguage(), entity.getResourceType());
    }

    private void validateResourceType(PhysicalBook book) {
        ResourceTypeValidationStrategy strategy = strategies.get(book.getResourceType());
        if (strategy == null)
            strategies.get("Книга").validateResourceType(book);
        else
            strategy.validateResourceType(book);
    }
}