package ua.fictionallibrary.digital_lib.digitizedbook.implementations;

import org.springframework.stereotype.Repository;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookRepository;
import ua.fictionallibrary.digital_lib.digitizedbook.model.DigitizedBookEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryDigitizedBookRepository implements DigitizedBookRepository {
    private final Map<UUID, DigitizedBookEntity> digitizedBooks;

    public InMemoryDigitizedBookRepository() {
        this.digitizedBooks = new ConcurrentHashMap<>();
    }

    @Override
    public DigitizedBookEntity saveDigitizedBook(DigitizedBookEntity digitizedBook) {
        digitizedBooks.put(digitizedBook.id(), digitizedBook);
        return digitizedBook;
    }

    @Override
    public Optional<DigitizedBookEntity> getDigitizedBook(UUID id) {
        return Optional.ofNullable(digitizedBooks.get(id));
    }

    @Override
    public List<DigitizedBookEntity> getAllDigitizedBooks() {
        return List.copyOf(digitizedBooks.values());
    }

    @Override
    public void deleteDigitizedBook(UUID id) {
        digitizedBooks.remove(id);
    }

    @Override
    public boolean exists(UUID id) {
        return digitizedBooks.containsKey(id);
    }
}
