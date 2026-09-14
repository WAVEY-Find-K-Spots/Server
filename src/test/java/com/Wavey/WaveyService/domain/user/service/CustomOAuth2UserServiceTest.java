package com.Wavey.WaveyService.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.Wavey.WaveyService.domain.user.dto.OAuth2UserInfo;
import com.Wavey.WaveyService.domain.user.entity.Role;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.global.common.JwtTokenProvider;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RedisAuthTokenService authTokenService;
    @Mock
    private OAuth2UserInfo userInfo;

    private CustomOAuth2UserService userService;

    @BeforeEach
    void setUp() {
        userService = new CustomOAuth2UserService(userRepository, tokenProvider, authTokenService);
    }

    @Test
    void 신규_사용자가_이메일_동의하지_않으면_구분된_오류를_반환한다() {
        when(userInfo.getProvider()).thenReturn("kakao");
        when(userInfo.getProviderId()).thenReturn("provider-id");
        when(userRepository.findByProviderAndProviderId("kakao", "provider-id"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.saveOrUpdate(userInfo))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(exception -> assertThat(
                        ((OAuth2AuthenticationException) exception).getError().getErrorCode()
                ).isEqualTo(ErrorCode.OAUTH_EMAIL_REQUIRED.getCode()));
    }

    @Test
    void 기존_사용자는_공급자가_이메일을_다시_주지_않아도_로그인할_수_있다() {
        User existingUser = User.builder()
                .id(1L)
                .provider("kakao")
                .providerId("provider-id")
                .email("saved@example.com")
                .name("기존 사용자")
                .role(Role.USER)
                .build();
        when(userInfo.getProvider()).thenReturn("kakao");
        when(userInfo.getProviderId()).thenReturn("provider-id");
        when(userInfo.getName()).thenReturn("변경된 이름");
        when(userRepository.findByProviderAndProviderId("kakao", "provider-id"))
                .thenReturn(Optional.of(existingUser));

        User result = userService.saveOrUpdate(userInfo);

        assertThat(result.getEmail()).isEqualTo("saved@example.com");
        assertThat(result.getName()).isEqualTo("변경된 이름");
    }
}
