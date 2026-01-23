package com.hcproj.healthcareprojectbackend.auth.service;

import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.mail.port.EmailSender;
import com.hcproj.healthcareprojectbackend.global.store.resetpassword.PasswordResetStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final PasswordResetStore passwordResetStore;
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
            // tokenId 생성
            String tokenId = UUID.randomUUID().toString();

            // Redis 저장 (value는 userId 또는 email 중 택1, 최소정보 추천: userId)
            passwordResetStore.saveToken(tokenId, String.valueOf(user.getId()));

            // 링크 생성
            String link = buildResetLink(tokenId, email);

            // 메일 발송
            try {
                emailSender.send(
                        email,
                        "[HCProject] 비밀번호 재설정 안내",
                        """
                        비밀번호 재설정을 요청하셨습니다.

                        아래 링크를 통해 비밀번호를 재설정해 주세요:
                        %s

                        본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.
                        """.formatted(link)
                );
            } catch (Exception e) {
                // 가입 여부 숨김 정책을 유지하기 위해, 외부로는 동일 응답이 나가게 하고
                // 서버 내부에서는 로깅만 (단, 스펙에 500이 있으니 "정책적으로" 500 낼지 선택 필요)
                log.error("Password reset mail send failed. email={}", email, e);
                throw e; // 스펙대로 500을 내고 싶으면 throw 유지
                // throw 하지 않고 삼키고 싶으면 return; (하지만 스펙엔 500이 있으니 지금은 throw)
            }
        });

        // 3) 항상 성공 응답(컨트롤러에서 ok 반환)
    }
    private String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
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
}
