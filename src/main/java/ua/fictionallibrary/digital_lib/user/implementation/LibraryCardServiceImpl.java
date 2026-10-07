package ua.fictionallibrary.digital_lib.user.implementation;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.fictionallibrary.digital_lib.starter.DataNotFoundException;
import ua.fictionallibrary.digital_lib.starter.DuplicateException;
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
    @Transactional(readOnly = true)
    public LibraryCardResponse getLibraryCard(UUID userId) {
        return libraryCardRepository.findByOwnerId(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Library card for user id " + userId + " doesn't exist"));
    }

    @Override
    @Transactional
    public LibraryCardResponse addLibraryCard(UUID userId, LibraryCardRequest card) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(
                        "Invalid user " + userId + " doesn't exist"
                ));
        if (libraryCardRepository.existsByOwnerId(userId))
            throw new DuplicateException("Digital card for user " + userId + " already exists");

        LibraryCard saved = libraryCardRepository.save(toEntity(card, user));
        user.setLibraryCard(saved);
        eventPublisher.publishEvent(new LibraryCardAddedEvent(
                saved.toString()
        ));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public LibraryCardResponse updateLibraryCard(UUID userId, LibraryCardRequest card) {
        LibraryCard existing = libraryCardRepository.findByOwnerId(userId)
                .orElseThrow(() -> new DataNotFoundException("Digital card for user " + userId + " doesn't exist"));
        existing.setEmail(card.email());
        existing.setPostCode(card.postcode());
        existing.setBirthYear(card.birthYear());
        existing.setLivingAddress(card.livingAddress());
        existing.setWorkOrStudyAddress(card.workOrStudyAddress());
        existing.setOrganisation(card.organisation());
        existing.setWorkPosition(card.workPosition());
        return toResponse(libraryCardRepository.save(existing));
    }

    @Override
    @Transactional(readOnly = true)
    public String getEmail(UUID userId) {
        return libraryCardRepository.findByOwnerId(userId)
                .map(LibraryCard::getEmail)
                .orElseThrow(() -> new DataNotFoundException("Library card for user id " + userId + " doesn't exist"));
    }

    @Override
    @Transactional
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
        return new LibraryCard(null, request.email(), request.postcode(), request.birthYear(),
                request.livingAddress(), request.workOrStudyAddress(), request.organisation(), request.workPosition(),
                user);
    }
}
