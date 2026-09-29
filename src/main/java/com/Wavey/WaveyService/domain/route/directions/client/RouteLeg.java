package com.Wavey.WaveyService.domain.route.directions.client;

import java.util.List;

/**
 * 외부 길찾기 엔진이 계산한 한 구간(출발 → 도착)의 결과.
 * TRANSIT일 때 거리·시간·폴리라인·세부 구간은 첫 번째(추천) 후보 기준이다.
 *
 * @param distanceMeters   구간 거리(m)
 * @param durationSeconds  구간 소요시간(초)
 * @param path             구간 폴리라인. 각 좌표는 {@code [경도, 위도]} 순서(GeoJSON 규격).
 * @param transitLegs      대중교통(TRANSIT)일 때만 채워지는 세부 구간(도보/버스/지하철) 목록. WALK/CAR는 빈 리스트.
 * @param transitOptions   대중교통(TRANSIT)일 때만 채워지는 경로 후보 목록(추천 순). WALK/CAR는 빈 리스트.
 */
public record RouteLeg(
        long distanceMeters,
        long durationSeconds,
        List<double[]> path,
        List<TransitLegDetail> transitLegs,
        List<TransitItinerary> transitOptions
) {
    public RouteLeg(long distanceMeters, long durationSeconds, List<double[]> path) {
        this(distanceMeters, durationSeconds, path, List.of(), List.of());
    }

    public static RouteLeg ofTransit(List<TransitItinerary> options) {
        TransitItinerary best = options.get(0);
        return new RouteLeg(best.distanceMeters(), best.durationSeconds(), best.path(), best.legs(), options);
    }
}
