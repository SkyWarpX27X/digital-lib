package ua.fictionallibrary.digital_lib.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@TestConfiguration(proxyBeanMethods = false)
public class MinioTestcontainersConfiguration {
    @Bean
    @SuppressWarnings("resource")
    GenericContainer<?> minio() {
        return new GenericContainer<>("pgsty/minio")
                .withCommand("server /data")
                .withEnv("MINIO_ROOT_USER", "minioadmin")
                .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                .withExposedPorts(9000)
                .waitingFor(Wait.forHttp("/minio/health/live").forPort(9000));
    }

    @Bean
    DynamicPropertyRegistrar minioProperties(GenericContainer<?> minio) {
        return registry -> registry.add("spring.cloud.aws.s3.endpoint",
                () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
    }
}