package ua.fictionallibrary.digital_lib.filestorage;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;

@Service
public class S3FileStorageService implements FileStorageService {
    private final S3Template s3template;
    private final S3Client s3Client;

    public S3FileStorageService(S3Template s3template, S3Client s3Client) {
        this.s3template = s3template;
        this.s3Client = s3Client;
    }

    @Value("${app.storage.bucket}")
    private String bucket;

    @Override
    public void store(String key, InputStream data, long size, String contentType) {
        s3template.upload(bucket, key, data, ObjectMetadata.builder().contentType(contentType).build());
    }


    @Override
    public InputStream load(String key) {
        try {
            return s3template.download(bucket, key).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String key) {
        s3template.deleteObject(bucket, key);
    }

    @Override
    public List<StoredObject> list(String prefix) {
        return s3Client.listObjectsV2Paginator(r -> r.bucket(bucket).prefix(prefix)).contents().stream()
                .map(o -> new StoredObject(o.key(), o.lastModified()))
                .toList();
    }

    @Override
    public boolean exists(String key) {
        return s3template.objectExists(bucket, key);
    }

    @Override
    public URL presignedUrl(String key, Duration ttl) {
        return s3template.createSignedGetURL(bucket, key, ttl);
    }
}
