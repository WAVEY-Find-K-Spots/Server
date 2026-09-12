package com.Wavey.WaveyService.domain.spot.converter;

import com.Wavey.WaveyService.domain.spot.dto.request.SpotCreateRequest;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotUpdateRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotNearbyResponse;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotResponse;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.service.SpotLocalizationService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SpotConverter {

    private final SpotLocalizationService localizationService;

    public Spot toEntity(SpotCreateRequest request) {
        return Spot.builder()
                .regionId(request.regionId())
                .nameKo(request.nameKo())
                .nameEn(request.nameEn())
                .category(request.category())
                .placeType(request.placeType())
                .descriptionKo(request.descriptionKo())
                .descriptionEn(request.descriptionEn())
                .openingHours(request.openingHours())
                .breakTime(request.breakTime())
                .closedDaysKo(request.closedDaysKo())
                .closedDaysEn(request.closedDaysEn())
                .tel(request.tel())
                .addressKo(request.addressKo())
                .addressEn(request.addressEn())
                .transportInfoKo(request.transportInfoKo())
                .transportInfoEn(request.transportInfoEn())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .imageUrl(request.imageUrl())
                .build();
    }

    public void updateEntity(
            Spot spot,
            SpotUpdateRequest request
    ) {
        spot.update(
                request.regionId() != null
                        ? request.regionId()
                        : spot.getRegionId(),

                request.nameKo() != null
                        ? request.nameKo()
                        : spot.getNameKo(),

                request.nameEn() != null
                        ? request.nameEn()
                        : spot.getNameEn(),

                request.category() != null
                        ? request.category()
                        : spot.getCategory(),

                request.placeType() != null
                        ? request.placeType()
                        : spot.getPlaceType(),

                request.descriptionKo() != null
                        ? request.descriptionKo()
                        : spot.getDescriptionKo(),

                request.descriptionEn() != null
                        ? request.descriptionEn()
                        : spot.getDescriptionEn(),

                request.openingHours() != null
                        ? request.openingHours()
                        : spot.getOpeningHours(),

                request.breakTime() != null
                        ? request.breakTime()
                        : spot.getBreakTime(),

                request.closedDaysKo() != null
                        ? request.closedDaysKo()
                        : spot.getClosedDaysKo(),

                request.closedDaysEn() != null
                        ? request.closedDaysEn()
                        : spot.getClosedDaysEn(),

                request.tel() != null
                        ? request.tel()
                        : spot.getTel(),

                request.addressKo() != null
                        ? request.addressKo()
                        : spot.getAddressKo(),

                request.addressEn() != null
                        ? request.addressEn()
                        : spot.getAddressEn(),

                request.transportInfoKo() != null
                        ? request.transportInfoKo()
                        : spot.getTransportInfoKo(),

                request.transportInfoEn() != null
                        ? request.transportInfoEn()
                        : spot.getTransportInfoEn(),

                request.latitude() != null
                        ? request.latitude()
                        : spot.getLatitude(),

                request.longitude() != null
                        ? request.longitude()
                        : spot.getLongitude(),

                request.imageUrl() != null
                        ? request.imageUrl()
                        : spot.getImageUrl()
        );
    }

    public SpotResponse toResponse(
            Spot spot,
            boolean saved,
            Locale locale
    ) {
        return new SpotResponse(
                spot.getSpotId(),

                localizationService.localize(
                        spot.getNameKo(),
                        spot.getNameEn(),
                        locale
                ),

                localizationService.localize(
                        spot.getDescriptionKo(),
                        spot.getDescriptionEn(),
                        locale
                ),

                spot.getCategory(),
                spot.getImageUrl(),
                spot.getAvgRating(),
                spot.getReviewCount(),
                saved,
                spot.getOpeningHours(),
                spot.getBreakTime(),

                localizationService.localize(
                        spot.getClosedDaysKo(),
                        spot.getClosedDaysEn(),
                        locale
                ),

                localizationService.localize(
                        spot.getAddressKo(),
                        spot.getAddressEn(),
                        locale
                ),

                localizationService.localize(
                        spot.getTransportInfoKo(),
                        spot.getTransportInfoEn(),
                        locale
                ),

                spot.getTel(),
                spot.getLatitude(),
                spot.getLongitude()
        );
    }

    public SpotListResponse toListResponse(
            Spot spot,
            boolean saved,
            Double distanceMeters,
            Locale locale
    ) {
        return new SpotListResponse(
                spot.getSpotId(),

                localizationService.localize(
                        spot.getNameKo(),
                        spot.getNameEn(),
                        locale
                ),

                localizationService.localize(
                        spot.getDescriptionKo(),
                        spot.getDescriptionEn(),
                        locale
                ),

                spot.getCategory(),
                spot.getImageUrl(),
                spot.getAvgRating(),
                spot.getReviewCount(),
                saved,
                distanceMeters
        );
    }

    public SpotNearbyResponse toNearbyResponse(
            Spot spot,
            Double distanceMeters,
            Locale locale
    ) {
        return new SpotNearbyResponse(
                spot.getSpotId(),

                localizationService.localize(
                        spot.getNameKo(),
                        spot.getNameEn(),
                        locale
                ),

                localizationService.localize(
                        spot.getDescriptionKo(),
                        spot.getDescriptionEn(),
                        locale
                ),

                localizationService.localize(
                        spot.getAddressKo(),
                        spot.getAddressEn(),
                        locale
                ),

                spot.getImageUrl(),
                spot.getAvgRating(),
                distanceMeters
        );
    }
}