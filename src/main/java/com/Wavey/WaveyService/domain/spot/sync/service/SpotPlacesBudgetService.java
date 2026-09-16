package com.Wavey.WaveyService.domain.spot.sync.service;

import java.time.YearMonth;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Google Places Photo 무료 제공량(월 단위 리셋, 2026-09 기준 SKU당 1,000건/월)을 넘지 않도록
 * 소진량을 추적한다. 인메모리 카운터라 배포 재시작 시 리셋되는 한계는 {@link SpotSyncBudgetService}와 동일.
 */
@Service
public class SpotPlacesBudgetService {

    @Value("${spot.sync.google-places.monthly-photo-limit:1000}")
    private int monthlyPhotoLimit;

    private YearMonth month = YearMonth.now();
    private int usedCount;

    public synchronized int remaining() {
        rollMonth();
        return Math.max(0, monthlyPhotoLimit - usedCount);
    }

    public synchronized boolean canUseOne() {
        return remaining() > 0;
    }

    public synchronized void recordUsed() {
        rollMonth();
        usedCount++;
    }

    public synchronized int getUsedCount() {
        rollMonth();
        return usedCount;
    }

    private void rollMonth() {
        YearMonth current = YearMonth.now();
        if (!current.equals(month)) {
            month = current;
            usedCount = 0;
        }
    }
}
