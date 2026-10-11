package com.Wavey.WaveyService.domain.user.service;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalPasswordResetMailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${auth.local-password-reset.from:}")
    private String from;

    @Value("${auth.local-password-reset.frontend-url:}")
    private String frontendUrl;

    public void ensureConfigured() {
        if (!StringUtils.hasText(smtpHost)
                || !StringUtils.hasText(from)
                || !StringUtils.hasText(frontendUrl)
                || mailSenderProvider.getIfAvailable() == null) {
            throw new CustomException(ErrorCode.LOCAL_PASSWORD_RESET_MAIL_NOT_CONFIGURED);
        }
    }

    public boolean sendResetLink(String recipientEmail, String recipientName, String rawToken) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return false;
        }

        String separator = frontendUrl.contains("?") ? "&" : "?";
        String resetUrl = frontendUrl + separator + "token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipientEmail);
        message.setSubject("[WAVEY] 비밀번호 재설정 안내");
        message.setText("안녕하세요, " + recipientName + "님.\n\n"
                + "아래 링크에서 새 비밀번호를 설정해 주세요.\n"
                + resetUrl + "\n\n"
                + "이 링크는 제한된 시간 동안 한 번만 사용할 수 있습니다.\n"
                + "본인이 요청하지 않았다면 이 메일을 무시해 주세요.");

        try {
            mailSender.send(message);
            return true;
        } catch (MailException exception) {
            log.error("Password reset email delivery failed ({})", exception.getClass().getSimpleName());
            return false;
        }
    }
}
