package ua.fictionallibrary.digital_lib.logging;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LogLevelResetScheduler {
    private final LogLevelResetService service;

    public LogLevelResetScheduler(LogLevelResetService service) {
        this.service = service;
    }

    @Scheduled(cron = "${app.scheduling.log-level-reset.cron}", zone = "UTC")
    public void run() {
        service.check(Instant.now());
    }
}
