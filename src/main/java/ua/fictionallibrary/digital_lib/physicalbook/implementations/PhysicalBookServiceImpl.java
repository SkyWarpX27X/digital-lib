package ua.fictionallibrary.digital_lib.physicalbook.implementations;

import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookRepository;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;
import ua.fictionallibrary.digital_lib.physicalbook.ResourceTypeValidationStrategy;
import ua.fictionallibrary.digital_lib.physicalbook.model.dto.PhysicalBookResponse;
import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBookEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PhysicalBookServiceImpl implements PhysicalBookService {

    private final PhysicalBookRepository repository;
    private final Map<String, ResourceTypeValidationStrategy> strategies;

    public PhysicalBookServiceImpl(PhysicalBookRepository repository, List<ResourceTypeValidationStrategy> strategies) {
        this.repository = repository;
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(ResourceTypeValidationStrategy::getResourceType, Function.identity()));
    }

    @Override
    public PhysicalBookResponse addPhysicalBook(PhysicalBookEntity book) {
        if (repository.exists(book.id()))
            throw new DuplicateException("Physical book with id " + book.id() + " already exists");
        ResourceTypeValidationStrategy strategy = strategies.get(book.resourceType());
        if (strategy == null)
            strategies.get("Книга").validateResourceType(book);
        else
            strategy.validateResourceType(book);
        return toResponse(repository.savePhysicalBook(book));
    }

    @Override
    public PhysicalBookResponse updatePhysicalBook(UUID id, PhysicalBookEntity book) {
        if (!repository.exists(id))
            throw new DataNotFoundException("Failed to update, not found physical book with id " + id);
        ResourceTypeValidationStrategy strategy = strategies.get(book.resourceType());
        if (strategy == null)
            strategies.get("Книга").validateResourceType(book);
        else
            strategy.validateResourceType(book);
        return toResponse(repository.savePhysicalBook(book));
    }

    @Override
    public PhysicalBookResponse getPhysicalBook(UUID id) {
        return repository.getPhysicalBook(id)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Not found physical book with id " + id));
    }

    @Override
    public List<PhysicalBookResponse> getAllPhysicalBooks() {
        return repository.getAllPhysicalBooks().stream().map(this::toResponse).toList();
    }

    @Override
    public void deletePhysicalBook(UUID id) {
        if (!repository.exists(id))
            throw new DataNotFoundException("Failed to delete, not found physical book with id " + id);
        repository.deletePhysicalBook(id);
    }

    private PhysicalBookResponse toResponse(PhysicalBookEntity entity) {
        return new PhysicalBookResponse(entity);
    }
}