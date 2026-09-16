package com.Wavey.WaveyService.domain.policy.controller;

import com.Wavey.WaveyService.domain.policy.service.PolicyService;
import com.Wavey.WaveyService.domain.policy.dto.response.PolicyResponse;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Policy", description = "이용약관 및 개인정보 처리방침 API")
@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @Operation(summary = "정책 조회", description = "category와 language에 해당하는 정책 문서를 Markdown 형식으로 조회합니다. 로그인 없이 이용할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정책 조회 성공"),
            @ApiResponse(responseCode = "400", description = "category 또는 language가 올바르지 않음"),
            @ApiResponse(responseCode = "404", description = "해당 언어의 정책 문서가 없음")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<PolicyResponse>> getPolicy(
            @Parameter(description = "정책 구분 (terms 또는 privacy)", example = "terms")
            @RequestParam String category,
            @Parameter(description = "언어 (ko 또는 en)", example = "ko")
            @RequestParam String language
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                "정책 조회 성공",
                policyService.getPolicy(category, language)
        ));
    }
}
