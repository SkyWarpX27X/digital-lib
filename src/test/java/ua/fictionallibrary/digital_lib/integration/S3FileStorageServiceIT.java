package ua.fictionallibrary.digital_lib.integration;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import ua.fictionallibrary.digital_lib.filestorage.S3FileStorageService;
import ua.fictionallibrary.digital_lib.support.IntegrationTest;
import ua.fictionallibrary.digital_lib.support.MinioTestcontainersConfiguration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@Import(MinioTestcontainersConfiguration.class)
class S3FileStorageServiceIT {

    @Autowired
    private S3FileStorageService storage;

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
    void storeLoadDelete_roundTrip() throws Exception {
        String key = "digitized-books/" + UUID.randomUUID() + "/book.pdf";
        byte[] data = "%PDF-test".getBytes(StandardCharsets.UTF_8);

        storage.store(key, new ByteArrayInputStream(data), data.length, "application/pdf");
        try (InputStream in = storage.load(key)) {
            assertThat(in.readAllBytes()).isEqualTo(data);
        }

        storage.delete(key);
        assertThat(s3Template.objectExists(bucket, key)).isFalse();
    }
}