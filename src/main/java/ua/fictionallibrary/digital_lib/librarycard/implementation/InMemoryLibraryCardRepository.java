package ua.fictionallibrary.digital_lib.librarycard.implementation;

import org.springframework.stereotype.Repository;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardRepository;
import ua.fictionallibrary.digital_lib.librarycard.model.LibraryCardEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryLibraryCardRepository implements LibraryCardRepository {

    private final Map<UUID, LibraryCardEntity> libraryCards;

    public InMemoryLibraryCardRepository() {
        libraryCards = new ConcurrentHashMap<>();
    }

    @Override
    public LibraryCardEntity saveLibraryCard(LibraryCardEntity entity) {
        return null;
    }

    @Override
    public Optional<LibraryCardEntity> getLibraryCard(UUID userId) {
        return Optional.empty();
    }

    @Override
    public List<LibraryCardEntity> getAllLibraryCards() {
        return List.of();
    }

    @Override
    public void deleteLibraryCard(UUID userId) {

    }

    @Override
    public boolean exists(UUID userId) {
        return false;
    }
}
