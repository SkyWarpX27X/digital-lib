package ua.fictionallibrary.digital_lib.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("digitallib.exception-handling")
public record ExceptionHandlingProperties(
        boolean enabled) {
}
