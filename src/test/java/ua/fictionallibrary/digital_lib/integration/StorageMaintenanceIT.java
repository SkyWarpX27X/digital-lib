package ua.fictionallibrary.digital_lib.integration;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBook;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookRepository;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookService;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.MissingStorageFile;
import ua.fictionallibrary.digital_lib.filestorage.FileStorageService;
import ua.fictionallibrary.digital_lib.support.IntegrationTest;
import ua.fictionallibrary.digital_lib.support.MinioTestcontainersConfiguration;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Year;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@Import(MinioTestcontainersConfiguration.class)
class StorageMaintenanceIT {

    @Autowired
    private DigitizedBookService service;
    @Autowired
    private DigitizedBookRepository repository;
    @Autowired
    private FileStorageService storage;
    @Autowired
    private S3Template s3Template;
    @Value("${app.storage.bucket}")
    private String bucket;

    @BeforeEach
    void createBucket() {
        if (!s3Template.bucketExists(bucket))
            s3Template.createBucket(bucket);
    }

    @Test
    void cleanup_deletesOldOrphan_keepsBookFiles() {
        DigitizedBook book = saveBook(true, true);
        String orphan = put("digitized-books/" + UUID.randomUUID() + "/book.pdf");

        service.cleanupOrphanedFiles(Duration.ZERO);

        assertThat(storage.exists(orphan)).isFalse();
        assertThat(storage.exists(book.getPdfKey())).isTrue();
        assertThat(storage.exists(book.getCoverKey())).isTrue();
    }

    @Test
    void cleanup_keepsOrphanYoungerThanMinAge() {
        String orphan = put("digitized-books/" + UUID.randomUUID() + "/book.pdf");

        service.cleanupOrphanedFiles(Duration.ofHours(1));

        assertThat(storage.exists(orphan)).isTrue();
    }

    @Test
    void cleanup_ignoresObjectsOutsidePrefix() {
        String other = put("other/" + UUID.randomUUID() + ".txt");

        service.cleanupOrphanedFiles(Duration.ZERO);

        assertThat(storage.exists(other)).isTrue();
    }

    @Test
    void integrityCheck_reportsMissingPdf_notCompleteBook() {
        DigitizedBook complete = saveBook(true, true);
        DigitizedBook broken = saveBook(false, false);

        var missing = service.checkStorageIntegrity();

        assertThat(missing).contains(new MissingStorageFile(broken.getId(), broken.getName(), broken.getPdfKey()));
        assertThat(missing).noneMatch(m -> m.bookId().equals(complete.getId()));
    }

    private DigitizedBook saveBook(boolean uploadFiles, boolean withCover) {
        String base = "digitized-books/" + UUID.randomUUID();
        DigitizedBook book = new DigitizedBook("Book " + base, new HashSet<>(), "Опис", "Тема", Year.of(2000),
                "uk", "Книга", false);
        book.setPdfKey(base + "/book.pdf");
        if (withCover) {
            book.setCoverKey(base + "/cover.png");
            book.setCoverContentType("image/png");
        }
        if (uploadFiles) {
            put(book.getPdfKey());
            if (withCover)
                put(book.getCoverKey());
        }
        return repository.save(book);
    }

    private String put(String key) {
        storage.store(key, new ByteArrayInputStream(new byte[]{1}), 1, "application/octet-stream");
        return key;
    }
}
