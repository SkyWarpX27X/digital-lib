package ua.fictionallibrary.digital_lib.logging;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ResourceMonitorScheduler {
    private final ResourceMonitorService service;

    public ResourceMonitorScheduler(ResourceMonitorService service) {
        this.service = service;
    }

    @Scheduled(cron = "${app.scheduling.resource-monitor.cron}", zone = "UTC")
    public void run() {
        service.check();
    }
}
