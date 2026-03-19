package com.Wavey.WaveyService.dto;

import java.util.Map;

public interface OAuth2UserInfo {
    String getProviderId(); // 소셜 고유 ID (sub)
    String getProvider();   // "google" 또는 "apple"
    String getEmail();
    String getName();
    Map<String, Object> getAttributes();
}