package com.Wavey.WaveyService.domain.route.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.Wavey.WaveyService.domain.user.entity.Role;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.global.common.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.vision.v1.ImageAnnotatorClient;

/**
 * 인증 토큰으로 루트 API 를 호출했을 때 principal → userId 추출이 정상 동작하는지(500 이 아닌지) 확인한다.
 * (#34 회귀 방지)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RouteAuthenticationIntegrationTest {

    @MockitoBean
    private CloudVisionTemplate cloudVisionTemplate;

    @MockitoBean
    private ImageAnnotatorClient imageAnnotatorClient;

    @MockitoBean
    private CredentialsProvider credentialsProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String PROVIDER = "google";
    private static final String PROVIDER_ID = "auth-it-user-1";

    private String accessToken;

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder()
                .provider(PROVIDER)
                .providerId(PROVIDER_ID)
                .email("auth-it@example.com")
                .name("auth-it")
                .role(Role.USER)
                .build());
        accessToken = jwtTokenProvider.createAccessToken(PROVIDER, PROVIDER_ID);
    }

    @Test
    @DisplayName("토큰과 함께 내 루트 목록 조회 시 200 을 반환한다 (이전에는 500)")
    void getMyRoutes_withToken_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/routes")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @DisplayName("토큰으로 루트를 생성하면 소유자로 저장되고 내 목록에 조회된다")
    void createRoute_thenListedAsMine() throws Exception {
        String body = """
                {"name":"인증 확인용 루트","visibility":"PRIVATE","spots":[]}
                """;

        mockMvc.perform(post("/api/v1/routes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("인증 확인용 루트"));

        mockMvc.perform(get("/api/v1/routes")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("인증 확인용 루트"));
    }

    @Test
    @DisplayName("토큰 없이 호출하면 401 을 반환한다")
    void getMyRoutes_withoutToken_returns401() throws Exception {
        int statusCode = mockMvc.perform(get("/api/v1/routes"))
                .andReturn().getResponse().getStatus();

        assertThat(statusCode).isEqualTo(401);
    }
}
