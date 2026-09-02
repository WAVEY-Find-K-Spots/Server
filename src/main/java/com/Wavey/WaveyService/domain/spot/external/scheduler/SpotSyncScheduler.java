package com.Wavey.WaveyService.domain.spot.external.scheduler;

import com.Wavey.WaveyService.domain.spot.dto.response.SpotSyncResponse;
import com.Wavey.WaveyService.domain.spot.external.service.SpotExternalSyncService;
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

    private final SpotExternalSyncService spotExternalSyncService;

    @Value("${spot.sync.scheduler.media-location-per-page:300}")
    private int mediaLocationPerPage;

    @Value("${spot.sync.scheduler.thumbnail-limit:50}")
    private int thumbnailLimit;

    @Scheduled(cron = "${spot.sync.scheduler.media-location-cron:0 0 3 * * *}", zone = "Asia/Seoul")
    public void syncMediaLocations() {
        SpotSyncResponse response = spotExternalSyncService.syncAllMediaLocationSpots(null, mediaLocationPerPage);
        log.info(
                "Media location sync completed. requested={}, saved={}, updated={}, unchanged={}, skipped={}",
                response.getRequestedCount(),
                response.getSavedCount(),
                response.getUpdatedCount(),
                response.getUnchangedCount(),
                response.getSkippedCount()
        );
    }

    @Scheduled(cron = "${spot.sync.scheduler.thumbnail-cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void fillMediaLocationThumbnails() {
        SpotSyncResponse response = spotExternalSyncService.fillMediaLocationThumbnails(null, thumbnailLimit);
        log.info(
                "Media location thumbnail backfill completed. requested={}, updated={}, skipped={}",
                response.getRequestedCount(),
                response.getUpdatedCount(),
                response.getSkippedCount()
        );
    }
}
