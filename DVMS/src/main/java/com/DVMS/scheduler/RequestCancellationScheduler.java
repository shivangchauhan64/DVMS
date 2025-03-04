package com.DVMS.scheduler;

import com.DVMS.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RequestCancellationScheduler {

    @Autowired
    private VisitorService visitorService;

    @Scheduled(cron = "0 0 0 * * *") // Runs at midnight daily
    public void cancelExpiredRequests() {
        visitorService.cancelExpiredRequests();
    }
}