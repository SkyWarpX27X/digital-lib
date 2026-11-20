package ua.fictionallibrary.digital_lib.filestorage;

import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class S3HealthIndicator implements HealthIndicator {
    private final S3Template s3Template;
    private final String bucket;

    public S3HealthIndicator(S3Template s3Template, @Value("${app.storage.bucket}") String bucket) {
        this.s3Template = s3Template;
        this.bucket = bucket;
    }

    @Override
    public Health health() {
        try {
            if (s3Template.bucketExists(bucket))
                return Health.up().withDetail("bucket", bucket).build();
            return Health.down()
                    .withDetail("bucket", bucket)
                    .withDetail("error", "Бакет не існує")
                    .build();
        } catch (RuntimeException e) {
            return Health.down(e).withDetail("bucket", bucket).build();
        }
    }
}
