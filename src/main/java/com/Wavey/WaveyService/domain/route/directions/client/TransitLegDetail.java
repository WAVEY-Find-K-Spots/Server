package com.Wavey.WaveyService.domain.route.directions.client;

import java.util.List;

/**
 * 대중교통 구간 내 세부 구간(도보/버스/지하철 등) 하나에 대한 정보.
 * Tmap 대중교통 길찾기 응답의 {@code legs} 원소에서 추출한다.
 *
 * @param mode           구간 수단 (예: {@code WALK}, {@code BUS}, {@code SUBWAY})
 * @param routeName      노선명/버스 번호 (도보 구간 등에는 없을 수 있음)
 * @param routeColor     노선 색상 (hex, 없을 수 있음)
 * @param startName      출발 정류장/역 이름
 * @param endName        도착 정류장/역 이름
 * @param distanceMeters 구간 거리(m)
 * @param durationSeconds 구간 소요시간(초)
 * @param stationCount   이동하는 정류장/역 수 (도보 구간은 0)
 * @param passStops      출발부터 도착까지 경유하는 정류장/역 이름 (도보 구간은 빈 리스트)
 */
public record TransitLegDetail(
        String mode,
        String routeName,
        String routeColor,
        String startName,
        String endName,
        long distanceMeters,
        long durationSeconds,
        int stationCount,
        List<String> passStops
) {
}
