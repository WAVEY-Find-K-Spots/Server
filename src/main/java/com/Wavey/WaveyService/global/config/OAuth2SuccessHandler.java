package com.Wavey.WaveyService.global.config;

import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.global.common.JwtTokenProvider;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String providerId = oAuth2User.getAttribute("sub");
        String provider = oAuth2User.getAttribute("provider");

        if (providerId == null || provider == null) {
            throw new IllegalStateException("Missing required OAuth2 attributes: sub or provider");
        }
        // 1. 액세스 및 리프레시 토큰 생성
        String accessToken = tokenProvider.createAccessToken(provider, providerId);
        String refreshToken = tokenProvider.createRefreshToken(provider, providerId);

        // 2. DB의 유저 엔티티에 리프레시 토큰 업데이트 (검증용)
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateRefreshToken(refreshToken);
/*
        // 3. 프론트엔드로 두 토큰을 모두 전달하며 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/swagger-ui/index.html")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    */
// 3. 테스트 최적화용 HTML 응답
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(String.format(
                "<html><head><style>" +
                        "body { font-family: 'Segoe UI', sans-serif; padding: 40px; background-color: #f4f7f6; }" +
                        ".card { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); max-width: 800px; margin: auto; }" +
                        "h2 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }" +
                        "h3 { margin-top: 25px; font-size: 16px; color: #7f8c8d; }" +
                        "textarea { width: 100%%; height: 70px; padding: 10px; border: 1px solid #ddd; border-radius: 6px; background: #f9f9f9; font-family: monospace; resize: none; }" +
                        "code { background: #eee; padding: 2px 5px; border-radius: 4px; color: #e74c3c; }" +
                        ".btn { display: inline-block; margin-top: 20px; padding: 10px 20px; background: #3498db; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; border: none; cursor: pointer; }" +
                        ".btn-copy { background: #2ecc71; margin-left: 5px; }" +
                        "</style></head><body>" +
                        "<div class='card'>" +
                        "<h2>WAVEY API 인증 테스트 도구</h2>" +
                        "<p><strong>사용자:</strong> %s (권한: %s)</p>" +
                        "<hr>" +
                        "<h3>1️⃣ Access Token (Swagger Authorize용)</h3>" +
                        "<textarea id='access'>%s</textarea>" +
                        "<button class='btn btn-copy' onclick=\"copyText('access')\">Access Token 복사</button>" +
                        "<h3>2️⃣ Refresh Token (재발급 API 테스트용 Body)</h3>" +
                        "<p>아래 내용을 그대로 <code>POST /api/auth/refresh</code>의 <b>Request Body</b>에 붙여넣으세요.</p>" +
                        "<textarea id='refresh-json'>{\n  \"refreshToken\": \"%s\"\n}</textarea>" +
                        "<button class='btn btn-copy' onclick=\"copyText('refresh-json')\">JSON Body 복사</button>" +
                        "<br><br>" +
                        "<a href='/swagger-ui/index.html' class='btn'>Swagger로 돌아가기</a>" +
                        "</div>" +
                        "<script>" +
                        "function copyText(id) { var copyText = document.getElementById(id); copyText.select(); document.execCommand('copy'); alert('복사되었습니다!'); }" +
                        "</script>" +
                        "</body></html>",
                HtmlUtils.htmlEscape(user.getName()),
                user.getRole().name(),
                HtmlUtils.htmlEscape(accessToken),
                HtmlUtils.htmlEscape(refreshToken)
        ));
    }

}