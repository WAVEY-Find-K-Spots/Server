package com.Wavey.WaveyService.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.Wavey.WaveyService.domain.user.dto.OAuth2UserInfo;
import com.Wavey.WaveyService.domain.user.dto.UserProfileUpdateRequest;
import com.Wavey.WaveyService.domain.user.dto.UserResponse;
import com.Wavey.WaveyService.domain.user.enums.Role;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.entity.UserSetting;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.domain.user.repository.UserSettingsRepository;
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
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RedisAuthTokenService authTokenService;
    @Mock
    private com.Wavey.WaveyService.domain.upload.service.UploadService uploadService;
    @Mock
    private OAuth2UserInfo userInfo;

    private CustomOAuth2UserService userService;

    @BeforeEach
    void setUp() {
        userService = new CustomOAuth2UserService(
                userRepository,
                userSettingsRepository,
                tokenProvider,
                authTokenService,
                uploadService
        );
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

    @Test
    void 프로필_수정으로_위치와_마케팅_설정을_부분_변경한다() {
        User user = createUser();
        UserSetting setting = UserSetting.builder()
                .userId(user.getId())
                .locationEnabled(true)
                .marketingEnabled(false)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(user.getId())).thenReturn(Optional.of(setting));
        when(userSettingsRepository.save(setting)).thenReturn(setting);

        UserResponse response = userService.updateProfile(
                user.getId(),
                new UserProfileUpdateRequest(null, null, null, false, true)
        );

        assertThat(response.locationEnabled()).isFalse();
        assertThat(response.marketingEnabled()).isTrue();
        verify(userSettingsRepository).save(setting);
    }

    @Test
    void 설정이_없는_사용자는_기본값으로_생성한_뒤_요청값을_반영한다() {
        User user = createUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(userSettingsRepository.save(any(UserSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateProfile(
                user.getId(),
                new UserProfileUpdateRequest(null, null, null, null, true)
        );

        assertThat(response.locationEnabled()).isTrue();
        assertThat(response.marketingEnabled()).isTrue();
    }

    @Test
    void 설정값을_전달하지_않으면_기존값을_유지하고_저장하지_않는다() {
        User user = createUser();
        UserSetting setting = UserSetting.builder()
                .userId(user.getId())
                .locationEnabled(false)
                .marketingEnabled(true)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(user.getId())).thenReturn(Optional.of(setting));

        UserResponse response = userService.updateProfile(
                user.getId(),
                new UserProfileUpdateRequest("새 닉네임", null, null, null, null)
        );

        assertThat(response.nickname()).isEqualTo("새 닉네임");
        assertThat(response.locationEnabled()).isFalse();
        assertThat(response.marketingEnabled()).isTrue();
        verify(userSettingsRepository, never()).save(setting);
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .provider("google")
                .providerId("provider-id")
                .email("user@example.com")
                .name("사용자")
                .role(Role.USER)
                .build();
    }
}
