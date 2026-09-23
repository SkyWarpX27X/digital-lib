package ua.fictionallibrary.digital_lib.librarycard.implementation;

import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardRepository;
import ua.fictionallibrary.digital_lib.librarycard.LibraryCardService;
import ua.fictionallibrary.digital_lib.librarycard.model.LibraryCardEntity;
import ua.fictionallibrary.digital_lib.librarycard.model.dto.LibraryCardRequest;
import ua.fictionallibrary.digital_lib.librarycard.model.dto.LibraryCardResponse;
import ua.fictionallibrary.digital_lib.user.UserService;

import java.util.UUID;

@Service
public class LibraryCardServiceImpl implements LibraryCardService {

    private final LibraryCardRepository libraryCardRepository;
    private final UserService userService;

    public LibraryCardServiceImpl(LibraryCardRepository repository, UserService userService) {
        this.libraryCardRepository = repository;
        this.userService = userService;
    }

    @Override
    public LibraryCardResponse getLibraryCard(UUID userId) {
        return libraryCardRepository.getLibraryCard(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Library card for user id " + userId + " doesn't exist"));
    }

    @Override
    public LibraryCardResponse addLibraryCard(UUID userId, LibraryCardRequest card) {
        if (!userService.exists(userId))
            throw new DataNotFoundException("Invalid user " + userId + " doesn't exist");
        if (libraryCardRepository.exists(userId))
            throw new DuplicateException("Digital card for user " + userId + " already exists");

        return toResponse(libraryCardRepository.saveLibraryCard(toEntity(userId, card)));
    }

    @Override
    public LibraryCardResponse updateLibraryCard(UUID userId, LibraryCardRequest card) {
        if (!libraryCardRepository.exists(userId))
            throw new DuplicateException("Digital card for user " + userId + " doesn't exist");
        return toResponse(libraryCardRepository.saveLibraryCard(toEntity(userId, card)));
    }

    @Override
    public String getEmail(UUID userId) {
        return libraryCardRepository.getLibraryCard(userId)
                .map(LibraryCardEntity::email)
                .orElseThrow(() -> new DataNotFoundException("Library card for user id " + userId + " doesn't exist"));
    }

    @Override
    public void deleteLibraryCard(UUID userId) {
        if (!libraryCardRepository.exists(userId))
            throw new DataNotFoundException("Can't delete non-existent library card for user " + userId);
        libraryCardRepository.deleteLibraryCard(userId);
    }

    @Override
    public boolean exists(UUID userId) {
        return libraryCardRepository.exists(userId);
    }

    private LibraryCardEntity toEntity(UUID userId, LibraryCardRequest request) {
        return new LibraryCardEntity(userId, request);
    }

    private LibraryCardResponse toResponse(LibraryCardEntity entity) {
        return new LibraryCardResponse(entity);
    }
}
