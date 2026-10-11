package com.Wavey.WaveyService.domain.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.enums.Role;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.domain.user.service.RedisAuthTokenService;
import com.Wavey.WaveyService.domain.user.service.LocalPasswordResetMailService;
import com.Wavey.WaveyService.global.common.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Tag("admin")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin-auth-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
class AdminLocalAuthenticationIntegrationTest {

    private static final String ADMIN_EMAIL = "local-admin@example.com";
    private static final String USER_EMAIL = "local-user@example.com";
    private static final String PASSWORD = "WaveyAdmin123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private RedisAuthTokenService authTokenService;

    @MockitoBean
    private LocalPasswordResetMailService passwordResetMailService;

    @MockitoBean
    private CloudVisionTemplate cloudVisionTemplate;

    @MockitoBean
    private ImageAnnotatorClient imageAnnotatorClient;

    @MockitoBean
    private CredentialsProvider credentialsProvider;

    private User admin;
    private User member;

    @BeforeEach
    void setUp() {
        admin = saveLocalUser(ADMIN_EMAIL, Role.ADMIN);
        member = saveLocalUser(USER_EMAIL, Role.USER);
    }

    @Test
    @DisplayName("로컬 회원가입은 관리자 화이트리스트 이메일이어도 USER로 생성하고 JWT를 발급한다")
    void localSignup_alwaysCreatesUser_andUsesExistingJwtFlow() throws Exception {
        String signupBody = """
                {
                  "email": "TEST@example.com",
                  "password": "LocalPass123!",
                  "name": "로컬 사용자"
                }
                """;

        String response = mockMvc.perform(post("/api/v1/auth/local/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.isNewUser").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User created = userRepository.findByProviderAndProviderId("local", "test@example.com")
                .orElseThrow();
        assertThat(created.getRole()).isEqualTo(Role.USER);
        assertThat(created.getPasswordHash()).isNotEqualTo("LocalPass123!");
        assertThat(passwordEncoder.matches("LocalPass123!", created.getPasswordHash())).isTrue();
        verify(authTokenService).storeRefreshToken(
                eq(created.getId()),
                anyString(),
                any(Duration.class)
        );

        JsonNode tokenData = objectMapper.readTree(response).path("data");
        String accessToken = tokenData.path("accessToken").asText();
        String refreshToken = tokenData.path("refreshToken").asText();

        mockMvc.perform(get("/api/v1/auth/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.role").value("USER"));

        when(authTokenService.rotateRefreshToken(
                eq(created.getId()),
                eq(refreshToken),
                anyString(),
                any(Duration.class)
        )).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer expired-access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("잘못된 비밀번호와 중복 로컬 가입은 구분된 인증 오류를 반환한다")
    void localLogin_rejectsWrongPassword_andDuplicateSignup() throws Exception {
        mockMvc.perform(post("/api/v1/auth/local/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "local-user@example.com",
                                  "password": "WrongPassword!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_401_LOCAL_CREDENTIALS"));

        mockMvc.perform(post("/api/v1/auth/local/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "LOCAL-USER@example.com",
                                  "password": "AnotherPass123!",
                                  "name": "중복 사용자"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("AUTH_409_LOCAL_ACCOUNT"));
    }

    @Test
    @DisplayName("SMTP 재설정은 동일 응답을 반환하고 일회용 토큰으로 비밀번호를 변경한다")
    void smtpPasswordReset_sendsGenericResponse_andConsumesOneTimeToken() throws Exception {
        when(authTokenService.issuePasswordResetToken(member.getId())).thenReturn("reset-token");
        when(passwordResetMailService.sendResetLink(
                member.getEmail(), member.getName(), "reset-token"
        )).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/local/password/reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"local-user@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "가입된 로컬 계정인 경우 비밀번호 재설정 메일을 발송했습니다."
                ));

        mockMvc.perform(post("/api/v1/auth/local/password/reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"unknown@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "가입된 로컬 계정인 경우 비밀번호 재설정 메일을 발송했습니다."
                ));

        verify(passwordResetMailService, times(2)).ensureConfigured();
        verify(passwordResetMailService).sendResetLink(
                member.getEmail(), member.getName(), "reset-token"
        );

        when(authTokenService.consumePasswordResetToken("reset-token"))
                .thenReturn(member.getId());

        mockMvc.perform(post("/api/v1/auth/local/password/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "reset-token",
                                  "newPassword": "ResetPassword123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "비밀번호 재설정 성공. 새 비밀번호로 로그인해 주세요."
                ));

        User changed = userRepository.findById(member.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("ResetPassword123!", changed.getPasswordHash())).isTrue();
        verify(authTokenService).deleteRefreshToken(member.getId());
    }

    @Test
    @DisplayName("SMTP 발송 실패 시 토큰을 폐기하고 실제 실패 응답을 반환한다")
    void smtpPasswordReset_deliveryFailure_returnsBadGateway() throws Exception {
        when(authTokenService.issuePasswordResetToken(member.getId())).thenReturn("failed-reset-token");
        when(passwordResetMailService.sendResetLink(
                member.getEmail(), member.getName(), "failed-reset-token"
        )).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/local/password/reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"local-user@example.com"}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code")
                        .value("AUTH_502_PASSWORD_RESET_MAIL_DELIVERY"))
                .andExpect(jsonPath("$.error.message")
                        .value("비밀번호 재설정 메일 발송에 실패했습니다. 잠시 후 다시 시도해 주세요."));

