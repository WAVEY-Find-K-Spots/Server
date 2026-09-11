package com.Wavey.WaveyService.domain.user.dto;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        return id == null ? null : String.valueOf(id);
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        return getString(kakaoAccount(), "email");
    }

    @Override
    public String getName() {
        return getString(profile(), "nickname");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    private Map<String, Object> kakaoAccount() {
        return nestedMap(attributes, "kakao_account");
    }

    private Map<String, Object> profile() {
        return nestedMap(kakaoAccount(), "profile");
    }

    private Map<String, Object> nestedMap(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (!(value instanceof Map<?, ?> rawMap)) {
            return Map.of();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) rawMap;
        return result;
    }

    private String getString(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
