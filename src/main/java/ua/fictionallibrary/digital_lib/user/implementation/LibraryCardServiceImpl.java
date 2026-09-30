package ua.fictionallibrary.digital_lib.user.implementation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ua.fictionallibrary.digital_lib.exception.DataNotFoundException;
import ua.fictionallibrary.digital_lib.exception.DuplicateException;
import ua.fictionallibrary.digital_lib.user.*;
import ua.fictionallibrary.digital_lib.user.dto.LibraryCardRequest;
import ua.fictionallibrary.digital_lib.user.dto.LibraryCardResponse;

import java.util.UUID;

@Service
public class LibraryCardServiceImpl implements LibraryCardService {

    private final LibraryCardRepository libraryCardRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;

    public LibraryCardServiceImpl(LibraryCardRepository repository, UserRepository userRepository, ApplicationEventPublisher eventPublisher, UserService userService) {
        this.libraryCardRepository = repository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.userService = userService;
    }

    @Override
    public LibraryCardResponse getLibraryCard(UUID userId) {
        return libraryCardRepository.findByOwnerId(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Library card for user id " + userId + " doesn't exist"));
    }

    @Override
    public LibraryCardResponse addLibraryCard(UUID userId, LibraryCardRequest card) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(
                        "Invalid user " + userId + " doesn't exist"
                ));
        if (!libraryCardRepository.existsById(userId))
            throw new DataNotFoundException("User with id " + userId + " does not have a library card and cannot place book orders");
        if (libraryCardRepository.existsById(userId))
            throw new DuplicateException("Digital card for user " + userId + " already exists");

        eventPublisher.publishEvent(new LibraryCardAddedEvent(
                userId,
                card.email()
        ));
        return toResponse(libraryCardRepository.save(toEntity(card, user)));
    }

    @Override
    public LibraryCardResponse updateLibraryCard(UUID userId, LibraryCardRequest card) {
        if (!libraryCardRepository.existsById(userId))
            throw new DataNotFoundException("Digital card for user " + userId + " doesn't exist");
        User user = userRepository.getReferenceById(userId);
        return toResponse(libraryCardRepository.save(toEntity(card, user)));
    }

    @Override
    public String getEmail(UUID userId) {
        return libraryCardRepository.findByOwnerId(userId)
                .map(LibraryCard::getEmail)
                .orElseThrow(() -> new DataNotFoundException("Library card for user id " + userId + " doesn't exist"));
    }

    @Override
    public void deleteLibraryCard(UUID userId) {
        if (!libraryCardRepository.existsByOwnerId(userId))
            throw new DataNotFoundException("Can't delete non-existent library card for user " + userId);
        libraryCardRepository.deleteById(userId);
    }

    @Override
    public boolean exists(UUID userId) {
        return libraryCardRepository.existsByOwnerId(userId);
    }

    private LibraryCardResponse toResponse(LibraryCard entity) {
        return new LibraryCardResponse(entity.getOwnerId(), entity.getEmail(), entity.getPostCode(), entity.getBirthYear(),
                entity.getLivingAddress(), entity.getWorkOrStudyAddress(), entity.getOrganisation(), entity.getWorkPosition());
    }
    private LibraryCard toEntity(LibraryCardRequest request, User user) {
        return new LibraryCard(user.getId(), request.email(), request.postcode(), request.birthYear(),
                request.livingAddress(), request.workOrStudyAddress(), request.organisation(), request.workPosition(),
                user);
    }
}
