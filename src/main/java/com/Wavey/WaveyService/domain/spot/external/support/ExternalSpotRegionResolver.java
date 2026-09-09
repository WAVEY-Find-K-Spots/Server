package com.Wavey.WaveyService.domain.spot.external.support;

import com.Wavey.WaveyService.domain.region.entity.Region;
import com.Wavey.WaveyService.domain.region.repository.RegionRepository;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ExternalSpotRegionResolver {

    private static final Map<String, String> REGION_CODES = Map.ofEntries(
            Map.entry("서울특별시", "SEOUL"),
            Map.entry("서울시", "SEOUL"),
            Map.entry("서울", "SEOUL"),
            Map.entry("부산광역시", "BUSAN"),
            Map.entry("부산", "BUSAN"),
            Map.entry("대구광역시", "DAEGU"),
            Map.entry("대구", "DAEGU"),
            Map.entry("인천광역시", "INCHEON"),
            Map.entry("인천", "INCHEON"),
            Map.entry("광주광역시", "GWANGJU"),
            Map.entry("광주", "GWANGJU"),
            Map.entry("대전광역시", "DAEJEON"),
            Map.entry("대전", "DAEJEON"),
            Map.entry("울산광역시", "ULSAN"),
            Map.entry("울산", "ULSAN"),
            Map.entry("세종특별자치시", "SEJONG"),
            Map.entry("세종시", "SEJONG"),
            Map.entry("세종", "SEJONG"),
            Map.entry("경기도", "GYEONGGI"),
            Map.entry("강원특별자치도", "GANGWON"),
            Map.entry("강원도", "GANGWON"),
            Map.entry("충청북도", "CHUNGBUK"),
            Map.entry("충북", "CHUNGBUK"),
            Map.entry("충청남도", "CHUNGNAM"),
            Map.entry("충남", "CHUNGNAM"),
            Map.entry("전북특별자치도", "JEONBUK"),
            Map.entry("전라북도", "JEONBUK"),
            Map.entry("전북", "JEONBUK"),
            Map.entry("전라남도", "JEONNAM"),
            Map.entry("전남", "JEONNAM"),
            Map.entry("경상북도", "GYEONGBUK"),
            Map.entry("경북", "GYEONGBUK"),
            Map.entry("경상남도", "GYEONGNAM"),
            Map.entry("경남", "GYEONGNAM"),
            Map.entry("제주특별자치도", "JEJU"),
            Map.entry("제주도", "JEJU"),
            Map.entry("제주", "JEJU")
    );

    private final RegionRepository regionRepository;

    public Optional<Long> resolveRegionId(String address) {
        String regionName = extractRegionName(address);
        if (!StringUtils.hasText(regionName)) {
            return Optional.empty();
        }

        String regionCode = resolveRegionCode(regionName);
        Region region = regionRepository.findByCode(regionCode)
                .or(() -> regionRepository.findByNameKo(regionName))
                .orElseGet(() -> regionRepository.save(Region.builder()
                        .nameKo(regionName)
                        .code(regionCode)
                        .build()));

        return Optional.of(region.getRegionId());
    }

    private String extractRegionName(String address) {
        if (!StringUtils.hasText(address)) {
            return null;
        }
        return address.trim().split("\\s+")[0];
    }

    private String resolveRegionCode(String regionName) {
        String code = REGION_CODES.get(regionName);
        if (code != null) {
            return code;
        }
        return "REGION_" + regionName.toUpperCase(Locale.ROOT).hashCode();
    }
}
