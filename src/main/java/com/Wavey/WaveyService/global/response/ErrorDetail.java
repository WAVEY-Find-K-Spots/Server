package com.Wavey.WaveyService.global.response;

import lombok.*;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail {
    private String code;
    private List<FieldErrorDetail> errors;
}