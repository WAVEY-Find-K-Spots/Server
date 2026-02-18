package com.Wavey.WaveyService.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter
public class OAuthDto {
    private String email;
    private String name;
    private String sub;
    private String provider;
    private Map<String, Object> attributes;

    @Builder
    public OAuthDto(String email, String name, String sub, String provider, Map<String, Object> attributes) {
        this.email = email;
        this.name = name;
        this.sub = sub;
        this.provider = provider;
        this.attributes = attributes;
    }

    public static OAuthDto ofGoogle(String registrationId, Map<String, Object> attributes) {
        return OAuthDto.builder()
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .sub((String) attributes.get("sub"))
                .provider(registrationId)
                .attributes(attributes)
                .build();
    }
}