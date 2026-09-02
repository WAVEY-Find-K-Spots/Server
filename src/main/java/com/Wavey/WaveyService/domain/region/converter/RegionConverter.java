package com.Wavey.WaveyService.domain.region.converter;

import com.Wavey.WaveyService.domain.region.dto.request.RegionCreateRequest;
import com.Wavey.WaveyService.domain.region.dto.request.RegionUpdateRequest;
import com.Wavey.WaveyService.domain.region.dto.response.RegionResponse;
import com.Wavey.WaveyService.domain.region.entity.Region;
import org.springframework.util.StringUtils;

public class RegionConverter {

    private RegionConverter() {
    }

    public static Region toEntity(RegionCreateRequest request) {
        return Region.builder()
                .name(request.getName().trim())
                .code(request.getCode().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    public static RegionResponse toResponse(Region region) {
        return RegionResponse.builder()
                .regionId(region.getRegionId())
                .name(region.getName())
                .code(region.getCode())
                .latitude(region.getLatitude())
                .longitude(region.getLongitude())
                .createdAt(region.getCreatedAt())
                .updatedAt(region.getUpdatedAt())
                .build();
    }

    public static void updateEntity(Region region, RegionUpdateRequest request) {
        String name = StringUtils.hasText(request.getName()) ? request.getName().trim() : region.getName();
        String code = StringUtils.hasText(request.getCode()) ? request.getCode().trim() : region.getCode();

        region.update(
                name,
                code,
                request.getLatitude() != null ? request.getLatitude() : region.getLatitude(),
                request.getLongitude() != null ? request.getLongitude() : region.getLongitude()
        );
    }
}
