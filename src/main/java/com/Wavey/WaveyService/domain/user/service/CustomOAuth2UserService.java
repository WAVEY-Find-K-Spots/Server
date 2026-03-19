package com.Wavey.WaveyService.domain.user.service;

import com.Wavey.WaveyService.domain.user.dto.OAuth2UserInfo;
import com.Wavey.WaveyService.domain.user.dto.OAuth2UserInfoFactory;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    // 환경 변수 주입
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

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        saveOrUpdate(userInfo);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                userInfo.getAttributes(),
                "sub"
        );
    }

    @Transactional
    public User saveOrUpdate(OAuth2UserInfo userInfo) {
        return userRepository.findByProviderId(userInfo.getProviderId())
                .map(entity -> entity.update(userInfo.getName(), userInfo.getEmail()))
                .orElseGet(() -> userRepository.save(User.builder()
                        .providerId(userInfo.getProviderId())
                        .email(userInfo.getEmail())
                        .name(userInfo.getName())
                        .provider(userInfo.getProvider())
                        .build()));
    }

    public List<User> findAllUsers() { return userRepository.findAll(); }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("회원 없음"));
    }

    @Transactional
    public void withdraw(Long id) {
        userRepository.deleteById(id);
    }
}