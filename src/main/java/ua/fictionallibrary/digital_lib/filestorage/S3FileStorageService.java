package ua.fictionallibrary.digital_lib.filestorage;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class S3FileStorageService implements FileStorageService {
    private final S3Template s3template;

    public S3FileStorageService(S3Template s3template) {
        this.s3template = s3template;
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
}
