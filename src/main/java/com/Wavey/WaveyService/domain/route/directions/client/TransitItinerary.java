package com.Wavey.WaveyService.domain.route.directions.client;

import java.util.List;

/**
 * 대중교통 경로 후보 하나. Tmap 대중교통 길찾기 응답의 {@code itineraries} 원소에서 추출한다.
 *
 * @param distanceMeters     총 거리(m)
 * @param durationSeconds    총 소요시간(초)
 * @param walkDistanceMeters 총 도보 거리(m)
 * @param walkSeconds        총 도보 시간(초)
 * @param transferCount      환승 횟수
 * @param fare               요금(원, 없으면 0)
 * @param path               경로 폴리라인. 각 좌표는 {@code [경도, 위도]} 순서(GeoJSON 규격).
 * @param legs               세부 구간(도보/버스/지하철) 목록
 */
public record TransitItinerary(
        long distanceMeters,
        long durationSeconds,
        long walkDistanceMeters,
        long walkSeconds,
        int transferCount,
        int fare,
        List<double[]> path,
        List<TransitLegDetail> legs
) {
}
