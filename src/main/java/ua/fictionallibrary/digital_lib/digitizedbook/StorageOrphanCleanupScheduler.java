package ua.fictionallibrary.digital_lib.digitizedbook;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class StorageOrphanCleanupScheduler {
    private final DigitizedBookService service;
    private final Duration minAge;

    public StorageOrphanCleanupScheduler(DigitizedBookService service, @Value("${app.storage.orphan-min-age}") Duration minAge) {
        this.service = service;
        this.minAge = minAge;
    }

    @Scheduled(cron = "${app.scheduling.storage-orphans.cron}", zone = "UTC")
    @SchedulerLock(name = "storageOrphanCleanup", lockAtMostFor = "30m", lockAtLeastFor = "1m")
    public void run() {
        service.cleanupOrphanedFiles(minAge);
    }
}
