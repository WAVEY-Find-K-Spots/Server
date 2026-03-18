package com.Wavey.WaveyService.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    @Schema(description = "HTTP 상태 코드", example = "200")
    private int statusCode;

    @Schema(description = "응답 메시지 (성공 시에만 포함)", example = "루트 생성 성공")
    private String message;

    private T data;
    private ErrorDetail error;

    public static <T> CommonResponse<T> success(String message, T data) {
        return CommonResponse.<T>builder()
                .statusCode(200)
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