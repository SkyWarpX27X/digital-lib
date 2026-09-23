package ua.fictionallibrary.digital_lib.librarycard;

import ua.fictionallibrary.digital_lib.librarycard.model.LibraryCardEntity;
import ua.fictionallibrary.digital_lib.librarycard.model.dto.LibraryCardResponse;

import java.util.UUID;

public interface LibraryCardService {
    LibraryCardResponse getLibraryCard(UUID userId);
    LibraryCardResponse addLibraryCard(UUID userId, LibraryCardEntity card);
    LibraryCardResponse updateLibraryCard(UUID userId, LibraryCardEntity card);
    String getEmail(UUID userId);
    void deleteLibraryCard(UUID userId);
    boolean exists(UUID userId);
}
