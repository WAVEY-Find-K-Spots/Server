package com.Wavey.WaveyService.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FieldErrorDetail {
    private String field;
    private String value;
    private String reason;
}
