package com.Wavey.WaveyService.domain.user.dto;

import com.Wavey.WaveyService.domain.user.enums.CountryCode;
import com.Wavey.WaveyService.domain.user.enums.Language;
import jakarta.validation.constraints.Size;

/**
 * 프로필 부분 수정 요청. 전달한 필드만 반영하며, {@code null}인 필드는 유지한다.
 * {@code email}/{@code name}은 OAuth 제공자 값으로만 동기화되므로 여기서 다루지 않는다.
 */
public record UserProfileUpdateRequest(
        @Size(max = 50) String nickname,
        CountryCode countryCode,
        Language language,
        Boolean locationEnabled,
        Boolean marketingEnabled
) {
}