        verify(authTokenService).deletePasswordResetToken("failed-reset-token");
    }

    @Test
    @DisplayName("JWT 이메일과 현재 비밀번호 검증 후 비밀번호를 변경하고 기존 토큰을 폐기한다")
    void localPasswordChange_verifiesJwtEmailAndRevokesTokens() throws Exception {
        String accessToken = login(USER_EMAIL);
        String originalPasswordHash = userRepository.findById(member.getId()).orElseThrow().getPasswordHash();
        String mismatchedEmailToken = tokenProvider.createAccessToken(
                "local",
                member.getProviderId(),
                "another-user@example.com"
        );

        mockMvc.perform(patch("/api/v1/auth/local/password")
                        .header("Authorization", bearer(mismatchedEmailToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "%s",
                                  "newPassword": "ChangedPassword123!"
                                }
                                """.formatted(PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("TOKEN_401"));

        mockMvc.perform(patch("/api/v1/auth/local/password")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "%s",
                                  "newPassword": "ChangedPassword123!"
                                }
                                """.formatted(PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호 변경 성공. 새 비밀번호로 다시 로그인해 주세요."));

        User changed = userRepository.findById(member.getId()).orElseThrow();
        assertThat(changed.getPasswordHash()).isNotEqualTo(originalPasswordHash);
        assertThat(passwordEncoder.matches("ChangedPassword123!", changed.getPasswordHash())).isTrue();
        verify(authTokenService).blacklistAccessToken(anyString(), any(Duration.class));
        verify(authTokenService).deleteRefreshToken(member.getId());

        mockMvc.perform(post("/api/v1/auth/local/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(USER_EMAIL, PASSWORD)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/local/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "ChangedPassword123!"
                                }
                                """.formatted(USER_EMAIL)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로컬 ADMIN 토큰으로 관리자 기능 묶음을 호출할 수 있다")
    void localAdmin_canCallAdminFeatureSet() throws Exception {
        String accessToken = login(ADMIN_EMAIL);

        mockMvc.perform(get("/api/v1/auth/all")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(patch("/api/v1/auth/role/{id}", member.getId())
                        .param("role", "ADMIN")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk());
        assertThat(userRepository.findById(member.getId()).orElseThrow().getRole())
                .isEqualTo(Role.ADMIN);

        mockMvc.perform(post("/api/v1/regions")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nameKo":"테스트 지역","nameEn":"Test Region"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.nameKo").value("테스트 지역"));

        mockMvc.perform(post("/api/v1/contents")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titleKo": "관리자 테스트 콘텐츠",
                                  "titleEn": "Admin Test Content",
                                  "category": "DRAMA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.titleKo").value("관리자 테스트 콘텐츠"));

        mockMvc.perform(post("/api/v1/admin/notifications/system")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "관리자 테스트",
                                  "titleEn": "Admin test",
                                  "body": "관리자 알림 테스트입니다.",
                                  "bodyEn": "This is an admin notification test.",
                                  "recipientUserIds": [%d],
                                  "targetType": "SYSTEM",
                                  "eventKey": "admin-test-local-login"
                                }
                                """.formatted(member.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.createdCount").value(1));

        mockMvc.perform(get("/api/v1/admin/badges")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/spots/sync/status")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dailyCallLimit").isNumber());
    }

    @Test
    @DisplayName("로컬 USER 토큰은 모든 관리자 기능 그룹에서 403을 반환한다")
    void localUser_isRejectedByAdminFeatureSet() throws Exception {
        String accessToken = login(USER_EMAIL);

        mockMvc.perform(get("/api/v1/auth/all")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/regions")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nameKo":"거부 지역","nameEn":"Denied Region"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/contents")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titleKo": "거부 콘텐츠",
                                  "titleEn": "Denied Content",
                                  "category": "DRAMA"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/admin/notifications/system")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "거부 알림",
                                  "body": "권한 없는 요청",
                                  "eventKey": "denied-admin-notification"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/badges")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/spots/sync/status")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isForbidden());
    }

    private User saveLocalUser(String email, Role role) {
        return userRepository.saveAndFlush(User.builder()
                .provider("local")
                .providerId(email)
                .email(email)
                .name(role == Role.ADMIN ? "로컬 관리자" : "로컬 사용자")
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .role(role)
                .build());
    }

    private String login(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/local/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isNewUser").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.path("data").path("accessToken").asText();
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}
