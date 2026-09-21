package ua.fictionallibrary.digital_lib.physicalbook.implementations;

import org.springframework.stereotype.Repository;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookRepository;
import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBookEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPhysicalBookRepository implements PhysicalBookRepository {
    private final Map<UUID, PhysicalBookEntity> physicalBooks;

    public InMemoryPhysicalBookRepository() {
        this.physicalBooks = new ConcurrentHashMap<>();
    }

    @Override
    public PhysicalBookEntity savePhysicalBook(PhysicalBookEntity physicalBook) {
        physicalBooks.put(physicalBook.id(), physicalBook);
        return physicalBook;
    }

    @Override
    public Optional<PhysicalBookEntity> getPhysicalBook(UUID id) {
        return Optional.ofNullable(physicalBooks.get(id));
    }

    @Override
    public List<PhysicalBookEntity> getAllPhysicalBooks() {
        return List.copyOf(physicalBooks.values());
    }

    @Override
    public void deletePhysicalBook(UUID id) {
        physicalBooks.remove(id);
    }

    @Override
    public boolean exists(UUID id) {
        return physicalBooks.containsKey(id);
    }
}