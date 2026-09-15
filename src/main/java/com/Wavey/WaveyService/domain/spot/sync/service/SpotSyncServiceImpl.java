package com.Wavey.WaveyService.domain.spot.sync.service;

import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import com.Wavey.WaveyService.domain.content.entity.SpotContent;
import com.Wavey.WaveyService.domain.content.repository.ContentRepository;
import com.Wavey.WaveyService.domain.content.repository.SpotContentRepository;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.PlaceType;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.spot.sync.client.SpotSyncMediaLocationClient;
import com.Wavey.WaveyService.domain.spot.sync.client.SpotSyncTourApiClient;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncPage;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncPayload;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncResponse;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncStatusResponse;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotTitleFeasibilityResponse;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotTitleReviewRequest;
import com.Wavey.WaveyService.domain.spot.sync.support.SpotSyncRegionResolver;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SpotSyncServiceImpl implements SpotSyncService {

    private static final List<SpotCategory> MEDIA_CATEGORIES = List.of(SpotCategory.K_DRAMA, SpotCategory.K_MOVIE, SpotCategory.K_POP);

    private final SpotSyncTourApiClient tourApiClient;
    private final SpotSyncMediaLocationClient mediaLocationClient;
    private final SpotSyncBudgetService budgetService;
    private final SpotSyncRegionResolver regionResolver;
    private final SpotRepository spotRepository;
    private final ContentRepository contentRepository;
    private final SpotContentRepository spotContentRepository;

    @Override
    @Transactional
    public SpotSyncResponse syncTourDaily(int size) {
        validatePaging(1, size);
        if (!budgetService.canCall()) {
            return emptyResponse(true);
        }
        int before = budgetService.getApiCallCount();
        int page = budgetService.getNextHeritagePage();
        budgetService.recordCall();
        SpotSyncResponse response = syncPage(tourApiClient.fetchHeritage(page, size), true, before);
        if (!response.isStoppedByDailyLimit()) {
            budgetService.updateNextHeritagePage(page + 1);
        }
        return response;
    }

    @Override
    @Transactional
    public SpotSyncResponse enrichPlace(String name) {
        if (!StringUtils.hasText(name)) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
        if (!budgetService.canCall()) {
            return emptyResponse(true);
        }
        int before = budgetService.getApiCallCount();
        budgetService.recordCall();
        budgetService.recordCall();
        budgetService.recordCall();
        budgetService.recordCall();
        return syncPage(tourApiClient.enrichPlace(name), true, before);
    }

    @Override
    @Transactional
    public SpotSyncResponse syncMediaPage(SpotCategory category, int page, int size) {
        validatePaging(page, size);
        validateMediaCategory(category);
        return syncPage(mediaLocationClient.fetch(category, page, size), true, 0);
    }

    @Override
    public SpotSyncStatusResponse status() {
        return budgetService.status();
    }

    @Override
    public SpotTitleFeasibilityResponse reviewTitleAutomation(SpotTitleReviewRequest request) {
        return SpotTitleFeasibilityResponse.builder()
                .implementationRecommended(false)
                .status("REVIEW_ONLY")
                .reason("공공데이터포털 API 문서와 현재 코드만으로는 제목 서버사이드 부분검색, 동명이작 구분, category/media-type 필터 정확도를 아직 검증하지 못했습니다. 대량 페이지 스캔과 약한 문자열 추정은 자동 SpotContent 생성에 사용하지 않습니다.")
                .build();
    }

    private SpotSyncResponse syncPage(SpotSyncPage page, boolean externalApiCalled, int apiCallCountBefore) {
        Accumulator accumulator = new Accumulator();
        accumulator.externalApiCalled = externalApiCalled;
        accumulator.apiCallCount = Math.max(0, budgetService.getApiCallCount() - apiCallCountBefore);
        accumulator.total = page.getTotalCount();
        accumulator.pages = 1;
        for (SpotSyncPayload payload : page.getItems() == null ? List.<SpotSyncPayload>of() : page.getItems()) {
            accumulator.requested++;
            if (!isSavable(payload)) {
                accumulator.skipped++;
                continue;
            }
            Optional<Long> regionId = regionResolver.resolveRegionId(payload.getAddressKo());
            if (regionId.isEmpty()) {
                accumulator.skipped++;
                continue;
            }
            Spot spot = upsertSpot(payload, regionId.get(), accumulator);
            linkContent(payload, spot, accumulator);
        }
        return accumulator.toResponse();
    }

    private SpotSyncResponse emptyResponse(boolean stoppedByDailyLimit) {
        return SpotSyncResponse.builder()
                .externalApiCalled(false)
                .stoppedByDailyLimit(stoppedByDailyLimit)
                .apiCallCount(0)
                .build();
    }

    private Spot upsertSpot(SpotSyncPayload payload, Long regionId, Accumulator accumulator) {
        Optional<Spot> existing = findExistingSpot(payload);
        if (existing.isEmpty()) {
            Spot saved = spotRepository.save(toEntity(payload, regionId));
            accumulator.inserted++;
            return saved;
        }
        Spot spot = existing.get();
        boolean changed = spot.updateFromExternal(
                regionId,
                payload.getNameKo(),
                payload.getNameEn(),
                payload.getPlaceType(),
                payload.getCategory(),
                payload.getAddressKo(),
                payload.getAddressEn(),
                payload.getLatitude(),
                payload.getLongitude(),
                payload.getDescriptionKo(),
                payload.getDescriptionEn(),
                payload.getOpeningHours(),
                payload.getBreakTime(),
                payload.getClosedDaysKo(),
                payload.getClosedDaysEn(),
                payload.getTel(),
                payload.getTransportInfoKo(),
                payload.getTransportInfoEn(),
                payload.getImageUrl(),
                payload.getSource(),
                payload.getExternalId());
        if (changed) {
            accumulator.updated++;
        } else {
            accumulator.unchanged++;
        }
        return spot;
    }

    private Optional<Spot> findExistingSpot(SpotSyncPayload payload) {
        if (payload.getSource() != null && StringUtils.hasText(payload.getExternalId())) {
            Optional<Spot> byExternalId = spotRepository.findByExternalSourceAndExternalId(payload.getSource(), payload.getExternalId());
            if (byExternalId.isPresent()) {
                return byExternalId;
            }
        }
        return spotRepository.findFirstByNameKoAndAddressKoAndCategory(payload.getNameKo(), payload.getAddressKo(), payload.getCategory());
    }

    private void linkContent(SpotSyncPayload payload, Spot spot, Accumulator accumulator) {
        ContentCategory contentCategory = toContentCategory(payload.getCategory(), payload.getMediaType());
        if (contentCategory == null || !StringUtils.hasText(payload.getContentTitle())) {
            return;
        }
        Content content = contentRepository.findByTitleKoAndCategory(payload.getContentTitle(), contentCategory)
                .orElseGet(() -> contentRepository.save(Content.builder().titleKo(payload.getContentTitle()).category(contentCategory).build()));
        accumulator.contentsResolved++;
        if (!spotContentRepository.existsBySpotIdAndContentId(spot.getSpotId(), content.getContentId())) {
            spotContentRepository.save(SpotContent.builder().spotId(spot.getSpotId()).contentId(content.getContentId()).build());
            accumulator.linksCreated++;
        }
    }

    private ContentCategory toContentCategory(SpotCategory category, String mediaType) {
        if (category == SpotCategory.K_DRAMA) {
            return ContentCategory.DRAMA;
        }
        if (category == SpotCategory.K_MOVIE) {
            return ContentCategory.MOVIE;
        }
        if (category == SpotCategory.K_POP && StringUtils.hasText(mediaType) && "artist".equalsIgnoreCase(mediaType.trim())) {
            return ContentCategory.ARTIST;
        }
        return null;
    }

    private Spot toEntity(SpotSyncPayload payload, Long regionId) {
        return Spot.builder()
                .regionId(regionId)
                .nameKo(payload.getNameKo())
                .nameEn(payload.getNameEn())
                .category(payload.getCategory())
                .placeType(PlaceType.from(payload.getPlaceType()))
                .descriptionKo(payload.getDescriptionKo())
                .descriptionEn(payload.getDescriptionEn())
                .openingHours(payload.getOpeningHours())
                .breakTime(payload.getBreakTime())
                .closedDaysKo(payload.getClosedDaysKo())
                .closedDaysEn(payload.getClosedDaysEn())
                .tel(payload.getTel())
                .addressKo(payload.getAddressKo())
                .addressEn(payload.getAddressEn())
                .transportInfoKo(payload.getTransportInfoKo())
                .transportInfoEn(payload.getTransportInfoEn())
                .latitude(payload.getLatitude())
                .longitude(payload.getLongitude())
                .imageUrl(payload.getImageUrl())
                .externalSource(payload.getSource())
                .externalId(payload.getExternalId())
                .avgRating(0.0)
                .reviewCount(0L)
                .savedCount(0L)
                .build();
    }

    private boolean isSavable(SpotSyncPayload payload) {
        return payload != null
                && StringUtils.hasText(payload.getNameKo())
                && StringUtils.hasText(payload.getAddressKo())
                && payload.getCategory() != null
                && payload.getLatitude() != null
                && payload.getLongitude() != null;
    }

    private void validatePaging(int page, int size) {
        if (page < 1 || size < 1) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
    }

    private void validateMediaCategory(SpotCategory category) {
        if (category != null && !MEDIA_CATEGORIES.contains(category)) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
    }

    private static class Accumulator {
        private int requested;
        private boolean externalApiCalled;
        private int apiCallCount;
        private int total;
        private int pages;
        private int inserted;
        private int updated;
        private int unchanged;
        private int skipped;
        private int failed;
        private boolean stoppedByDailyLimit;
        private int contentsResolved;
        private int linksCreated;

        private SpotSyncResponse toResponse() {
            return SpotSyncResponse.builder()
                    .externalApiCalled(externalApiCalled)
                    .apiCallCount(apiCallCount)
                    .requested(requested)
                    .total(total)
                    .pages(pages)
                    .inserted(inserted)
                    .updated(updated)
                    .unchanged(unchanged)
                    .skipped(skipped)
                    .failed(failed)
                    .stoppedByDailyLimit(stoppedByDailyLimit)
                    .contentsResolved(contentsResolved)
                    .linksCreated(linksCreated)
                    .build();
        }
    }
}
