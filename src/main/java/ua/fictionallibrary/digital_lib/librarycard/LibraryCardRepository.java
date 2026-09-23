package ua.fictionallibrary.digital_lib.librarycard;

import ua.fictionallibrary.digital_lib.librarycard.model.LibraryCardEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LibraryCardRepository {

    LibraryCardEntity saveLibraryCard(LibraryCardEntity entity);
    Optional<LibraryCardEntity> getLibraryCard(UUID userId);
    void deleteLibraryCard(UUID userId);
    boolean exists(UUID userId);
}
