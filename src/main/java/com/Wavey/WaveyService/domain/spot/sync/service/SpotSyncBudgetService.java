package com.Wavey.WaveyService.domain.spot.sync.service;

import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncStatusResponse;
import java.time.LocalDate;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SpotSyncBudgetService {

    @Value("${spot.sync.tour-api.daily-call-limit:1000}")
    private int dailyCallLimit;

    @Value("${spot.sync.tour-api.safety-reserve:50}")
    private int safetyReserve;

    @Getter
    private int nextHeritagePage = 1;

    private LocalDate date = LocalDate.now();
    private int apiCallCount;

    public synchronized boolean canCall() {
        rollDate();
        return remainingCalls() > safetyReserve;
    }

    public synchronized void recordCall() {
        rollDate();
        apiCallCount++;
    }

    public synchronized int getApiCallCount() {
        rollDate();
        return apiCallCount;
    }

    public synchronized void updateNextHeritagePage(int nextHeritagePage) {
        this.nextHeritagePage = Math.max(1, nextHeritagePage);
    }

    public synchronized SpotSyncStatusResponse status() {
        rollDate();
        return SpotSyncStatusResponse.builder()
                .date(date)
                .dailyCallLimit(dailyCallLimit)
                .safetyReserve(safetyReserve)
                .apiCallCount(apiCallCount)
                .remainingCalls(remainingCalls())
                .nextHeritagePage(nextHeritagePage)
                .build();
    }

    private int remainingCalls() {
        return Math.max(0, dailyCallLimit - apiCallCount);
    }

    private void rollDate() {
        LocalDate today = LocalDate.now();
        if (!today.equals(date)) {
            date = today;
            apiCallCount = 0;
        }
    }
}
