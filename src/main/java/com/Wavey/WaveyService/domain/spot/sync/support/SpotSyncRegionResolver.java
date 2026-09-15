package com.Wavey.WaveyService.domain.spot.sync.support;

import com.Wavey.WaveyService.domain.region.entity.Region;
import com.Wavey.WaveyService.domain.region.repository.RegionRepository;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class SpotSyncRegionResolver {

    private static final Map<String, String> REGION_EN = Map.ofEntries(
            Map.entry("서울", "Seoul"), Map.entry("서울특별시", "Seoul"), Map.entry("서울시", "Seoul"),
            Map.entry("부산", "Busan"), Map.entry("부산광역시", "Busan"),
            Map.entry("대구", "Daegu"), Map.entry("대구광역시", "Daegu"),
            Map.entry("인천", "Incheon"), Map.entry("인천광역시", "Incheon"),
            Map.entry("광주", "Gwangju"), Map.entry("광주광역시", "Gwangju"),
            Map.entry("대전", "Daejeon"), Map.entry("대전광역시", "Daejeon"),
            Map.entry("울산", "Ulsan"), Map.entry("울산광역시", "Ulsan"),
            Map.entry("세종", "Sejong"), Map.entry("세종특별자치시", "Sejong"),
            Map.entry("경기도", "Gyeonggi-do"),
            Map.entry("강원도", "Gangwon-do"), Map.entry("강원특별자치도", "Gangwon-do"),
            Map.entry("충청북도", "Chungcheongbuk-do"), Map.entry("충북", "Chungcheongbuk-do"),
            Map.entry("충청남도", "Chungcheongnam-do"), Map.entry("충남", "Chungcheongnam-do"),
            Map.entry("전라북도", "Jeollabuk-do"), Map.entry("전북특별자치도", "Jeollabuk-do"), Map.entry("전북", "Jeollabuk-do"),
            Map.entry("전라남도", "Jeollanam-do"), Map.entry("전남", "Jeollanam-do"),
            Map.entry("경상북도", "Gyeongsangbuk-do"), Map.entry("경북", "Gyeongsangbuk-do"),
            Map.entry("경상남도", "Gyeongsangnam-do"), Map.entry("경남", "Gyeongsangnam-do"),
            Map.entry("제주", "Jeju-do"), Map.entry("제주도", "Jeju-do"), Map.entry("제주특별자치도", "Jeju-do")
    );

    private final RegionRepository regionRepository;

    public Optional<Long> resolveRegionId(String address) {
        if (!StringUtils.hasText(address)) {
            return Optional.empty();
        }
        String regionName = address.trim().split("\\s+")[0];
        String normalizedRegionName = normalizeRegionName(regionName);
        String nameEn = REGION_EN.getOrDefault(regionName, REGION_EN.getOrDefault(normalizedRegionName, normalizedRegionName));
        Region region = regionRepository.findByNameKo(regionName)
                .or(() -> regionRepository.findByNameKo(normalizedRegionName))
                .orElseGet(() -> regionRepository.save(Region.builder().nameKo(normalizedRegionName).nameEn(nameEn).build()));
        return Optional.of(region.getRegionId());
    }

    private String normalizeRegionName(String regionName) {
        if (!StringUtils.hasText(regionName)) {
            return regionName;
        }
        if (regionName.endsWith("특별시") || regionName.endsWith("광역시") || regionName.endsWith("특별자치시")) {
            return regionName
                    .replace("특별시", "")
                    .replace("광역시", "")
                    .replace("특별자치시", "");
        }
        if ("제주특별자치도".equals(regionName) || "제주도".equals(regionName)) {
            return "제주";
        }
        if ("강원특별자치도".equals(regionName)) {
            return "강원도";
        }
        if ("전북특별자치도".equals(regionName)) {
            return "전라북도";
        }
        return regionName;
    }
}
