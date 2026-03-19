package com.Wavey.WaveyService.dto;

import java.util.Map;

public class AppleUserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;
    public AppleUserInfo(Map<String, Object> attributes) { this.attributes = attributes; }

    @Override public String getProviderId() { return (String) attributes.get("sub"); }
    @Override public String getProvider() { return "apple"; }
    @Override public String getEmail() { return (String) attributes.get("email"); }
    @Override public String getName() { return (String) attributes.get("name"); } // 최초 이후 null 가능
    @Override public Map<String, Object> getAttributes() { return attributes; }
}