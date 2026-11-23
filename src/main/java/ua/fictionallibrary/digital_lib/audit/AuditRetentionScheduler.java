package ua.fictionallibrary.digital_lib.audit;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuditRetentionScheduler {
    private final AuditLogService service;

    public AuditRetentionScheduler(AuditLogService service) {
        this.service = service;
    }

    @Scheduled(cron = "${app.scheduling.audit-retention.cron}", zone = "UTC")
    @SchedulerLock(name = "auditRetention", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    public void run() {
        service.purgeExpired();
    }
}
