package com.Wavey.WaveyService.domain.policy.service;

import com.Wavey.WaveyService.domain.policy.dto.response.PolicyResponse;
import com.Wavey.WaveyService.domain.policy.entity.PolicyDocument;
import com.Wavey.WaveyService.domain.policy.enums.PolicyCategory;
import com.Wavey.WaveyService.domain.policy.repository.PolicyDocumentRepository;
import com.Wavey.WaveyService.domain.user.enums.Language;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyDocumentRepository policyDocumentRepository;

    public PolicyResponse getPolicy(String category, String language) {
        PolicyCategory policyCategory = parseCategory(category);
        Language policyLanguage = parseLanguage(language);

        PolicyDocument policyDocument = policyDocumentRepository
                .findByCategoryAndLanguageAndActiveTrue(policyCategory, policyLanguage)
                .orElseThrow(() -> new CustomException(ErrorCode.POLICY_NOT_FOUND));

        return PolicyResponse.from(policyDocument);
    }

    private PolicyCategory parseCategory(String value) {
        try {
            return PolicyCategory.valueOf(normalize(value));
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
    }

    private Language parseLanguage(String value) {
        try {
            return Language.valueOf(normalize(value));
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
