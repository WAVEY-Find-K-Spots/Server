package com.Wavey.WaveyService.service;

import com.Wavey.WaveyService.dto.OAuthDto;
import com.Wavey.WaveyService.entity.User;
import com.Wavey.WaveyService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthDto oAuthDto = OAuthDto.ofGoogle(registrationId, oAuth2User.getAttributes());

        // DB 저장 혹은 업데이트 로직 실행
        saveOrUpdate(oAuthDto);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oAuthDto.getAttributes(),
                "sub"
        );
    }

    private User saveOrUpdate(OAuthDto oAuthDto) {
        User user = userRepository.findBySub(oAuthDto.getSub())
                .map(entity -> entity.update(oAuthDto.getName(), oAuthDto.getEmail())) // 있으면 업데이트
                .orElse(User.builder() // 없으면 신규 생성
                        .sub(oAuthDto.getSub())
                        .email(oAuthDto.getEmail())
                        .name(oAuthDto.getName())
                        .provider(oAuthDto.getProvider())
                        .build());

        return userRepository.save(user);
    }
}