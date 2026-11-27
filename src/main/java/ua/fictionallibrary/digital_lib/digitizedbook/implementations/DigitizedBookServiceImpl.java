package ua.fictionallibrary.digital_lib.digitizedbook.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import ua.fictionallibrary.digital_lib.audit.AuditAction;
import ua.fictionallibrary.digital_lib.author.Author;
import ua.fictionallibrary.digital_lib.author.AuthorService;
import ua.fictionallibrary.digital_lib.common.BookFilter;
import ua.fictionallibrary.digital_lib.common.BookFilterOptions;
import ua.fictionallibrary.digital_lib.digitizedbook.BookDigitizedEvent;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookDeletedEvent;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBook;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookRepository;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookService;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.MissingStorageFile;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.StoredFile;
import ua.fictionallibrary.digital_lib.filestorage.FileStorageService;
import ua.fictionallibrary.digital_lib.filestorage.FileValidator;
import ua.fictionallibrary.digital_lib.filestorage.StoredObject;
import ua.fictionallibrary.digital_lib.starter.exception.DataNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class DigitizedBookServiceImpl implements DigitizedBookService {
    private static final String FILTER_OPTIONS_CACHE = "digitized-book-filter-options";
    private static final String STORAGE_PREFIX = "digitized-books/";

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
    public DigitizedBookResponse getDigitizedBook(UUID id, boolean canReadCopyrighted) {
        return repository.findById(id)
                .map(book -> toResponse(book, canReadCopyrighted))
                .orElseThrow(() -> new DataNotFoundException("Not found digitized book with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = FILTER_OPTIONS_CACHE)
    public BookFilterOptions getFilterOptions() {
        return new BookFilterOptions(repository.findDistinctTopics(), repository.findDistinctLanguages(),
                repository.findDistinctResourceTypes());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DigitizedBookResponse> getAllDigitizedBooks(BookFilter filter, boolean canReadCopyrighted) {
        return repository.search(filter).stream().map(book -> toResponse(book, canReadCopyrighted)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DigitizedBookResponse> getDigitizedBooks(Collection<UUID> ids, boolean canReadCopyrighted) {
        if (ids.isEmpty())
            return List.of();
        return repository.findAllWithAuthorsByIdIn(ids).stream().map(book -> toResponse(book, canReadCopyrighted)).toList();
    }

    @Override
    @Transactional
    @AuditAction
    @CacheEvict(cacheNames = FILTER_OPTIONS_CACHE, allEntries = true)
    public DigitizedBookResponse addDigitizedBook(DigitizedBookRequest request, UUID physicalBookId, MultipartFile pdf,
                                                  MultipartFile cover) {
        fileValidator.validatePdf(pdf);
        String coverFormat = (cover != null && !cover.isEmpty()) ? fileValidator.validateCoverImage(cover) : null;
        String base = STORAGE_PREFIX + UUID.randomUUID();
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
                physicalBookId,
                digitizedBook.getName()
        ));
        return toResponse(digitizedBook, true);
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
    @CacheEvict(cacheNames = FILTER_OPTIONS_CACHE, allEntries = true)
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
        return toResponse(repository.save(book), true);
    }

    @Override
    @Transactional
    @AuditAction
    @CacheEvict(cacheNames = FILTER_OPTIONS_CACHE, allEntries = true)
    public void deleteDigitizedBook(UUID id) {
        DigitizedBook book = repository.findById(id).orElseThrow(() -> new DataNotFoundException("Failed to delete, not found digitized book with id " + id));
        List<String> keys = Stream.of(book.getPdfKey(), book.getCoverKey()).filter(Objects::nonNull).toList();

        repository.deleteById(id);
        publisher.publishEvent(new DigitizedBookDeletedEvent(id));

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
    public StoredFile getPdf(UUID id) {
        DigitizedBook book = repository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Not found digitized book with id " + id));
        return new StoredFile(fileStorageService.load(book.getPdfKey()), "application/pdf");
    }

    @Override
    public URL getPdfDownloadUrl(UUID id, Duration ttl) {
        DigitizedBook book = repository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Not found digitized book with id " + id));
        return fileStorageService.presignedUrl(book.getPdfKey(), ttl);
    }

    @Override
    public StoredFile getCover(UUID id) {
        DigitizedBook book = repository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Not found digitized book with id " + id));
        if (book.getCoverKey() == null)
            throw new DataNotFoundException("Digitized book with id " + id + " has no cover");
        return new StoredFile(fileStorageService.load(book.getCoverKey()), book.getCoverContentType());
    }

    @Override
    public boolean exists(UUID id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public void cleanupOrphanedFiles(Duration minAge) {
        log.info("Storage orphan cleanup started (min age {})", minAge);
        Set<String> referenced = new HashSet<>(repository.findAllPdfKeys());
        referenced.addAll(repository.findAllCoverKeys());
        Instant cutoff = Instant.now().minus(minAge);
        List<String> orphans = fileStorageService.list(STORAGE_PREFIX).stream()
                .filter(o -> !referenced.contains(o.key()) && o.lastModified().isBefore(cutoff))
                .map(StoredObject::key)
                .toList();
        int deleted = 0;
        for (String key : orphans) {
            try {
                fileStorageService.delete(key);
                deleted++;
                log.debug("Deleted orphaned S3 object {}", key);
            }
            catch (Exception e) {
                log.warn("Could not delete orphaned S3 object {}", key, e);
            }
        }
        log.info("Storage orphan cleanup finished: found {}, deleted {}, failed {}",
                orphans.size(), deleted, orphans.size() - deleted);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissingStorageFile> checkStorageIntegrity() {
        log.info("Storage integrity check started");
        List<DigitizedBook> books = repository.findAll();
        List<MissingStorageFile> missing = new ArrayList<>();
        for (DigitizedBook book : books) {
            for (String key : Stream.of(book.getPdfKey(), book.getCoverKey()).filter(Objects::nonNull).toList()) {
                if (!fileStorageService.exists(key)) {
                    log.warn("Digitized book {} '{}' is missing S3 object {}", book.getId(), book.getName(), key);
                    missing.add(new MissingStorageFile(book.getId(), book.getName(), key));
                }
            }
        }
        log.info("Storage integrity check finished: {} books checked, {} files missing", books.size(), missing.size());
        return missing;
    }

    private DigitizedBook toEntity(DigitizedBookRequest request) {
        return new DigitizedBook(request.name(), authorService.findOrCreate(request.authors()), request.description(),
                request.topic(), request.publishingYear(), request.language(), request.resourceType(),
                request.isCopyrighted());
    }

    private DigitizedBookResponse toResponse(DigitizedBook entity, boolean canReadCopyrighted) {
        List<String> authors = entity.getAuthors().stream().map(Author::getName).toList();
        String base = "/api/v1/digitized-books/" + entity.getId();
        boolean canRead = !entity.isCopyrighted() || canReadCopyrighted;
        return new DigitizedBookResponse(entity.getId(), entity.getName(), authors, entity.getDescription(),
                entity.getTopic(), entity.getPublishingYear(), entity.getLanguage(), entity.getResourceType(),
                entity.isCopyrighted(), entity.getCoverKey() == null ? null : base + "/cover",
                canRead ? base + "/pdf" : null, canRead);
    }

}
