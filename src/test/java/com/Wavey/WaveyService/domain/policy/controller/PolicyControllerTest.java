package com.Wavey.WaveyService.domain.policy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.Wavey.WaveyService.domain.policy.dto.response.PolicyResponse;
import com.Wavey.WaveyService.domain.policy.service.PolicyService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import java.time.LocalDate;
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
    void 정책을_카테고리와_언어로_조회한다() {
        PolicyResponse terms = new PolicyResponse(
                "terms", "ko", "WAVEY 이용약관", "# WAVEY 이용약관", 1, LocalDate.of(2026, 9, 16));
        when(policyService.getPolicy("terms", "ko")).thenReturn(terms);

        CommonResponse<PolicyResponse> response = policyController.getPolicy("terms", "ko").getBody();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getData()).isEqualTo(terms);
        verify(policyService).getPolicy("terms", "ko");
    }
}
