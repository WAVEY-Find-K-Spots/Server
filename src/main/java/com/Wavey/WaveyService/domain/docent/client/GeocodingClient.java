package com.Wavey.WaveyService.domain.docent.client;

import com.Wavey.WaveyService.domain.docent.model.AdministrativeArea;

import java.util.Optional;

/** 좌표를 서비스 데이터셋에서 사용하는 행정구역으로 변환하는 내부 경계입니다. */
public interface GeocodingClient {

    Optional<AdministrativeArea> reverseGeocode(double latitude, double longitude);
}