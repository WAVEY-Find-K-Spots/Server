package com.Wavey.WaveyService.domain.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class KakaoUserInfoTest {

    @Test
    void 카카오_응답에서_사용자_정보를_추출한다() {
        Map<String, Object> attributes = Map.of(
                "id", 123456789L,
                "kakao_account", Map.of(
                        "email", "kakao@example.com",
                        "profile", Map.of("nickname", "카카오 사용자")
                )
        );

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("kakao", attributes);

        assertThat(userInfo.getProvider()).isEqualTo("kakao");
        assertThat(userInfo.getProviderId()).isEqualTo("123456789");
        assertThat(userInfo.getEmail()).isEqualTo("kakao@example.com");
        assertThat(userInfo.getName()).isEqualTo("카카오 사용자");
    }

    @Test
    void 선택_동의_정보가_없어도_사용자_식별자는_추출한다() {
        OAuth2UserInfo userInfo = new KakaoUserInfo(Map.of("id", 42L));

        assertThat(userInfo.getProviderId()).isEqualTo("42");
        assertThat(userInfo.getEmail()).isNull();
        assertThat(userInfo.getName()).isNull();
    }
}
