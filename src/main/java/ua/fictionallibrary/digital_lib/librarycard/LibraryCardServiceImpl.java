package ua.fictionallibrary.digital_lib.librarycard;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LibraryCardServiceImpl implements LibraryCardService {
    @Override
    public String getEmail(UUID ownerId) {
        return "";
    }

    @Override
    public boolean exists(UUID ownerId) {
        return false;
    }
}
