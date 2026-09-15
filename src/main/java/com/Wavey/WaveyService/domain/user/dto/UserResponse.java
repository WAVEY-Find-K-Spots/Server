package com.Wavey.WaveyService.domain.user.dto;

import com.Wavey.WaveyService.domain.user.enums.CountryCode;
import com.Wavey.WaveyService.domain.user.enums.Language;
import com.Wavey.WaveyService.domain.user.enums.Role;
import com.Wavey.WaveyService.domain.user.entity.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        String provider,
        Role role,
        String profileImageUrl,
        String nickname,
        CountryCode countryCode,
        Language language
) {
    public static UserResponse from(User user, String resolvedProfileImageUrl) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProvider(),
                user.getRole(),
                resolvedProfileImageUrl,
                user.getNickname(),
                user.getCountryCode(),
                user.getLanguage());
    }
}
