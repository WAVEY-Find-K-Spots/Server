package com.Wavey.WaveyService.domain.spot.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.Wavey.WaveyService.domain.spot.converter.SpotConverter;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotSearchRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.PlaceType;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.upload.service.UploadService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Locale;

class SpotMapResponseTest {

    @Test
    void listResponse_hasSpotCoordinates() {
        SpotConverter converter =
                new SpotConverter(new SpotLocalizationService(), Mockito.mock(UploadService.class));
        Spot spot =
                Spot.builder()
                        .regionId(1L)
                        .nameKo("경복궁")
                        .category(SpotCategory.K_DRAMA)
                        .placeType(PlaceType.OTHER)
                        .addressKo("서울")
                        .latitude(new BigDecimal("37.57961700"))
                        .longitude(new BigDecimal("126.97704100"))
                        .build();

        SpotListResponse response = converter.toListResponse(spot, false, null, Locale.KOREAN);

        assertThat(response.latitude()).isEqualTo(37.579617);
        assertThat(response.longitude()).isEqualTo(126.977041);
    }

    @Test
    void searchRequest_usesDefaultSizeWhenOmitted_andKeepsRequestedSize() {
        assertThat(request(null).size()).isEqualTo(6);
        assertThat(request(100).size()).isEqualTo(100);
    }

    private SpotSearchRequest request(Integer size) {
        return new SpotSearchRequest(
                null, null, null, null, null, null, null, null, null, null, null, null, size);
    }
}
