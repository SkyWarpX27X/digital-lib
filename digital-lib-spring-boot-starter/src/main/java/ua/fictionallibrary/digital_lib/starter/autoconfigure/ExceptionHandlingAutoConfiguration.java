package ua.fictionallibrary.digital_lib.starter.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import ua.fictionallibrary.digital_lib.starter.exception.GlobalExceptionHandler;

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
