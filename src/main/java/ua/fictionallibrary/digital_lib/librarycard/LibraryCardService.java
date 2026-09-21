package ua.fictionallibrary.digital_lib.librarycard;

import java.util.UUID;

public interface LibraryCardService {
    String getEmail(UUID ownerId);
    boolean exists(UUID ownerId);
}
