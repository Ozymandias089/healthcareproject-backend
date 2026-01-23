package com.hcproj.healthcareprojectbackend.auth.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.mail.port.EmailSender;
import com.hcproj.healthcareprojectbackend.global.store.verification.VerificationStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import static com.hcproj.healthcareprojectbackend.global.util.UtilityProvider.normalizeEmail;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final VerificationStore verificationStore;
    private final EmailSender emailSender;

    private final SecureRandom random = new SecureRandom();

    public void sendVerificationCode(String rawEmail) {
        String email = normalizeEmail(rawEmail);

        // Cooldown
        boolean allowed = verificationStore.tryStartCooldown(email);
        if (!allowed) throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);

        // 가입된 유저만 전송
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        if (user.isEmailVerified()) return;

        String code = generate6DigitCode();
        String hash = sha256Hex(code);

        verificationStore.saveEmailCodeHash(email, hash);

        emailSender.send(
                email,
                "[HCProject] 이메일 인증 코드",
                """
                가입 이메일 인증을 진행해 주세요.

                인증 코드: %s

                본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.
                """.formatted(code)
        );
    }

    /** 인증 코드 확인 */
    @Transactional
    public void confirmVerificationCode(String rawEmail, String rawCode) {
        String email = normalizeEmail(rawEmail);
        String code = rawCode == null ? null : rawCode.trim();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        // 이미 인증 완료면 idempotent 처리
        if (user.isEmailVerified()) return;

        String savedHash = verificationStore.getEmailCodeHash(email);
        if (savedHash == null) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        String inputHash = sha256Hex(code);
        if (!inputHash.equals(savedHash)) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        // 성공 처리
        user.markVerified();
        verificationStore.deleteEmailCode(email);
        // user는 영속 상태라 @Transactional이면 자동 flush
    }

    private String generate6DigitCode() {
        int n = random.nextInt(1_000_000);
        return String.format("%06d", n);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return new String(Hex.encode(digest));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
