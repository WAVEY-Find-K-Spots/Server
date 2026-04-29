package com.Wavey.WaveyService.domain.route.dto.response;

import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.entity.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "루트 목록 응답")
@Getter
@Builder
public class RouteSummaryResponse {

    @Schema(description = "루트 ID", example = "1")
    private Long routeId;

    @Schema(description = "루트 이름", example = "서울 야경 루트")
    private String name;

    @Schema(description = "루트 설명", example = "한강과 남산을 잇는 야경 코스")
    private String description;

    @Schema(description = "공개 여부", example = "PUBLIC")
    private Visibility visibility;

    @Schema(description = "포함된 스팟 수", example = "4")
    private int spotCount;

    @Schema(description = "생성일시", example = "2025-04-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2025-04-10T15:30:00")
    private LocalDateTime updatedAt;

    public static RouteSummaryResponse from(Route route) {
        return RouteSummaryResponse.builder()
                .routeId(route.getId())
                .name(route.getName())
                .description(route.getDescription())
                .visibility(route.getVisibility())
                .spotCount(route.getRouteSpots() == null ? 0 : route.getRouteSpots().size())
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }
}