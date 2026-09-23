package ua.fictionallibrary.digital_lib.librarycard;

import ua.fictionallibrary.digital_lib.librarycard.model.dto.LibraryCardRequest;
import ua.fictionallibrary.digital_lib.librarycard.model.dto.LibraryCardResponse;

import java.util.List;
import java.util.UUID;

public interface LibraryCardService {
    LibraryCardResponse getLibraryCard(UUID userId);
    List<LibraryCardResponse> getAllLibraryCards();
    LibraryCardResponse addDigitalCard(UUID userId, LibraryCardRequest card);
    LibraryCardResponse updateDigitalCard(UUID userId, LibraryCardRequest card);
    String getEmail(UUID userId);
    void deleteLibraryCard(UUID userId);
    boolean exists(UUID userId);
}
