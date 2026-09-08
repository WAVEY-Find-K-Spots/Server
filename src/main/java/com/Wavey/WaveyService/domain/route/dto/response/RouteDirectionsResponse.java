package com.Wavey.WaveyService.domain.route.dto.response;

import com.Wavey.WaveyService.domain.route.entity.TransportMode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "루트 경로 계산 응답")
@Getter
@Builder
public class RouteDirectionsResponse {

    @Schema(description = "루트 ID", example = "1")
    private Long routeId;

    @Schema(description = "이동수단", example = "TRANSIT")
    private TransportMode transportMode;

    @Schema(description = "전체 합계")
    private Total total;

    @Schema(description = "구간 목록 (i번째 스팟 → i+1번째 스팟)")
    private List<Segment> segments;

    @Schema(description = "전체 경로 폴리라인")
    private GeoLineString geometry;

    @Schema(description = "계산 시각", example = "2026-09-08T12:00:00")
    private LocalDateTime calculatedAt;

    @Schema(description = "경로 전체 합계")
    @Getter
    @Builder
    public static class Total {

        @Schema(description = "총 거리(m)", example = "12400")
        private long distanceMeters;

        @Schema(description = "총 소요시간(초)", example = "9000")
        private long durationSeconds;

        @Schema(description = "총 거리 표시 문자열", example = "12.4km")
        private String distanceText;

        @Schema(description = "총 소요시간 표시 문자열", example = "약 2시간 30분")
        private String durationText;
    }

    @Schema(description = "스팟 간 구간")
    @Getter
    @Builder
    public static class Segment {

        @Schema(description = "출발 루트 스팟 ID", example = "10")
        private Long fromRouteSpotId;

        @Schema(description = "도착 루트 스팟 ID", example = "15")
        private Long toRouteSpotId;

        @Schema(description = "출발 스팟 ID", example = "101")
        private Long fromSpotId;

        @Schema(description = "도착 스팟 ID", example = "105")
        private Long toSpotId;

        @Schema(description = "구간 순서 (1부터)", example = "1")
        private int sequenceOrder;

        @Schema(description = "구간 거리(m)", example = "900")
        private long distanceMeters;

        @Schema(description = "구간 소요시간(초)", example = "900")
        private long durationSeconds;

        @Schema(description = "구간 소요시간 표시 문자열", example = "대중교통 15분")
        private String durationText;

        @Schema(description = "구간 폴리라인")
        private GeoLineString geometry;
    }
}
