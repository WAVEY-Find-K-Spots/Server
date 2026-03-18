package com.Wavey.WaveyService.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail {

    @Schema(description = "에러 코드", example = "도메인_상태코드")
    private String code;

    @Schema(description = "에러 메시지", example = "해당 루트를 찾을 수 없습니다.")
    private String message;

    @Schema(description = "상세 에러 내역 (필드 밸리데이션 등)")
    private List<FieldErrorDetail> errors;

}