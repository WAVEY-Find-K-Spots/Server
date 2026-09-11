package com.Wavey.WaveyService.domain.user.service;

import com.Wavey.WaveyService.domain.user.dto.OAuth2UserInfo;
import com.Wavey.WaveyService.domain.user.dto.OAuth2UserInfoFactory;
import com.Wavey.WaveyService.domain.user.dto.UserResponse;
import com.Wavey.WaveyService.domain.user.dto.TokenResponse;
import com.Wavey.WaveyService.domain.user.entity.Role;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.global.common.JwtTokenProvider;
import com.Wavey.WaveyService.global.common.TokenType;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final RedisAuthTokenService authTokenService;

    @Value("${auth.admin-white-list}")
    private List<String> adminWhiteList;

    @Value("${auth.server-url}")
    private String serverUrl;
    @Value("${auth.base-url}")
    private String baseUrl;
    @Value("${auth.google-path}")
    private String googlePath;
    @Value("${auth.apple-path}")
    private String applePath;
    @Value("${auth.kakao-path}")
    private String kakaoPath;

    public Map<String, String> getLoginUrls() {
        Map<String, String> loginUrls = new HashMap<>();
        loginUrls.put("google", serverUrl + baseUrl + googlePath);
        loginUrls.put("apple", serverUrl + baseUrl + applePath);
        loginUrls.put("kakao", serverUrl + baseUrl + kakaoPath);
        return loginUrls;
    }

    /**
     * 토큰 재발급 비즈니스 로직 (Refresh Token Rotation)
     * 피드백 반영: 만료된 리프레시 토큰 시 TOKEN_402(EXPIRED_TOKEN) 반환
     */
    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        Claims claims = tokenProvider.getValidatedClaims(refreshToken, TokenType.REFRESH);
        String provider = (String) claims.get("provider");
        String providerId = claims.getSubject();

        User user = findByProviderAndProviderId(provider, providerId);

        IssuedTokenPair issuedTokenPair = createTokenPair(user);
        if (!authTokenService.rotateRefreshToken(
                user.getId(),
                refreshToken,
                issuedTokenPair.response().refreshToken(),
                issuedTokenPair.refreshTokenTtl()
        )) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        return issuedTokenPair.response();
    }

    public TokenResponse exchangeLoginCode(String loginCode) {
        Long userId = authTokenService.getLoginCodeUserId(loginCode);
        IssuedTokenPair issuedTokenPair = createTokenPair(findEntityById(userId));
        if (!authTokenService.exchangeLoginCode(
                loginCode,
                userId,
                issuedTokenPair.response().refreshToken(),
                issuedTokenPair.refreshTokenTtl()
        )) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_CODE);
        }
        return issuedTokenPair.response();
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        User user = saveOrUpdate(userInfo);

        Map<String, Object> customAttributes = new HashMap<>(oAuth2User.getAttributes());
        customAttributes.put("provider", registrationId);
        customAttributes.put("providerId", userInfo.getProviderId());
        customAttributes.put("role", user.getRole().getKey());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                customAttributes,
                "providerId"
        );
    }

    @Transactional
    public User saveOrUpdate(OAuth2UserInfo userInfo) {
        if (!hasText(userInfo.getProviderId())) {
            throw oauth2Exception(ErrorCode.OAUTH_INVALID_USER_INFO);
        }

        return userRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                .map(entity -> entity.update(userInfo.getName(), userInfo.getEmail()))
                .orElseGet(() -> {
                    if (!hasText(userInfo.getEmail())) {
                        throw oauth2Exception(ErrorCode.OAUTH_EMAIL_REQUIRED);
                    }
                    if (!hasText(userInfo.getName())) {
                        throw oauth2Exception(ErrorCode.OAUTH_PROFILE_REQUIRED);
                    }
                    Role initialRole = Role.USER;
                    if (adminWhiteList != null && adminWhiteList.contains(userInfo.getEmail())) {
                        initialRole = Role.ADMIN;
                    }
                    return userRepository.save(User.builder()
                            .providerId(userInfo.getProviderId())
                            .email(userInfo.getEmail())
                            .name(userInfo.getName())
                            .provider(userInfo.getProvider())
                            .role(initialRole)
                            .build());
                });
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public User findByProviderAndProviderId(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void updateUserRole(Long id, Role role) {
        User user = findEntityById(id);
        user.updateRole(role);
    }

    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public void withdraw(User user, String accessToken) {
        revokeTokens(user, accessToken);
        userRepository.deleteById(user.getId());
    }

    public void logout(User user, String accessToken) {
        revokeTokens(user, accessToken);
    }

    private OAuth2AuthenticationException oauth2Exception(ErrorCode errorCode) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(errorCode.getCode()),
                errorCode.getMessage()
        );
    }

    private IssuedTokenPair createTokenPair(User user) {
        String accessToken = tokenProvider.createAccessToken(user.getProvider(), user.getProviderId());
        String refreshToken = tokenProvider.createRefreshToken(user.getProvider(), user.getProviderId());
        Claims refreshClaims = tokenProvider.getValidatedClaims(refreshToken, TokenType.REFRESH);
        return new IssuedTokenPair(
                TokenResponse.bearer(accessToken, refreshToken),
                tokenProvider.getRemainingValidity(refreshClaims)
        );
    }

    private void revokeTokens(User user, String accessToken) {
        Claims claims = tokenProvider.getValidatedClaims(accessToken, TokenType.ACCESS);
        String provider = claims.get("provider", String.class);
        if (!user.getProviderId().equals(claims.getSubject()) || !user.getProvider().equals(provider)) {
            throw new CustomException(ErrorCode.USER_ACCESS_DENIED);
        }

        authTokenService.blacklistAccessToken(
                claims.getId(),
                tokenProvider.getRemainingValidity(claims)
        );
        authTokenService.deleteRefreshToken(user.getId());
    }

    private record IssuedTokenPair(TokenResponse response, java.time.Duration refreshTokenTtl) {
    }
}
