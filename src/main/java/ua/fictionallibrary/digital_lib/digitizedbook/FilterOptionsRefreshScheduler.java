package ua.fictionallibrary.digital_lib.digitizedbook;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FilterOptionsRefreshScheduler {
    private final DigitizedBookService digitizedBookService;

    public FilterOptionsRefreshScheduler(DigitizedBookService digitizedBookService) {
        this.digitizedBookService = digitizedBookService;
    }

    @Scheduled(cron = "${app.scheduling.filter-options-refresh.cron}", zone = "UTC")
    public void run() {
        digitizedBookService.refreshFilterOptions();
    }
}
