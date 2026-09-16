package com.Wavey.WaveyService.domain.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.Wavey.WaveyService.domain.policy.entity.PolicyDocument;
import com.Wavey.WaveyService.domain.policy.enums.PolicyCategory;
import com.Wavey.WaveyService.domain.policy.repository.PolicyDocumentRepository;
import com.Wavey.WaveyService.domain.user.enums.Language;
import com.Wavey.WaveyService.global.exception.CustomException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyDocumentRepository policyDocumentRepository;

    @InjectMocks
    private PolicyService policyService;

    @Test
    void 카테고리와_언어에_맞는_정책을_조회한다() {
        PolicyDocument document = PolicyDocument.builder()
                .category(PolicyCategory.TERMS)
                .language(Language.EN)
                .title("WAVEY Terms of Service")
                .content("# WAVEY Terms of Service")
                .version(1)
                .effectiveDate(LocalDate.of(2026, 9, 16))
                .active(true)
                .build();
        when(policyDocumentRepository.findByCategoryAndLanguageAndActiveTrue(
                PolicyCategory.TERMS, Language.EN)).thenReturn(Optional.of(document));

        assertThat(policyService.getPolicy("terms", "en").content())
                .isEqualTo("# WAVEY Terms of Service");
    }

    @Test
    void 지원하지_않는_카테고리와_언어는_예외를_발생시킨다() {
        assertThatThrownBy(() -> policyService.getPolicy("notice", "ko"))
                .isInstanceOf(CustomException.class);

        assertThatThrownBy(() -> policyService.getPolicy("terms", "ja"))
                .isInstanceOf(CustomException.class);
    }
}
