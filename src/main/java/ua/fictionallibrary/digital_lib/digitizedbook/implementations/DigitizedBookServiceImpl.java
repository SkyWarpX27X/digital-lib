package ua.fictionallibrary.digital_lib.digitizedbook.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import ua.fictionallibrary.digital_lib.audit.AuditAction;
import ua.fictionallibrary.digital_lib.author.Author;
import ua.fictionallibrary.digital_lib.author.AuthorService;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBook;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookRepository;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookService;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.filestorage.FileStorageService;
import ua.fictionallibrary.digital_lib.filestorage.FileValidator;
import ua.fictionallibrary.digital_lib.filestorage.S3FileStorageService;
import ua.fictionallibrary.digital_lib.starter.exception.DataNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class DigitizedBookServiceImpl implements DigitizedBookService {

    private final DigitizedBookRepository repository;
    private final ApplicationEventPublisher publisher;
    private final AuthorService authorService;
    private final FileValidator fileValidator;
    private final FileStorageService fileStorageService;
    private final Logger log = LoggerFactory.getLogger(DigitizedBookServiceImpl.class);

    public DigitizedBookServiceImpl(DigitizedBookRepository repository, ApplicationEventPublisher publisher, AuthorService authorService, FileValidator fileValidator, FileStorageService fileStorageService) {
        this.repository = repository;
        this.publisher = publisher;
        this.authorService = authorService;
        this.fileValidator = fileValidator;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public DigitizedBookResponse getDigitizedBook(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new DataNotFoundException("Not found digitized book with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DigitizedBookResponse> getAllDigitizedBooks() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<DigitizedBookResponse> findDigitizedBooksByName(String name) {
        final var books = (name != null && !name.isBlank()) ? repository.findByName(name) : repository.findAll();
        return books.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    @AuditAction
    public DigitizedBookResponse addDigitizedBook(DigitizedBookRequest request, UUID physicalBookId, MultipartFile pdf,
                                                  MultipartFile cover) {
        fileValidator.validatePdf(pdf);
        String coverFormat = (cover != null && !cover.isEmpty()) ? fileValidator.validateCoverImage(cover) : null;
        String base = "digitized-books/" + UUID.randomUUID();
        String pdfKey = base + "/book.pdf";
        String coverKey = coverFormat == null ? null : base + "/cover." + (coverFormat.equals("image/png") ? "png" : "jpg");
        upload(pdfKey, pdf, "application/pdf");
        if (coverKey != null)
            upload(coverKey, cover, coverFormat);
        DigitizedBook entity = toEntity(request);
        entity.setPdfKey(pdfKey);
        entity.setCoverKey(coverKey);
        entity.setCoverContentType(coverFormat);
        DigitizedBook digitizedBook = repository.save(entity);
        publisher.publishEvent(new BookDigitizedEvent(
                digitizedBook.getId(),
                physicalBookId
        ));
        return toResponse(digitizedBook);
    }

    private void upload(String key, MultipartFile file, String contentType) {
        try (InputStream in = file.getInputStream()) {
            fileStorageService.store(key, in, 0L, contentType);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        deleteIfRolledBack(key);
    }

    private void deleteIfRolledBack(String key) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        fileStorageService.delete(key);
                    }
                    catch (Exception e) {
                        log.warn("Could not clean up orphaned S3 object {}", key, e);
                    }
                }
            }
        });
    }

    @Override
    @Transactional
    @AuditAction
    public DigitizedBookResponse updateDigitizedBook(UUID id, DigitizedBookRequest request) {
        DigitizedBook book = repository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Failed to update, not found digitized book with id " + id));
        book.setName(request.name());
        book.setAuthors(authorService.findOrCreate(request.authors()));
        book.setDescription(request.description());
        book.setTopic(request.topic());
        book.setPublishingYear(request.publishingYear());
        book.setLanguage(request.language());
        book.setResourceType(request.resourceType());
        book.setCopyrighted(request.isCopyrighted());
        return toResponse(repository.save(book));
    }

    @Override
    @Transactional
    @AuditAction
    public void deleteDigitizedBook(UUID id) {
        DigitizedBook book = repository.findById(id).orElseThrow(() -> new DataNotFoundException("Failed to delete, not found digitized book with id " + id));
        List<String> keys = Stream.of(book.getPdfKey(), book.getCoverKey()).filter(Objects::nonNull).toList();

        repository.deleteById(id);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String key : keys) {
                    try {
                        fileStorageService.delete(key);
                    }
                    catch (Exception e) {
                        log.warn("Could not clean up orphaned S3 object {}", key, e);
                    }
                }
            }
        });
    }

    @Override
    public boolean exists(UUID id) {
        return repository.existsById(id);
    }

    private DigitizedBook toEntity(DigitizedBookRequest request) {
        return new DigitizedBook(request.name(), authorService.findOrCreate(request.authors()), request.description(),
                request.topic(), request.publishingYear(), request.language(), request.resourceType(),
                request.isCopyrighted());
    }

    private DigitizedBookResponse toResponse(DigitizedBook entity) {
        List<String> authors = entity.getAuthors().stream().map(Author::getName).toList();
        String base = "/digitized-books/" + entity.getId();
        return new DigitizedBookResponse(entity.getId(), entity.getName(), authors, entity.getDescription(),
                entity.getTopic(), entity.getPublishingYear(), entity.getLanguage(), entity.getResourceType(),
                entity.isCopyrighted(), entity.getCoverKey() == null ? null : base + "/cover", base + "/pdf");
    }

}
