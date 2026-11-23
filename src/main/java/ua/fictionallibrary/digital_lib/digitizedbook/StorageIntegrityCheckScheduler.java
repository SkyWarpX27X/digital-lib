package ua.fictionallibrary.digital_lib.digitizedbook;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StorageIntegrityCheckScheduler {
    private final DigitizedBookService service;

    public StorageIntegrityCheckScheduler(DigitizedBookService service) {
        this.service = service;
    }

    @Scheduled(cron = "${app.scheduling.storage-integrity.cron}", zone = "UTC")
    @SchedulerLock(name = "storageIntegrityCheck", lockAtMostFor = "30m", lockAtLeastFor = "1m")
    public void run() {
        service.checkStorageIntegrity();
    }
}
