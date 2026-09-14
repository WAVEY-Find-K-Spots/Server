package com.Wavey.WaveyService.domain.user.dto;

import java.util.Map;

public interface OAuth2UserInfo {
    String getProviderId(); // 공급자별 소셜 고유 ID
    String getProvider();   // "google", "apple" 또는 "kakao"
    String getEmail();
    String getName();
    Map<String, Object> getAttributes();
}
