package com.hcproj.healthcareprojectbackend.auth.service;

import com.hcproj.healthcareprojectbackend.auth.dto.request.*;
import com.hcproj.healthcareprojectbackend.auth.dto.response.EmailCheckResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.response.TokenResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.security.jwt.JwtProperties;
import com.hcproj.healthcareprojectbackend.global.security.jwt.JwtTokenProvider;
import com.hcproj.healthcareprojectbackend.global.security.jwt.RefreshTokenStore;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenStore refreshTokenStore;

    @Transactional
    public TokenResponseDTO signup(SignupRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }

        // handle 정책이 아직이면 임시 생성(팀 정책 정해지면 교체)
        String handle = "u_" + UUID.randomUUID().toString().substring(0, 8);

        UserEntity user = UserEntity.builder()
                .email(request.email())
                .handle(handle)
                .passwordHash(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .phoneNumber(request.phoneNumber())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .profileImageUrl(request.profileImageUrl())
                .build();

        UserEntity saved = userRepository.save(user);
        return issueTokens(saved.getId(), saved.getHandle(), saved.getRole().name());
    }

    @Transactional(readOnly = true)
    public EmailCheckResponseDTO checkEmailDuplicate(EmailCheckRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }
        return new EmailCheckResponseDTO(true);
    }

    @Transactional(readOnly = true)
    public TokenResponseDTO login(LoginRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        return issueTokens(user.getId(), user.getHandle(), user.getRole().name());
    }

    @Transactional
    public TokenResponseDTO reissue(TokenReissueRequestDTO request) {
        Claims claims = parseRefreshClaimsOrThrow(request.refreshToken());

        long userId = extractUserIdOrThrow(claims);
        String handle = claims.getSubject();
        String role = String.valueOf(claims.get("role"));
        String oldJti = extractJtiOrThrow(claims);

        // whitelist 확인
        if (!refreshTokenStore.exists(userId, oldJti)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 회전: 기존 refresh 무효화
        refreshTokenStore.delete(userId, oldJti);

        // 새 토큰 발급 + 저장
        return issueTokens(userId, handle, role);
    }

    @Transactional
    public void logout(LogoutRequestDTO request) {
        Claims claims = parseRefreshClaimsOrThrow(request.refreshToken());

        long userId = extractUserIdOrThrow(claims);
        String jti = extractJtiOrThrow(claims);

        // idempotent
        refreshTokenStore.delete(userId, jti);
    }

    // -------------------------
    // Private helpers
    // -------------------------

    private TokenResponseDTO issueTokens(long userId, String handle, String role) {
        String at = jwtTokenProvider.createAccessToken(userId, handle, role);
        String rJti = UUID.randomUUID().toString();
        String rt =  jwtTokenProvider.createRefreshToken(userId, handle, role, rJti);
        refreshTokenStore.save(userId, rJti, jwtProperties.refreshTokenValiditySeconds());
        return TokenResponseDTO.builder()
                .accessToken(at)
                .refreshToken(rt)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenValiditySeconds())
                .build();
    }

    private Claims parseRefreshClaimsOrThrow(String refreshToken) {
        jwtTokenProvider.validate(refreshToken);
        return jwtTokenProvider.parseClaims(refreshToken);
    }

    private long extractUserIdOrThrow(Claims claims) {
        Object uidObj = claims.get("uid");
        if (!(uidObj instanceof Number n)) throw new BusinessException(ErrorCode.INVALID_TOKEN);
        return n.longValue();
    }

    private String extractJtiOrThrow(Claims claims) {
        String jti = claims.getId();
        if (jti == null || jti.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return jti;
    }
}