package ua.fictionallibrary.digital_lib.librarycard.implementation;

import org.springframework.stereotype.Repository;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardRepository;
import ua.fictionallibrary.digital_lib.librarycard.model.LibraryCardEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryLibraryCardRepository implements LibraryCardRepository {

    private final Map<UUID, LibraryCardEntity> libraryCards;

    public InMemoryLibraryCardRepository() {
        libraryCards = new ConcurrentHashMap<>();
    }
}
