package com.Wavey.WaveyService.domain.spot.converter;

import com.Wavey.WaveyService.domain.spot.dto.request.SpotCreateRequest;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotUpdateRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotResponse;
import com.Wavey.WaveyService.domain.spot.entity.Spot;

import org.springframework.util.StringUtils;

public final class SpotConverter {

    private SpotConverter() {}

    public static Spot toEntity(SpotCreateRequest request) {
        return Spot.builder()
                .regionId(request.getRegionId())
                .name(request.getName().trim())
                .category(request.getCategory())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .description(request.getDescription())
                .openingHours(request.getOpeningHours())
                .closedDays(request.getClosedDays())
                .tel(request.getTel())
                .thumbnailUrl(request.getThumbnailUrl())
                .sourceType(request.getSourceType())
                .externalContentId(normalizeNullableText(request.getExternalContentId()))
                .avgRating(0.0)
                .build();
    }

    public static SpotResponse toResponse(Spot spot) {
        return SpotResponse.builder()
                .spotId(spot.getSpotId())
                .regionId(spot.getRegionId())
                .mediaType(spot.getMediaType())
                .title(spot.getTitle())
                .name(spot.getName())
                .placeType(spot.getPlaceType())
                .category(spot.getCategory())
                .address(spot.getAddress())
                .latitude(spot.getLatitude())
                .longitude(spot.getLongitude())
                .description(spot.getDescription())
                .openingHours(spot.getOpeningHours())
                .breakTime(spot.getBreakTime())
                .closedDays(spot.getClosedDays())
                .tel(spot.getTel())
                .thumbnailUrl(spot.getThumbnailUrl())
                .sourceUpdatedAt(spot.getSourceUpdatedAt())
                .sourceType(spot.getSourceType())
                .externalContentId(spot.getExternalContentId())
                .avgRating(spot.getAvgRating())
                .createdAt(spot.getCreatedAt())
                .updatedAt(spot.getUpdatedAt())
                .build();
    }

    public static SpotListResponse toListResponse(Spot spot) {
        return SpotListResponse.builder()
                .spotId(spot.getSpotId())
                .name(spot.getName())
                .category(spot.getCategory())
                .address(spot.getAddress())
                .latitude(spot.getLatitude())
                .longitude(spot.getLongitude())
                .thumbnailUrl(spot.getThumbnailUrl())
                .avgRating(spot.getAvgRating())
                .build();
    }

    public static void updateEntity(Spot spot, SpotUpdateRequest request) {
        spot.update(
                spot.getMediaType(),
                spot.getTitle(),
                request.getName() != null ? request.getName().trim() : spot.getName(),
                spot.getPlaceType(),
                request.getCategory() != null ? request.getCategory() : spot.getCategory(),
                request.getAddress() != null ? request.getAddress() : spot.getAddress(),
                request.getLatitude() != null ? request.getLatitude() : spot.getLatitude(),
                request.getLongitude() != null ? request.getLongitude() : spot.getLongitude(),
                request.getDescription() != null ? request.getDescription() : spot.getDescription(),
                request.getOpeningHours() != null
                        ? request.getOpeningHours()
                        : spot.getOpeningHours(),
                spot.getBreakTime(),
                request.getClosedDays() != null ? request.getClosedDays() : spot.getClosedDays(),
                request.getTel() != null ? request.getTel() : spot.getTel(),
                request.getThumbnailUrl() != null
                        ? request.getThumbnailUrl()
                        : spot.getThumbnailUrl(),
                spot.getSourceUpdatedAt());
    }

    private static String normalizeNullableText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
