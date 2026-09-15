package com.Wavey.WaveyService.domain.spot.sync.scheduler;

import com.Wavey.WaveyService.domain.spot.sync.service.SpotSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spot.sync.scheduler", name = "enabled", havingValue = "true")
public class SpotSyncScheduler {

    private final SpotSyncService spotSyncService;

    @Value("${spot.sync.scheduler.media-location-per-page:300}")
    private int mediaLocationPerPage;

    @Scheduled(cron = "${spot.sync.scheduler.media-location-cron:0 0 3 * * *}")
    public void syncMediaLocations() {
        log.info("Scheduled media-location spot sync started.");
        spotSyncService.syncMediaPage(null, 1, mediaLocationPerPage);
        log.info("Scheduled media-location spot sync finished.");
    }
}
