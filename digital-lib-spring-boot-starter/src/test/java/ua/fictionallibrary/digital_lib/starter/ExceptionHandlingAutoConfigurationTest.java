package ua.fictionallibrary.digital_lib.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ua.fictionallibrary.digital_lib.starter.autoconfigure.ExceptionHandlingAutoConfiguration;
import ua.fictionallibrary.digital_lib.starter.exception.GlobalExceptionHandler;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionHandlingAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExceptionHandlingAutoConfiguration.class));

    @Test
    void shouldCreateHandlerWhenEnabled() {
        runner
                .withPropertyValues("digitallib.exception-handling.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(GlobalExceptionHandler.class));
    }

    @Test
    void shouldNotCreateHandlerWhenDisabled() {
        runner
                .withPropertyValues("digitallib.exception-handling.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(GlobalExceptionHandler.class));
    }
}
