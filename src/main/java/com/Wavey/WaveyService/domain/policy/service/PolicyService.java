package com.Wavey.WaveyService.domain.policy.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    private static final String TERMS_PATH = "policies/terms-of-service.md";
    private static final String PRIVACY_PATH = "policies/privacy-policy.md";

    public String getTermsOfService() {
        return readPolicy(TERMS_PATH);
    }

    public String getPrivacyPolicy() {
        return readPolicy(PRIVACY_PATH);
    }

    private String readPolicy(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("정책 문서를 읽을 수 없습니다: " + path, e);
        }
    }
}
