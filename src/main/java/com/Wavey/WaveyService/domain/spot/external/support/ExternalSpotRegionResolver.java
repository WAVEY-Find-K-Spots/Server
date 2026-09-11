package com.Wavey.WaveyService.domain.spot.external.support;

import com.Wavey.WaveyService.domain.region.entity.Region;
import com.Wavey.WaveyService.domain.region.repository.RegionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExternalSpotRegionResolver {

    private static final Map<String, String> REGION_NAME_ALIASES = Map.ofEntries(
            Map.entry("서울특별시", "서울"),
            Map.entry("서울시", "서울"),
            Map.entry("부산광역시", "부산"),
            Map.entry("대구광역시", "대구"),
            Map.entry("인천광역시", "인천"),
            Map.entry("광주광역시", "광주"),
            Map.entry("대전광역시", "대전"),
            Map.entry("울산광역시", "울산"),
            Map.entry("세종특별자치시", "세종"),
            Map.entry("세종시", "세종"),
            Map.entry("경기도", "경기"),
            Map.entry("강원특별자치도", "강원"),
            Map.entry("강원도", "강원"),
            Map.entry("충청북도", "충북"),
            Map.entry("충청남도", "충남"),
            Map.entry("전북특별자치도", "전북"),
            Map.entry("전라북도", "전북"),
            Map.entry("전라남도", "전남"),
            Map.entry("경상북도", "경북"),
            Map.entry("경상남도", "경남"),
            Map.entry("제주특별자치도", "제주"),
            Map.entry("제주도", "제주")
    );

    private final RegionRepository regionRepository;

    public Optional<Long> resolveRegionId(String address) {
        String regionName = extractRegionName(address);

        if (!StringUtils.hasText(regionName)) {
            return Optional.empty();
        }

        String canonicalName = REGION_NAME_ALIASES.getOrDefault(regionName, regionName);

        return regionRepository.findByNameKo(canonicalName)
                .map(Region::getRegionId);
    }

    private String extractRegionName(String address) {
        if (!StringUtils.hasText(address)) {
            return null;
        }

        return address.trim().split("\\s+")[0];
    }
}