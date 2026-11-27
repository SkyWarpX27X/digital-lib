package ua.fictionallibrary.digital_lib.user;

import ua.fictionallibrary.digital_lib.user.dto.LibraryCardOverviewResponse;
import ua.fictionallibrary.digital_lib.user.dto.LibraryCardRequest;
import ua.fictionallibrary.digital_lib.user.dto.LibraryCardResponse;

import java.util.List;
import java.util.UUID;

public interface LibraryCardService {
    LibraryCardResponse getLibraryCard(UUID userId);
    List<LibraryCardOverviewResponse> getAllLibraryCards();
    LibraryCardResponse addLibraryCard(UUID userId, LibraryCardRequest card);
    LibraryCardResponse updateLibraryCard(UUID userId, LibraryCardRequest card);
    String getEmail(UUID userId);
    void deleteLibraryCard(UUID userId);
    boolean exists(UUID userId);
}
