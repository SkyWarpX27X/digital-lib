package ua.fictionallibrary.digital_lib.physicalbook;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FilterOptionsRefreshScheduler {
    private final PhysicalBookService physicalBookService;

    public FilterOptionsRefreshScheduler(PhysicalBookService physicalBookService) {
        this.physicalBookService = physicalBookService;
    }

    @Scheduled(cron = "${app.scheduling.filter-options-refresh.cron}", zone = "UTC")
    public void run() {
        physicalBookService.refreshFilterOptions();
    }
}
