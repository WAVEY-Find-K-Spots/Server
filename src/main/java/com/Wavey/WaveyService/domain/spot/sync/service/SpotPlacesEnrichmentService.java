package com.Wavey.WaveyService.domain.spot.sync.service;

import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.spot.sync.client.SpotPlacesImageClient;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotPlacesEnrichResponse;
import com.Wavey.WaveyService.domain.upload.enums.UploadCategory;
import com.Wavey.WaveyService.domain.upload.service.UploadService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotPlacesEnrichmentService {

    private final SpotRepository spotRepository;
    private final SpotPlacesImageClient placesImageClient;
    private final SpotPlacesBudgetService budgetService;
    private final UploadService uploadService;

    @Transactional
    public SpotPlacesEnrichResponse enrichImages(int limit) {
        int budgetBefore = budgetService.remaining();
        int toProcess = Math.min(limit, budgetBefore);

        if (toProcess <= 0) {
            return SpotPlacesEnrichResponse.builder()
                    .requested(0)
                    .filled(0)
                    .skipped(0)
                    .budgetBefore(budgetBefore)
                    .budgetAfter(budgetBefore)
                    .stoppedByBudget(true)
                    .build();
        }

        List<Spot> candidates = spotRepository.findMissingImage(PageRequest.of(0, toProcess));

        int filled = 0;
        int skipped = 0;
        for (Spot spot : candidates) {
            if (!budgetService.canUseOne()) {
                break;
            }
            try {
                Optional<SpotPlacesImageClient.PhotoResult> photo =
                        placesImageClient.findPhoto(spot.getNameKo(), spot.getAddressKo());
                if (photo.isEmpty()) {
                    skipped++;
                    continue;
                }
                budgetService.recordUsed();
                String imageUrl = uploadService.uploadServerSide(
                        UploadCategory.SPOT, spot.getSpotId(), photo.get().bytes(), photo.get().contentType());
                spot.updateImageUrl(imageUrl);
                filled++;
            } catch (Exception e) {
                log.warn("Google Places image enrichment failed for spotId={}", spot.getSpotId(), e);
                skipped++;
            }
        }

        int budgetAfter = budgetService.remaining();
        return SpotPlacesEnrichResponse.builder()
                .requested(candidates.size())
                .filled(filled)
                .skipped(skipped)
                .budgetBefore(budgetBefore)
                .budgetAfter(budgetAfter)
                .stoppedByBudget(budgetAfter == 0)
                .build();
    }
}
