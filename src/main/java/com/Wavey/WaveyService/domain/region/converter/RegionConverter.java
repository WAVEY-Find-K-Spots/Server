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
                .nameKo(request.getNameKo().trim())
                .nameEn(request.getNameEn().trim())
                .build();
    }

    public static RegionResponse toResponse(Region region) {
        return RegionResponse.builder()
                .regionId(region.getRegionId())
                .nameKo(region.getNameKo())
                .nameEn(region.getNameEn())
                .createdAt(region.getCreatedAt())
                .updatedAt(region.getUpdatedAt())
                .build();
    }

    public static void updateEntity(Region region, RegionUpdateRequest request) {
        String nameKo =
                StringUtils.hasText(request.getNameKo())
                        ? request.getNameKo().trim()
                        : region.getNameKo();
        String nameEn =
                StringUtils.hasText(request.getNameEn())
                        ? request.getNameEn().trim()
                        : region.getNameEn();

        region.update(nameKo, nameEn);
    }
}
