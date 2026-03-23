package com.Wavey.WaveyService.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // COMMON
    COMMON_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_INVALID_PARAMETER", "요청 파라미터가 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "서버 내부 오류가 발생했습니다."),

    // ROUTE
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTE_404", "해당 루트를 찾을 수 없습니다."),
    ROUTE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ROUTE_403", "해당 루트에 대한 권한이 없습니다."),

    // CONTENT
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_404", "해당 콘텐츠를 찾을 수 없습니다."),
    CONTENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "CONTENT_409", "이미 등록된 콘텐츠입니다."),
    CONTENT_URL_REQUIRED(HttpStatus.BAD_REQUEST, "CONTENT_400_URL", "콘텐츠 URL은 필수입니다."),
    CONTENT_THUMBNAIL_RESOLVE_FAILED(HttpStatus.BAD_GATEWAY, "CONTENT_502_THUMBNAIL", "썸네일 정보를 자동으로 가져오지 못했습니다."),
    INVALID_YOUTUBE_URL(HttpStatus.BAD_REQUEST, "CONTENT_400_YOUTUBE_URL", "유효한 유튜브 URL이 아닙니다."),
    INVALID_SPOTIFY_URL(HttpStatus.BAD_REQUEST, "CONTENT_400_SPOTIFY_URL", "유효한 스포티파이 URL이 아닙니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "해당 유저를 찾을 수 없습니다."),
    USER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "USER_403", "해당 유저 관련 권한이 없습니다."),

    // JWT
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_401", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_402", "만료된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_403", "접근 권한이 부족합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
