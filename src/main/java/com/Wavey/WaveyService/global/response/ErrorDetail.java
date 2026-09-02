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

    @Schema(description = "에러 코드", example = "ROUTE_NOT_FOUND")
    private String code;

    @Schema(description = "에러 메시지", example = "해당 루트를 찾을 수 없습니다.")
    private String message;

    @Schema(description = "상세 에러 내역")
    private List<FieldErrorDetail> errors;
}
