package ua.fictionallibrary.digital_lib.support;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Tag("integration")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
        "app.scheduling.storage-orphans.cron=-",
        "app.scheduling.audit-retention.cron=-",
        "app.scheduling.storage-integrity.cron=-",
        "app.scheduling.resource-monitor.cron=-",
        "app.scheduling.log-level-reset.cron=-",
        "app.scheduling.filter-options-refresh.cron=-"})
@AutoConfigureMockMvc
@Sql(scripts = "/sql/reset_db.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Import(TestcontainersConfiguration.class)
public @interface IntegrationTest {
}
