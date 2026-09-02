package com.Wavey.WaveyService.domain.spot.external.service;

import com.Wavey.WaveyService.domain.spot.dto.response.SpotSyncResponse;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;
import com.Wavey.WaveyService.domain.spot.external.client.MediaLocationSpotClient;
import com.Wavey.WaveyService.domain.spot.external.client.TourApiSpotClient;
import com.Wavey.WaveyService.domain.spot.external.dto.ExternalSpotPage;
import com.Wavey.WaveyService.domain.spot.external.dto.ExternalSpotPayload;
import com.Wavey.WaveyService.domain.spot.external.support.ExternalSpotRegionResolver;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SpotExternalSyncServiceImpl implements SpotExternalSyncService {

    private static final List<SpotCategory> MEDIA_LOCATION_CATEGORIES = List.of(
            SpotCategory.K_DRAMA,
            SpotCategory.K_POP,
            SpotCategory.K_MOVIE
    );

    private final TourApiSpotClient tourApiSpotClient;
    private final MediaLocationSpotClient mediaLocationSpotClient;
    private final ExternalSpotRegionResolver regionResolver;
    private final SpotRepository spotRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public SpotSyncResponse syncHeritageSpots(int pageNo, int numOfRows) {
        validatePaging(pageNo, numOfRows);
        ExternalSpotPage page = tourApiSpotClient.fetchHeritageSpotPage(pageNo, numOfRows);
        return syncPage(page, false);
    }

    @Override
    public SpotSyncResponse syncAllHeritageSpots(int numOfRows) {
        validatePaging(1, numOfRows);
        return syncAllPages(
                pageNo -> tourApiSpotClient.fetchHeritageSpotPage(pageNo, numOfRows),
                false
        );
    }

    @Override
    public SpotSyncResponse syncMediaLocationSpots(SpotCategory category, int page, int perPage) {
        validatePaging(page, perPage);
        validateMediaCategory(category);
        ExternalSpotPage spotPage = mediaLocationSpotClient.fetchMediaLocationSpotPage(category, page, perPage);
        return syncPage(spotPage, true);
    }

    @Override
    public SpotSyncResponse syncAllMediaLocationSpots(SpotCategory category, int perPage) {
        validatePaging(1, perPage);
        validateMediaCategory(category);

        List<SpotCategory> categories = category == null ? MEDIA_LOCATION_CATEGORIES : List.of(category);
        SyncAccumulator accumulator = new SyncAccumulator();

        for (SpotCategory targetCategory : categories) {
            accumulator.add(syncAllPages(
                    page -> mediaLocationSpotClient.fetchMediaLocationSpotPage(targetCategory, page, perPage),
                    false
            ));
        }

        return accumulator.toResponse();
    }

    @Override
    @Transactional
    public SpotSyncResponse fillMediaLocationThumbnails(SpotCategory category, int limit) {
        validatePaging(1, limit);
        validateMediaCategory(category);

        List<SpotCategory> categories = category == null ? MEDIA_LOCATION_CATEGORIES : List.of(category);
        List<Spot> spots = spotRepository.findThumbnailTargets(
                SpotSourceType.MEDIA_LOCATION_DATA,
                categories,
                PageRequest.of(0, limit)
        );
        Map<String, Optional<String>> thumbnailCache = new HashMap<>();

        int updatedCount = 0;
        for (Spot spot : spots) {
            Optional<String> thumbnailUrl = findThumbnailWithCache(toPayload(spot), thumbnailCache);
            if (thumbnailUrl.isEmpty()) {
                continue;
            }

            spot.updateThumbnailUrl(thumbnailUrl.get());
            updatedCount++;
        }

        return SpotSyncResponse.builder()
                .requestedCount(spots.size())
                .updatedCount(updatedCount)
                .skippedCount(spots.size() - updatedCount)
                .build();
    }

    private SpotSyncResponse syncAllPages(
            Function<Integer, ExternalSpotPage> pageFetcher,
            boolean enrichMediaThumbnail
    ) {
        SyncAccumulator accumulator = new SyncAccumulator();
        int pageNo = 1;
        int totalCount = 0;
        int pageCount = 0;

        while (true) {
            ExternalSpotPage page = pageFetcher.apply(pageNo);
            List<ExternalSpotPayload> payloads = page.getItems() == null ? List.of() : page.getItems();
            if (enrichMediaThumbnail) {
                payloads = enrichMediaThumbnails(payloads);
            }

            accumulator.add(syncPayloadsInTransaction(payloads));
            totalCount = page.getTotalCount();
            pageCount++;

            if (!page.hasNextPage()) {
                break;
            }
            pageNo++;
        }

        return accumulator.toResponse(totalCount, pageCount);
    }

    private SpotSyncResponse syncPage(ExternalSpotPage page, boolean enrichMediaThumbnail) {
        List<ExternalSpotPayload> payloads = page.getItems() == null ? List.of() : page.getItems();
        if (enrichMediaThumbnail) {
            payloads = enrichMediaThumbnails(payloads);
        }

        SyncResult result = syncPayloadsInTransaction(payloads);

        return SpotSyncResponse.builder()
                .requestedCount(result.requestedCount())
                .totalCount(page.getTotalCount())
                .pageCount(1)
                .savedCount(result.savedCount())
                .updatedCount(result.updatedCount())
                .unchangedCount(result.unchangedCount())
                .skippedCount(result.skippedCount())
                .build();
    }

    private SyncResult syncPayloadsInTransaction(List<ExternalSpotPayload> payloads) {
        SyncResult result = transactionTemplate.execute(status -> syncPayloads(payloads));
        if (result == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    private SyncResult syncPayloads(List<ExternalSpotPayload> payloads) {
        int savedCount = 0;
        int updatedCount = 0;
        int unchangedCount = 0;
        int skippedCount = 0;
        Map<String, Spot> existingSpots = findExistingSpots(payloads);
        Set<String> requestedExternalKeys = new HashSet<>();
        Map<String, Optional<Long>> regionCache = new HashMap<>();

        for (ExternalSpotPayload payload : payloads) {
            if (!isSavable(payload)) {
                skippedCount++;
                continue;
            }

            String externalKey = externalKey(payload.getSourceType(), payload.getExternalContentId());
            if (!requestedExternalKeys.add(externalKey)) {
                skippedCount++;
                continue;
            }

            Optional<Long> regionId = resolveRegionIdWithCache(payload.getAddress(), regionCache);
            if (regionId.isEmpty()) {
                skippedCount++;
                continue;
            }

            Spot existingSpot = existingSpots.get(externalKey);
            if (existingSpot == null) {
                spotRepository.save(toEntity(payload, regionId.get()));
                savedCount++;
                continue;
            }

            boolean changed = existingSpot.updateFromExternal(
                    regionId.get(),
                    payload.getName(),
                    payload.getCategory(),
                    payload.getAddress(),
                    payload.getLatitude(),
                    payload.getLongitude(),
                    payload.getDescription(),
                    payload.getOpeningHours(),
                    payload.getClosedDays(),
                    payload.getTel(),
                    payload.getThumbnailUrl()
            );

            if (changed) {
                updatedCount++;
            } else {
                unchangedCount++;
            }
        }

        return new SyncResult(
                payloads.size(),
                savedCount,
                updatedCount,
                unchangedCount,
                skippedCount
        );
    }

    private List<ExternalSpotPayload> enrichMediaThumbnails(List<ExternalSpotPayload> payloads) {
        Map<String, Optional<String>> thumbnailCache = new HashMap<>();

        return payloads.stream()
                .map(payload -> enrichMediaThumbnail(payload, thumbnailCache))
                .toList();
    }

    private ExternalSpotPayload enrichMediaThumbnail(
            ExternalSpotPayload payload,
            Map<String, Optional<String>> thumbnailCache
    ) {
        if (payload == null || StringUtils.hasText(payload.getThumbnailUrl())) {
            return payload;
        }

        return findThumbnailWithCache(payload, thumbnailCache)
                .map(payload::withThumbnailUrl)
                .orElse(payload);
    }

    private Optional<String> findThumbnailWithCache(
            ExternalSpotPayload payload,
            Map<String, Optional<String>> thumbnailCache
    ) {
        String cacheKey = thumbnailCacheKey(payload);
        if (!StringUtils.hasText(cacheKey)) {
            return Optional.empty();
        }

        return thumbnailCache.computeIfAbsent(cacheKey, ignored -> tourApiSpotClient.findBestThumbnail(payload));
    }

    private Map<String, Spot> findExistingSpots(List<ExternalSpotPayload> payloads) {
        Map<SpotSourceType, Set<String>> externalIdsBySource = new EnumMap<>(SpotSourceType.class);

        payloads.stream()
                .filter(this::isSavable)
                .forEach(payload -> externalIdsBySource
                        .computeIfAbsent(payload.getSourceType(), ignored -> new HashSet<>())
                        .add(payload.getExternalContentId()));

        Map<String, Spot> existingSpots = new HashMap<>();
        externalIdsBySource.forEach((sourceType, externalContentIds) ->
                spotRepository.findExternalSpots(sourceType, externalContentIds)
                        .forEach(spot -> existingSpots.putIfAbsent(
                                externalKey(spot.getSourceType(), spot.getExternalContentId()),
                                spot
                        ))
        );

        return existingSpots;
    }

    private Optional<Long> resolveRegionIdWithCache(
            String address,
            Map<String, Optional<Long>> regionCache
    ) {
        String cacheKey = regionCacheKey(address);
        if (!StringUtils.hasText(cacheKey)) {
            return Optional.empty();
        }

        return regionCache.computeIfAbsent(cacheKey, ignored -> regionResolver.resolveRegionId(address));
    }

    private ExternalSpotPayload toPayload(Spot spot) {
        return ExternalSpotPayload.builder()
                .name(spot.getName())
                .category(spot.getCategory())
                .address(spot.getAddress())
                .latitude(spot.getLatitude())
                .longitude(spot.getLongitude())
                .sourceType(spot.getSourceType())
                .externalContentId(spot.getExternalContentId())
                .build();
    }

    private String externalKey(SpotSourceType sourceType, String externalContentId) {
        if (sourceType == null || !StringUtils.hasText(externalContentId)) {
            return "";
        }
        return sourceType.name() + ":" + externalContentId.trim();
    }

    private String thumbnailCacheKey(ExternalSpotPayload payload) {
        if (payload == null || !StringUtils.hasText(payload.getName())) {
            return "";
        }
        return normalizeCacheValue(payload.getName()) + "|" + normalizeCacheValue(payload.getAddress());
    }

    private String regionCacheKey(String address) {
        if (!StringUtils.hasText(address)) {
            return "";
        }
        return address.trim().split("\\s+")[0];
    }

    private String normalizeCacheValue(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "")
                .trim();
    }

    private Spot toEntity(ExternalSpotPayload payload, Long regionId) {
        return Spot.builder()
                .regionId(regionId)
                .name(payload.getName())
                .category(payload.getCategory())
                .address(payload.getAddress())
                .latitude(payload.getLatitude())
                .longitude(payload.getLongitude())
                .description(payload.getDescription())
                .openingHours(payload.getOpeningHours())
                .closedDays(payload.getClosedDays())
                .tel(payload.getTel())
                .thumbnailUrl(payload.getThumbnailUrl())
                .sourceType(payload.getSourceType())
                .externalContentId(payload.getExternalContentId())
                .avgRating(0.0)
                .build();
    }

    private boolean isSavable(ExternalSpotPayload payload) {
        return payload != null
                && StringUtils.hasText(payload.getName())
                && payload.getCategory() != null
                && payload.getLatitude() != null
                && payload.getLongitude() != null
                && payload.getSourceType() != null
                && StringUtils.hasText(payload.getExternalContentId());
    }

    private void validatePaging(int page, int size) {
        if (page < 1 || size < 1) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
    }

    private void validateMediaCategory(SpotCategory category) {
        if (category != null && !MEDIA_LOCATION_CATEGORIES.contains(category)) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
    }

    private record SyncResult(
            int requestedCount,
            int savedCount,
            int updatedCount,
            int unchangedCount,
            int skippedCount
    ) {
    }

    private static class SyncAccumulator {

        private int requestedCount;
        private int totalCount;
        private int pageCount;
        private int savedCount;
        private int updatedCount;
        private int unchangedCount;
        private int skippedCount;

        private void add(SpotSyncResponse response) {
            this.requestedCount += response.getRequestedCount();
            this.totalCount += response.getTotalCount();
            this.pageCount += response.getPageCount();
            this.savedCount += response.getSavedCount();
            this.updatedCount += response.getUpdatedCount();
            this.unchangedCount += response.getUnchangedCount();
            this.skippedCount += response.getSkippedCount();
        }

        private void add(SyncResult result) {
            this.requestedCount += result.requestedCount();
            this.savedCount += result.savedCount();
            this.updatedCount += result.updatedCount();
            this.unchangedCount += result.unchangedCount();
            this.skippedCount += result.skippedCount();
        }

        private SpotSyncResponse toResponse() {
            return toResponse(totalCount, pageCount);
        }

        private SpotSyncResponse toResponse(int totalCount, int pageCount) {
            return SpotSyncResponse.builder()
                    .requestedCount(requestedCount)
                    .totalCount(totalCount)
                    .pageCount(pageCount)
                    .savedCount(savedCount)
                    .updatedCount(updatedCount)
                    .unchangedCount(unchangedCount)
                    .skippedCount(skippedCount)
                    .build();
        }
    }
}
