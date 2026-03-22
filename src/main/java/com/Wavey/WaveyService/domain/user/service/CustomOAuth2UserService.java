package com.Wavey.WaveyService.domain.user.service;

import com.Wavey.WaveyService.domain.user.dto.OAuth2UserInfo;
import com.Wavey.WaveyService.domain.user.dto.OAuth2UserInfoFactory;
import com.Wavey.WaveyService.domain.user.dto.UserResponse;
import com.Wavey.WaveyService.domain.user.entity.Role;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.global.common.JwtTokenProvider;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

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

    public Map<String, String> getLoginUrls() {
        Map<String, String> loginUrls = new HashMap<>();
        loginUrls.put("google", serverUrl + baseUrl + googlePath);
        loginUrls.put("apple", serverUrl + baseUrl + applePath);
        return loginUrls;
    }

    /**
     * 토큰 재발급 비즈니스 로직 (Refresh Token Rotation)
     * 피드백 반영: 만료된 리프레시 토큰 시 TOKEN_402(EXPIRED_TOKEN) 반환
     */
    @Transactional
    public Map<String, String> refreshToken(String refreshToken) {
        // 1. 기본 유효성 검증
        if (refreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        try {
            // 토큰 만료 여부 및 구조 검증
            tokenProvider.validateToken(refreshToken);
        } catch (ExpiredJwtException e) {
            // 리프레시 토큰이 만료된 경우 -> 프론트엔드에서 재로그인 유도 가능
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            // 그 외 유효하지 않은 토큰 처리
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 2. 유저 정보 추출
        Claims claims = tokenProvider.getClaims(refreshToken);
        String provider = (String) claims.get("provider");
        String providerId = claims.getSubject();

        User user = findByProviderAndProviderId(provider, providerId);

        // 3. DB 토큰 일치 확인 (Timing Attack 방지를 위한 MessageDigest 사용)
        String storedToken = user.getRefreshToken();
        if (storedToken == null || !MessageDigest.isEqual(
                refreshToken.getBytes(StandardCharsets.UTF_8),
                storedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 4. 새로운 토큰 생성 및 로테이션 (RTR)
        String newAccessToken = tokenProvider.createAccessToken(provider, providerId);
        String newRefreshToken = tokenProvider.createRefreshToken(provider, providerId);

        user.updateRefreshToken(newRefreshToken);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshToken);

        return response;
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
        customAttributes.put("role", user.getRole().getKey());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                customAttributes,
                "sub"
        );
    }

    @Transactional
    public User saveOrUpdate(OAuth2UserInfo userInfo) {
        return userRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                .map(entity -> entity.update(userInfo.getName(), userInfo.getEmail()))
                .orElseGet(() -> {
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

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void withdraw(Long id) {
        if (!userRepository.existsById(id)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void logout(Long id) {
        User user = findEntityById(id);
        user.updateRefreshToken(null);
    }
}