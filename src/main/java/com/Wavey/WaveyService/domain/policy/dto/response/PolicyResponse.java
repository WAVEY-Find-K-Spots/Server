package com.Wavey.WaveyService.domain.policy.dto.response;

import com.Wavey.WaveyService.domain.policy.entity.PolicyDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Locale;

@Schema(description = "정책 문서 응답")
public record PolicyResponse(
        @Schema(description = "정책 구분", example = "terms")
        String category,
        @Schema(description = "문서 언어", example = "ko")
        String language,
        @Schema(description = "정책 제목", example = "WAVEY 이용약관")
        String title,
        @Schema(description = "Markdown 형식의 정책 본문")
        String content,
        @Schema(description = "정책 버전", example = "1")
        Integer version,
        @Schema(description = "시행일", example = "2026-09-16")
        LocalDate effectiveDate
) {

    public static PolicyResponse from(PolicyDocument policyDocument) {
        return new PolicyResponse(
                policyDocument.getCategory().name().toLowerCase(Locale.ROOT),
                policyDocument.getLanguage().name().toLowerCase(Locale.ROOT),
                policyDocument.getTitle(),
                policyDocument.getContent(),
                policyDocument.getVersion(),
                policyDocument.getEffectiveDate()
        );
    }
}
