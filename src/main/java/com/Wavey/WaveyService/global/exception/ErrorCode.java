package com.Wavey.WaveyService.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // COMMON
    COMMON_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_INVALID_PARAMETER", "요청 파라미터가 잘못되었습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "서버 내부 에러가 발생했습니다."),

    // ROUTE
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND", "해당 루트를 찾을 수 없습니다."),
    ROUTE_FORBIDDEN(HttpStatus.FORBIDDEN, "ROUTE_FORBIDDEN", "해당 루트에 접근 권한이 없습니다."),
    ROUTE_SPOT_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTE_SPOT_NOT_FOUND", "루트에 해당 스팟이 없습니다."),
    ROUTE_SPOT_ALREADY_EXISTS(HttpStatus.CONFLICT, "ROUTE_SPOT_ALREADY_EXISTS", "이미 루트에 추가된 스팟입니다."),
    ROUTE_SPOT_ORDER_MISMATCH(HttpStatus.BAD_REQUEST, "ROUTE_SPOT_ORDER_MISMATCH", "재정렬 요청에 루트 스팟 ID가 누락되었습니다."),

    // SPOT
    SPOT_NOT_FOUND(HttpStatus.NOT_FOUND, "SPOT_NOT_FOUND", "해당 스팟을 찾을 수 없습니다."),
  
    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "해당 유저를 찾을 수 없습니다."),
    USER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "USER_403", "해당 유저 관련 권한이 없습니다."),

    //JWT
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_401", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_402", "만료된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_403", "접근 권한이 부족합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}