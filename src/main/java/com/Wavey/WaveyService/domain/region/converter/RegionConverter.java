package com.Wavey.WaveyService.domain.region.converter;

import com.Wavey.WaveyService.domain.region.dto.request.RegionCreateRequest;
import com.Wavey.WaveyService.domain.region.dto.request.RegionUpdateRequest;
import com.Wavey.WaveyService.domain.region.dto.response.RegionResponse;
import com.Wavey.WaveyService.domain.region.entity.Region;

import org.springframework.util.StringUtils;

public class RegionConverter {

    private RegionConverter() {}

    public static Region toEntity(RegionCreateRequest request) {
        return Region.builder()
                .nameKo(request.getName().trim())
                .code(request.getCode().trim())
                .build();
    }

    public static RegionResponse toResponse(Region region) {
        return RegionResponse.builder()
                .regionId(region.getRegionId())
                .nameEn(region.getNameEn())
                .name(region.getNameKo())
                .code(region.getCode())
                .createdAt(region.getCreatedAt())
                .updatedAt(region.getUpdatedAt())
                .build();
    }

    public static void updateEntity(Region region, RegionUpdateRequest request) {
        String name =
                StringUtils.hasText(request.getName())
                        ? request.getName().trim()
                        : region.getNameKo();
        String code =
                StringUtils.hasText(request.getCode())
                        ? request.getCode().trim()
                        : region.getCode();

        region.update(
                name,
                code,
                request.getLatitude(),
                request.getLongitude());
    }
}
