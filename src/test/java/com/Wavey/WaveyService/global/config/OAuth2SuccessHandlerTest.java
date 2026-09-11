package com.Wavey.WaveyService.global.config;

import com.Wavey.WaveyService.domain.user.entity.Role;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.domain.user.service.RedisAuthTokenService;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock
    private RedisAuthTokenService authTokenService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private OAuth2User oAuth2User;

    @Test
    void 로그인_성공시_일회성_코드를_담아_프론트로_리다이렉트한다() throws Exception {
        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(
                authTokenService,
                userRepository,
                "http://localhost:3000/oauth/callback"
        );
        User user = User.builder()
                .id(1L)
                .provider("google")
                .providerId("provider-id")
                .email("user@example.com")
                .name("사용자")
                .role(Role.USER)
                .build();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("providerId")).thenReturn("provider-id");
        when(oAuth2User.getAttribute("provider")).thenReturn("google");
        when(userRepository.findByProviderAndProviderId("google", "provider-id"))
                .thenReturn(Optional.of(user));
        when(authTokenService.issueLoginCode(1L)).thenReturn("login-code");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).isEqualTo(
                "http://localhost:3000/oauth/callback?code=login-code"
        );
        assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
        assertThat(response.getHeader(HttpHeaders.PRAGMA)).isEqualTo("no-cache");
        verify(authTokenService).issueLoginCode(1L);
        verify(userRepository).findByProviderAndProviderId("google", "provider-id");
    }

    @Test
    void 로그인_코드_저장_실패시_오류코드로_프론트에_리다이렉트한다() throws Exception {
        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(
                authTokenService,
                userRepository,
                "http://localhost:3000/oauth/callback"
        );
        User user = User.builder()
                .id(1L)
                .provider("google")
                .providerId("provider-id")
                .email("user@example.com")
                .name("사용자")
                .role(Role.USER)
                .build();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("providerId")).thenReturn("provider-id");
        when(oAuth2User.getAttribute("provider")).thenReturn("google");
        when(userRepository.findByProviderAndProviderId("google", "provider-id"))
                .thenReturn(Optional.of(user));
        when(authTokenService.issueLoginCode(1L))
                .thenThrow(new CustomException(ErrorCode.AUTH_STORAGE_UNAVAILABLE));
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(), response, authentication
        );

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).isEqualTo(
                "http://localhost:3000/oauth/callback?error=AUTH_503_STORAGE"
        );
    }
}
