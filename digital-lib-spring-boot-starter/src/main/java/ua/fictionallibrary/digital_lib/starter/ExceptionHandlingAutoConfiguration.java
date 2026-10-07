package ua.fictionallibrary.digital_lib.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ExceptionHandlingProperties.class)
@ConditionalOnProperty(
        prefix = "digitallib.exception-handling",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ExceptionHandlingAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
