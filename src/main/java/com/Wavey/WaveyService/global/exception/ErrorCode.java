package com.Wavey.WaveyService.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // COMMON
    COMMON_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_INVALID_PARAMETER", "요청 파라미터가 올바르지 않습니다."),
    COMMON_INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "COMMON_FILE_TYPE", "지원하지 않는 파일 형식입니다."),
    COMMON_FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMON_FILE_SIZE", "파일 용량이 제한을 초과했습니다."),
    COMMON_FILE_EMPTY(HttpStatus.BAD_REQUEST, "COMMON_FILE", "파일이 비어있거나 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    EXTERNAL_API_DISABLED(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_API_DISABLED", "현재 DB 전용 모드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "요청한 데이터를 찾을 수 없습니다."),
    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "POLICY_NOT_FOUND", "해당 정책 문서를 찾을 수 없습니다."),
    STAMP_TOO_FAR(HttpStatus.BAD_REQUEST, "STAMP_TOO_FAR", "스탬프 획득 가능 거리 밖입니다."),

    // ROUTE
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND", "해당 루트를 찾을 수 없습니다."),
    ROUTE_FORBIDDEN(HttpStatus.FORBIDDEN, "ROUTE_FORBIDDEN", "해당 루트에 접근 권한이 없습니다."),
    ROUTE_SPOT_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTE_SPOT_NOT_FOUND", "루트에 해당 스팟이 없습니다."),
    ROUTE_SPOT_ALREADY_EXISTS(HttpStatus.CONFLICT, "ROUTE_SPOT_ALREADY_EXISTS", "이미 루트에 추가된 스팟입니다."),
    ROUTE_SPOT_ORDER_MISMATCH(HttpStatus.BAD_REQUEST, "ROUTE_SPOT_ORDER_MISMATCH", "수정 요청의 루트 스팟 ID가 일치하지 않습니다."),

    // DIRECTIONS
    DIRECTIONS_NOT_ENOUGH_SPOTS(HttpStatus.BAD_REQUEST, "DIRECTIONS_NOT_ENOUGH_SPOTS", "경로 계산에는 스팟이 2개 이상 필요합니다."),
    DIRECTIONS_UNSUPPORTED_MODE(HttpStatus.BAD_REQUEST, "DIRECTIONS_UNSUPPORTED_MODE", "지원하지 않는 이동수단입니다."),
    DIRECTIONS_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY, "DIRECTIONS_PROVIDER_ERROR", "외부 경로 엔진 호출에 실패했습니다."),

    // REGION
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "REGION_NOT_FOUND", "존재하지 않는 지역입니다."),
    REGION_ALREADY_EXISTS(HttpStatus.CONFLICT, "REGION_ALREADY_EXISTS", "이미 등록된 지역입니다."),

    // SPOT
    SPOT_NOT_FOUND(HttpStatus.NOT_FOUND, "SPOT_NOT_FOUND", "존재하지 않는 장소입니다."),
    SPOT_EXTERNAL_DATA_ALREADY_EXISTS(HttpStatus.CONFLICT, "SPOT_EXTERNAL_DATA_ALREADY_EXISTS", "이미 등록된 외부 장소 데이터입니다."),
    SPOT_INVALID_MAP_BOUNDS(HttpStatus.BAD_REQUEST, "SPOT_INVALID_MAP_BOUNDS", "잘못된 지도 범위입니다."),
    SPOT_EXTERNAL_API_KEY_MISSING(HttpStatus.BAD_REQUEST, "SPOT_EXTERNAL_API_KEY_MISSING", "외부 API 인증키가 설정되지 않았습니다."),
    SPOT_EXTERNAL_API_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "SPOT_EXTERNAL_API_REQUEST_FAILED", "외부 장소 데이터 요청에 실패했습니다."),

    // CONTENT
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_404", "해당 콘텐츠를 찾을 수 없습니다."),
    CONTENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "CONTENT_409", "이미 등록된 콘텐츠입니다."),
    CONTENT_URL_REQUIRED(HttpStatus.BAD_REQUEST, "CONTENT_400_URL", "콘텐츠 URL은 필수입니다."),
    CONTENT_THUMBNAIL_RESOLVE_FAILED(HttpStatus.BAD_GATEWAY, "CONTENT_502_THUMBNAIL", "썸네일 정보를 자동으로 가져오지 못했습니다."),
    INVALID_YOUTUBE_URL(HttpStatus.BAD_REQUEST, "CONTENT_400_YOUTUBE_URL", "유효한 유튜브 URL이 아닙니다."),
    INVALID_SPOTIFY_URL(HttpStatus.BAD_REQUEST, "CONTENT_400_SPOTIFY_URL", "유효한 스포티파이 URL이 아닙니다."),
    YOUTUBE_API_KEY_MISSING(HttpStatus.BAD_REQUEST, "CONTENT_400_YOUTUBE_KEY", "유튜브 API 키가 설정되지 않았습니다."),
    YOUTUBE_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_404_YOUTUBE", "유튜브에서 해당 영상을 찾을 수 없습니다."),
    YOUTUBE_API_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "CONTENT_502_YOUTUBE", "유튜브 API 요청에 실패했습니다."),
    SPOTIFY_CREDENTIALS_MISSING(HttpStatus.BAD_REQUEST, "CONTENT_400_SPOTIFY_KEY", "스포티파이 API 자격 증명이 설정되지 않았습니다."),
    SPOTIFY_TRACK_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_404_SPOTIFY", "스포티파이에서 해당 트랙을 찾을 수 없습니다."),
    SPOTIFY_API_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "CONTENT_502_SPOTIFY", "스포티파이 API 요청에 실패했습니다."),

    // WORK
    WORK_NOT_FOUND(HttpStatus.NOT_FOUND, "WORK_404", "해당 콘텐츠을 찾을 수 없습니다."),
    WORK_ALREADY_EXISTS(HttpStatus.CONFLICT, "WORK_409", "이미 등록된 콘텐츠입니다."),
    WORK_VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "WORK_VIDEO_404", "해당 콘텐츠 영상을 찾을 수 없습니다."),
    WORK_TRACK_NOT_FOUND(HttpStatus.NOT_FOUND, "WORK_TRACK_404", "해당 콘텐츠 트랙을 찾을 수 없습니다."),
    CONTENT_ALBUM_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_ALBUM_404", "해당 앨범을 찾을 수 없습니다."),

    // REVIEW
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW404", "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW409", "이미 해당 장소에 리뷰를 작성했습니다."),

    // NOTIFICATION
    NOTIFICATION_NOT_FOUND(
            HttpStatus.NOT_FOUND, "NOTIFICATION_404", "해당 알림을 찾을 수 없습니다."),

    // BADGE
    BADGE_NOT_FOUND(HttpStatus.NOT_FOUND, "BADGE_404", "해당 배지를 찾을 수 없습니다."),
    BADGE_NOT_CLAIMABLE(
            HttpStatus.BAD_REQUEST, "BADGE_400_NOT_CLAIMABLE", "아직 배지 수령 조건을 충족하지 않았습니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "해당 유저를 찾을 수 없습니다."),
    USER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "USER_403", "해당 유저 관련 권한이 없습니다."),

    // UPLOAD
    UPLOAD_INVALID_FILE_URL(HttpStatus.BAD_REQUEST, "UPLOAD_400_FILE_URL", "\uC5C5\uB85C\uB4DC\uB41C \uD30C\uC77C URL\uC774 \uC720\uD6A8\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4."),

    // OAUTH2
    OAUTH_INVALID_USER_INFO(HttpStatus.BAD_REQUEST, "OAUTH_400_USER_INFO", "소셜 로그인 사용자 정보를 확인할 수 없습니다."),
    OAUTH_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "OAUTH_400_EMAIL_REQUIRED", "신규 가입을 위해 이메일 제공 동의가 필요합니다."),
    OAUTH_PROFILE_REQUIRED(HttpStatus.BAD_REQUEST, "OAUTH_400_PROFILE_REQUIRED", "신규 가입을 위해 프로필 이름 제공 동의가 필요합니다."),

    // JWT
    REVOKED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_403", "로그아웃되어 사용할 수 없는 토큰입니다."),
    INVALID_LOGIN_CODE(HttpStatus.UNAUTHORIZED, "AUTH_401_LOGIN_CODE", "로그인 코드가 만료되었거나 이미 사용되었습니다."),
    AUTH_STORAGE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AUTH_503_STORAGE", "인증 저장소에 일시적으로 연결할 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_401", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_402", "만료된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_403", "접근 권한이 부족합니다."),

    // VISION
    VISION_IMAGE_TYPE_UNSUPPORTED(HttpStatus.BAD_REQUEST, "VISION_400_IMAGE_TYPE", "지원하지 않는 이미지 형식입니다. JPEG, PNG, WEBP 이미지만 업로드할 수 있습니다."),
    VISION_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "VISION_500_FAILED", "AI 이미지 분석 중 오류가 발생했습니다."),
    VISION_TOKEN_EXHAUSTED(HttpStatus.TOO_MANY_REQUESTS, "VISION_429_QUOTA", "이미지 분석 쿼터가 초과되었습니다."),
    VISION_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "VISION_429_RATE_LIMIT", "이미지 분석 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),

    // TRANSLATION
    GOOGLE_TRANSLATION_CONFIGURATION_MISSING(HttpStatus.SERVICE_UNAVAILABLE, "TRANSLATION_503_CONFIG", "Google Translation 인증 정보가 설정되지 않았습니다."),
    GOOGLE_TRANSLATION_GLOSSARY_CONFIGURATION_MISSING(HttpStatus.SERVICE_UNAVAILABLE, "TRANSLATION_503_GLOSSARY", "Google Translation Glossary가 설정되지 않았습니다."),
    GOOGLE_TRANSLATION_FAILED(HttpStatus.BAD_GATEWAY, "TRANSLATION_502_FAILED", "Google 번역 요청에 실패했습니다."),
    GOOGLE_TRANSLATION_QUOTA_EXHAUSTED(HttpStatus.TOO_MANY_REQUESTS, "TRANSLATION_429_QUOTA", "Google Translation 쿼터가 초과되었습니다."),

    // GEOCODING
    GOOGLE_GEOCODING_CONFIGURATION_MISSING(HttpStatus.SERVICE_UNAVAILABLE, "GEOCODING_503_CONFIG", "Google Geocoding API 키가 설정되지 않았습니다."),
    GOOGLE_GEOCODING_FAILED(HttpStatus.BAD_GATEWAY, "GEOCODING_502_FAILED", "좌표를 행정구역으로 변환하지 못했습니다."),
    GOOGLE_GEOCODING_QUOTA_EXHAUSTED(HttpStatus.TOO_MANY_REQUESTS, "GEOCODING_429_QUOTA", "Google Geocoding 쿼터가 초과되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
