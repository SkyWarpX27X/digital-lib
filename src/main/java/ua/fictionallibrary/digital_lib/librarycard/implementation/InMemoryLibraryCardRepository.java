package ua.fictionallibrary.digital_lib.librarycard.implementation;

import org.springframework.stereotype.Repository;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardRepository;
import ua.fictionallibrary.digital_lib.librarycard.model.LibraryCardEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryLibraryCardRepository implements LibraryCardRepository {

    private final Map<UUID, LibraryCardEntity> libraryCards;

    public InMemoryLibraryCardRepository() {
        libraryCards = new ConcurrentHashMap<>();
    }

    @Override
    public LibraryCardEntity saveLibraryCard(LibraryCardEntity entity) {
        libraryCards.put(entity.ownerId(), entity);
        return entity;
    }

    @Override
    public Optional<LibraryCardEntity> getLibraryCard(UUID userId) {
        return Optional.ofNullable(libraryCards.get(userId));
    }

    @Override
    public List<LibraryCardEntity> getAllLibraryCards() {
        return new ArrayList<>(libraryCards.values());
    }

    @Override
    public void deleteLibraryCard(UUID userId) {
        libraryCards.remove(userId);
    }

    @Override
    public boolean exists(UUID userId) {
        return libraryCards.containsKey(userId);
    }
}
