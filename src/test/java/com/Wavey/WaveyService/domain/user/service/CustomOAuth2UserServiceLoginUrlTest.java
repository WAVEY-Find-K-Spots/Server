package com.Wavey.WaveyService.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.global.common.JwtTokenProvider;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceLoginUrlTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private RedisAuthTokenService authTokenService;
    @InjectMocks
    private CustomOAuth2UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "serverUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(userService, "baseUrl", "/oauth2/authorization/");
        ReflectionTestUtils.setField(userService, "googlePath", "google");
        ReflectionTestUtils.setField(userService, "applePath", "apple");
        ReflectionTestUtils.setField(userService, "kakaoPath", "kakao");
    }

    @Test
    void 로그인_URL에_카카오를_포함한다() {
        Map<String, String> loginUrls = userService.getLoginUrls();

        assertThat(loginUrls).containsEntry(
                "kakao",
                "http://localhost:8080/oauth2/authorization/kakao"
        );
    }
}
