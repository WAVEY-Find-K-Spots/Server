package com.Wavey.WaveyService.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {
    private int statusCode;
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