package com.Wavey.WaveyService.global.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}