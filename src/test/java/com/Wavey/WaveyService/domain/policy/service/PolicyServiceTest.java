package com.Wavey.WaveyService.domain.policy.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PolicyServiceTest {

    private final PolicyService policyService = new PolicyService();

    @Test
    void 이용약관_리소스를_UTF8_원문으로_읽는다() {
        assertThat(policyService.getTermsOfService())
                .startsWith("# WAVEY 이용약관")
                .contains("회원정보 및 계정 관리");
    }

    @Test
    void 개인정보_처리방침_리소스를_UTF8_원문으로_읽는다() {
        assertThat(policyService.getPrivacyPolicy())
                .startsWith("# WAVEY 개인정보 처리방침")
                .contains("회원 식별, 소셜 로그인, 회원가입 및 계정 관리")
                .contains("개인정보 보호책임자 또는 담당부서");
    }
}
