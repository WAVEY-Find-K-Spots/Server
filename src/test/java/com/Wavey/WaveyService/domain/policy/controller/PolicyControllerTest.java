package com.Wavey.WaveyService.domain.policy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.Wavey.WaveyService.domain.policy.service.PolicyService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyControllerTest {

    @Mock
    private PolicyService policyService;

    @InjectMocks
    private PolicyController policyController;

    @Test
    void 이용약관_원문을_조회한다() {
        String terms = "# WAVEY 이용약관";
        when(policyService.getTermsOfService()).thenReturn(terms);

        CommonResponse<String> response = policyController.getTermsOfService().getBody();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getData()).isEqualTo(terms);
        verify(policyService).getTermsOfService();
    }

    @Test
    void 개인정보_처리방침_원문을_조회한다() {
        String privacyPolicy = "# WAVEY 개인정보 처리방침";
        when(policyService.getPrivacyPolicy()).thenReturn(privacyPolicy);

        CommonResponse<String> response = policyController.getPrivacyPolicy().getBody();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getData()).isEqualTo(privacyPolicy);
        verify(policyService).getPrivacyPolicy();
    }
}
