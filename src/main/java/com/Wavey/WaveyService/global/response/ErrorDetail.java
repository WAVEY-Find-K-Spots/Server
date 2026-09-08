package com.Wavey.WaveyService.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail {

    @Schema(description = "에러 코드", example = "COMMON_INVALID_PARAMETER")
    private String code;

    @Schema(description = "에러 메시지", example = "요청 파라미터가 올바르지 않습니다.")
    private String message;

    @Schema(description = "상세 에러 내역", nullable = true)
    private List<FieldErrorDetail> errors;
}
