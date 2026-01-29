package com.hcproj.healthcareprojectbackend.auth.service;

import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.mail.port.EmailSender;
import com.hcproj.healthcareprojectbackend.global.mail.template.EmailTemplateLoader;
import com.hcproj.healthcareprojectbackend.global.store.resetpassword.PasswordResetStore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static com.hcproj.healthcareprojectbackend.global.util.UtilityProvider.normalizeEmail;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final EmailTemplateLoader templateLoader;
    private final PasswordResetStore passwordResetStore;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redis;

    // 프론트 reset 페이지 URL (예: https://your-frontend.com/reset-password)
    @Value("${app.frontend.reset-password-url:}")
    private String resetPasswordBaseUrl;

    // 이메일 단위 레이트리밋(쿨다운) - 스펙상 429 지원
    @Value("${app.reset-password.cooldown-seconds:60}")
    private long cooldownSeconds;

    public void requestPasswordResetMail(String rawEmail) {
        String email = normalizeEmail(rawEmail);

        // 1) 레이트리밋(이메일 단위)
        // 키 설계: reset:pw:cooldown:{email}
        String cooldownKey = "reset:pw:cooldown:" + email;
        Boolean allowed = redis.opsForValue().setIfAbsent(
                cooldownKey,
                "1",
                Duration.ofSeconds(cooldownSeconds)
        );

        if (!Boolean.TRUE.equals(allowed)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }

        // 2) 가입 여부 확인 (존재하지 않아도 항상 성공 응답)
        userRepository.findByEmail(email).ifPresent(user -> {
            String tokenId = UUID.randomUUID().toString();
            passwordResetStore.saveToken(tokenId, String.valueOf(user.getId()));

            String link = buildResetLink(tokenId, email);

            String html = templateLoader.render(
                    "mail/password-reset.html",
                    Map.of("link", link)
            );

            try {
                emailSender.sendHtml(email, "[HCProject] 비밀번호 재설정 안내", html);
            } catch (Exception e) {
                log.error("Password reset mail send failed. email={}", email, e);
                throw e;
            }
        });

        // 3) 항상 성공 응답(컨트롤러에서 ok 반환)
    }

    private String buildResetLink(String tokenId, String email) {
        // 프론트 URL이 비어있으면 일단 토큰만 보내는 방식으로 fallback
        if (resetPasswordBaseUrl == null || resetPasswordBaseUrl.isBlank()) {
            return "token=" + tokenId;
        }

        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String encodedToken = URLEncoder.encode(tokenId, StandardCharsets.UTF_8);

        // 예시: https://frontend/reset-password?token=...&email=...
        String sep = resetPasswordBaseUrl.contains("?") ? "&" : "?";
        return resetPasswordBaseUrl + sep + "token=" + encodedToken + "&email=" + encodedEmail;
    }

    @Transactional
    public void resetPassword(String rawToken, String rawEmail, String newPassword) {
        String token = rawToken == null ? null : rawToken.trim();
        String email = rawEmail == null ? null : rawEmail.trim().toLowerCase();

        // 1) token으로 Redis 조회
        String value = passwordResetStore.getTokenValue(token);
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT); // 또는 TOKEN_INVALID 같은 코드
        }

        long userId;
        try {
            userId = Long.parseLong(value);
        } catch (NumberFormatException e) {
            // 저장값이 예상 포맷이 아니면 방어적으로 실패 처리
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        // 2) 사용자 조회
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        // 3) email 일치 확인(권장)
        // 링크에 email도 같이 줬으니, 토큰 탈취 같은 경우 1차 방어가 됨
        if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(email)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // 4) 비밀번호 변경
        String encoded = passwordEncoder.encode(newPassword);
        user.changePasswordHash(encoded); // <- 엔티티 메서드명에 맞게 바꿔

        // 5) 토큰 삭제(재사용 방지)
        passwordResetStore.deleteToken(token);

    }
}
