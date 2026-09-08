package com.Wavey.WaveyService.domain.spot.external.bootstrap;

import com.Wavey.WaveyService.domain.spot.external.service.MediaLocationCsvImportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spot.sync.csv-bootstrap", name = "enabled", havingValue = "true")
public class MediaLocationCsvImportRunner implements ApplicationRunner {

    private final MediaLocationCsvImportService mediaLocationCsvImportService;

    @Override
    public void run(ApplicationArguments args) {
        var summary = mediaLocationCsvImportService.importCsv();
        log.info(
                "촬영지 CSV 가져오기 완료. 요청={}, 저장={}, 갱신={}, 변경 없음={}, 제외={}",
                summary.getRequestedCount(),
                summary.getSavedCount(),
                summary.getUpdatedCount(),
                summary.getUnchangedCount(),
                summary.getSkippedCount());
    }
}
