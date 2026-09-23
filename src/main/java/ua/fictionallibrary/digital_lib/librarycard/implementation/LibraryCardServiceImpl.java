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

import java.util.List;
import java.util.UUID;

@Service
public class LibraryCardServiceImpl implements LibraryCardService {

    private final LibraryCardRepository repository;
    private final UserService userService;

    public LibraryCardServiceImpl(LibraryCardRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    @Override
    public LibraryCardResponse getLibraryCard(UUID userId) {
        return repository.getLibraryCard(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Library card for user id " + userId + " doesn't exist"));
    }

    @Override
    public List<LibraryCardResponse> getAllLibraryCards() {
        return repository.getAllLibraryCards().stream().map(this::toResponse).toList();
    }

    @Override
    public LibraryCardResponse addDigitalCard(UUID userId, LibraryCardRequest card) {
        if (!userService.exists(userId))
            throw new DataNotFoundException("Invalid user " + userId + " doesn't exist");
        if (repository.exists(userId))
            throw new DuplicateException("Digital card for user " + userId + " already exists");

        return toResponse(repository.saveLibraryCard(toEntity(userId, card)));
    }

    @Override
    public LibraryCardResponse updateDigitalCard(UUID userId, LibraryCardRequest card) {
        if (!repository.exists(userId))
            throw new DuplicateException("Digital card for user " + userId + " doesn't exist");
        return toResponse(repository.saveLibraryCard(toEntity(userId, card)));
    }

    @Override
    public String getEmail(UUID userId) {
        return repository.getLibraryCard(userId)
                .map(LibraryCardEntity::email)
                .orElseThrow(() -> new DataNotFoundException("Library card for user id " + userId + " doesn't exist"));
    }

    @Override
    public void deleteLibraryCard(UUID userId) {
        repository.deleteLibraryCard(userId);
    }

    @Override
    public boolean exists(UUID userId) {
        return repository.exists(userId);
    }

    private LibraryCardEntity toEntity(UUID userId, LibraryCardRequest request) {
        return new LibraryCardEntity(userId, request);
    }

    private LibraryCardResponse toResponse(LibraryCardEntity entity) {
        return new LibraryCardResponse(entity);
    }
}
