package com.Wavey.WaveyService.domain.policy.controller;

import com.Wavey.WaveyService.domain.policy.service.PolicyService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Policy", description = "이용약관 및 개인정보 처리방침 API")
@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @Operation(summary = "이용약관 조회", description = "WAVEY 이용약관 원문을 조회합니다. 로그인 없이 이용할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이용약관 조회 성공")
    })
    @GetMapping("/terms")
    public ResponseEntity<CommonResponse<String>> getTermsOfService() {
        return ResponseEntity.ok(CommonResponse.success("이용약관 조회 성공", policyService.getTermsOfService()));
    }

    @Operation(summary = "개인정보 처리방침 조회", description = "WAVEY 개인정보 처리방침 원문을 조회합니다. 로그인 없이 이용할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "개인정보 처리방침 조회 성공")
    })
    @GetMapping("/privacy")
    public ResponseEntity<CommonResponse<String>> getPrivacyPolicy() {
        return ResponseEntity.ok(CommonResponse.success("개인정보 처리방침 조회 성공", policyService.getPrivacyPolicy()));
    }
}
