package com.Wavey.WaveyService.domain.spot.service;

import com.Wavey.WaveyService.domain.spot.converter.SpotConverter;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotSearchRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotPageResponse;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.SortBy;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.spot.specification.SpotSearchSpecification;
import com.Wavey.WaveyService.domain.spot.support.SpotGeoSupport;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotSearchService {

    private static final int PAGE_SIZE = 6;

    private final SpotRepository spotRepository;
    private final SpotConverter spotConverter;

    public SpotPageResponse search(
            SpotSearchRequest request,
            Long userId
    ) {
        validateLocation(request);

        Page<Spot> page =
                spotRepository.findAll(
                        SpotSearchSpecification.from(request),
                        PageRequest.of(
                                request.page(),
                                PAGE_SIZE,
                                resolveSort(request.sort())
                        )
                );

        Set<Long> savedSpotIds =
                findSavedSpotIds(
                        userId,
                        page.getContent()
                );

        Locale locale =
                LocaleContextHolder.getLocale();

        List<SpotListResponse> spots =
                page.getContent()
                        .stream()
                        .map(
                                spot ->
                                        spotConverter.toListResponse(
                                                spot,
                                                savedSpotIds.contains(
                                                        spot.getSpotId()
                                                ),
                                                SpotGeoSupport.meters(
                                                        request.latitude(),
                                                        request.longitude(),
                                                        spot
                                                ),
                                                locale
                                        )
                        )
                        .toList();

        return new SpotPageResponse(
                spots,
                page.getNumber(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    private Sort resolveSort(SortBy sort) {
        return switch (sort) {

            case POPULAR ->
                    Sort.by(
                            Sort.Order.desc("savedCount"),
                            Sort.Order.desc("reviewCount"),
                            Sort.Order.desc("id")
                    );

            case RATING ->
                    Sort.by(
                            Sort.Order.desc("avgRating"),
                            Sort.Order.desc("reviewCount"),
                            Sort.Order.desc("id")
                    );

            case LATEST ->
                    Sort.by(
                            Sort.Order.desc("createdAt"),
                            Sort.Order.desc("id")
                    );

            case DISTANCE ->
                    Sort.unsorted();
        };
    }

    private void validateLocation(
            SpotSearchRequest request
    ) {
        boolean hasLatitude =
                request.latitude() != null;

        boolean hasLongitude =
                request.longitude() != null;

        if (hasLatitude != hasLongitude) {
            throw new CustomException(
                    ErrorCode.COMMON_INVALID_PARAMETER
            );
        }

        if ((request.radiusMeters() != null
                || request.sort() == SortBy.DISTANCE)
                && !hasLatitude) {

            throw new CustomException(
                    ErrorCode.COMMON_INVALID_PARAMETER
            );
        }
    }

    private Set<Long> findSavedSpotIds(
            Long userId,
            List<Spot> spots
    ) {
        return Set.of();
    }
}