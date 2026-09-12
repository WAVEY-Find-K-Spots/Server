package com.Wavey.WaveyService.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    @Schema(description = "HTTP 상태 코드", example = "200")
    private int statusCode;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    @Schema(description = "에러 정보", nullable = true)
    private ErrorDetail error;

    public static <T> CommonResponse<T> success(String message, T data) {
        return success(200, message, data);
    }

    public static <T> CommonResponse<T> success(int statusCode, String message, T data) {
        return CommonResponse.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .error(null)
                .build();
    }

    public static CommonResponse<Void> error(int statusCode, ErrorDetail error) {
        return CommonResponse.<Void>builder()
                .statusCode(statusCode)
                .message(null)
                .data(null)
                .error(error)
                .build();
    }
}
