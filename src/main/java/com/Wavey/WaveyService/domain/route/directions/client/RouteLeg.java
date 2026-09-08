package com.Wavey.WaveyService.domain.route.directions.client;

import java.util.List;

/**
 * 외부 길찾기 엔진이 계산한 한 구간(출발 → 도착)의 결과.
 *
 * @param distanceMeters   구간 거리(m)
 * @param durationSeconds  구간 소요시간(초)
 * @param path             구간 폴리라인. 각 좌표는 {@code [경도, 위도]} 순서(GeoJSON 규격).
 */
public record RouteLeg(
        long distanceMeters,
        long durationSeconds,
        List<double[]> path
) {
}
