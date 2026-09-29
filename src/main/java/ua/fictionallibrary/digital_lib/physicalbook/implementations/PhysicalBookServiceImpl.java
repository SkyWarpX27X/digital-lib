package ua.fictionallibrary.digital_lib.physicalbook.implementations;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookAddedEvent;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookRepository;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;
import ua.fictionallibrary.digital_lib.physicalbook.ResourceTypeValidationStrategy;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBook;
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
    private final Map<String, ResourceTypeValidationStrategy> strategies;

    public PhysicalBookServiceImpl(PhysicalBookRepository repository, List<ResourceTypeValidationStrategy> strategies, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(ResourceTypeValidationStrategy::getResourceType, Function.identity()));
    }

    @Override
    public PhysicalBookResponse addPhysicalBook(PhysicalBook book) {
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
    public PhysicalBookResponse updatePhysicalBook(UUID id, PhysicalBook book) {
        if (!repository.existsById(id))
            throw new DataNotFoundException("Failed to update, not found physical book with id " + id);
        ResourceTypeValidationStrategy strategy = strategies.get(book.getResourceType());
        if (strategy == null)
            strategies.get("Книга").validateResourceType(book);
        else
            strategy.validateResourceType(book);
        return toResponse(repository.save(book));
    }

    @Override
    public PhysicalBookResponse getPhysicalBook(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Not found physical book with id " + id));
    }

    @Override
    public List<PhysicalBookResponse> getAllPhysicalBooks() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
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

    private PhysicalBookResponse toResponse(PhysicalBook entity) {
        return new PhysicalBookResponse(entity.getId(), entity.getName(), entity.getAuthors(), entity.getDescription(),
                entity.getTopic(), entity.getPublishingYear(), entity.getLanguage(), entity.getResourceType());
    }
}