package com.Wavey.WaveyService.domain.route.dto.response;

import com.Wavey.WaveyService.domain.route.entity.RouteSpot;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "루트 상세 조회용 스팟 응답 (스팟 상세정보 포함)")
@Getter
@Builder
public class RouteSpotDetailResponse {

    @Schema(description = "루트 스팟 ID", example = "10")
    private Long routeSpotId;

    @Schema(description = "스팟 ID", example = "101")
    private Long spotId;

    @Schema(description = "순서", example = "1")
    private Integer sequenceOrder;

    @Schema(description = "스팟 이름", example = "경복궁")
    private String name;

    @Schema(description = "스팟 카테고리", example = "K_HERITAGE")
    private SpotCategory category;

    @Schema(description = "주소", example = "서울 종로구 사직로 161")
    private String address;

    @Schema(description = "위도", example = "37.57961700")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "126.97704100")
    private BigDecimal longitude;

    @Schema(description = "썸네일 URL")
    private String thumbnailUrl;

    public static RouteSpotDetailResponse of(RouteSpot routeSpot, Spot spot) {
        RouteSpotDetailResponseBuilder builder = RouteSpotDetailResponse.builder()
                .routeSpotId(routeSpot.getId())
                .spotId(routeSpot.getSpotId())
                .sequenceOrder(routeSpot.getSequenceOrder());

        if (spot != null) {
            builder.name(spot.getName())
                    .category(spot.getCategory())
                    .address(spot.getAddress())
                    .latitude(spot.getLatitude())
                    .longitude(spot.getLongitude())
                    .thumbnailUrl(spot.getThumbnailUrl());
        }

        return builder.build();
    }
}
