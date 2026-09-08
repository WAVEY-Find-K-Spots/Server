package com.Wavey.WaveyService.global.response;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FieldErrorDetail {
    private String field;
    private String value;
    private String reason;
}