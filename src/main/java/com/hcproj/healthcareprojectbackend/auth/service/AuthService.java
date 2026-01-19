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

        String access = jwtTokenProvider.createAccessToken(saved.getId(), saved.getHandle(), saved.getRole().name());
        String refreshJti = UUID.randomUUID().toString();
        String refresh = jwtTokenProvider.createRefreshToken(saved.getId(), saved.getHandle(), saved.getRole().name(), refreshJti);
        refreshTokenStore.save(saved.getId(), refreshJti, jwtProperties.refreshTokenValiditySeconds());

        return TokenResponseDTO.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenValiditySeconds())
                .build();
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

        String access = jwtTokenProvider.createAccessToken(user.getId(), user.getHandle(), user.getRole().name());
        String refreshJti = UUID.randomUUID().toString();
        String refresh = jwtTokenProvider.createRefreshToken(user.getId(), user.getHandle(), user.getRole().name(), refreshJti);
        refreshTokenStore.save(user.getId(), refreshJti, jwtProperties.refreshTokenValiditySeconds());

        return TokenResponseDTO.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenValiditySeconds())
                .build();
    }

    @Transactional
    public TokenResponseDTO reissue(TokenReissueRequestDTO request) {
        String oldRefresh = request.refreshToken();

        jwtTokenProvider.validate(oldRefresh);
        Claims claims = jwtTokenProvider.parseClaims(oldRefresh);

        Object uidObj = claims.get("uid");
        if (!(uidObj instanceof Number n)) throw new BusinessException(ErrorCode.INVALID_TOKEN);
        long userId = n.longValue();

        String handle = claims.getSubject();
        String role = String.valueOf(claims.get("role"));

        String oldJti = claims.getId();
        if (oldJti == null || oldJti.isBlank()) throw new BusinessException(ErrorCode.INVALID_TOKEN);

        // ✅ Redis에 존재해야만 재발급 가능
        if (!refreshTokenStore.exists(userId, oldJti)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN); // 또는 REVOKED_TOKEN 같은 코드
        }

        // ✅ 회전: 기존 refresh 무효화
        refreshTokenStore.delete(userId, oldJti);

        // ✅ 새 토큰 발급 + 새 refresh 저장
        String newAccess = jwtTokenProvider.createAccessToken(userId, handle, role);

        String newJti = UUID.randomUUID().toString();
        String newRefresh = jwtTokenProvider.createRefreshToken(userId, handle, role, newJti);
        refreshTokenStore.save(userId, newJti, jwtProperties.refreshTokenValiditySeconds());

        return TokenResponseDTO.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenValiditySeconds())
                .build();
    }


    @Transactional
    public void logout(LogoutRequestDTO request) {
        String rt = request.refreshToken();

        jwtTokenProvider.validate(rt);
        Claims claims = jwtTokenProvider.parseClaims(rt);

        Object uidObj = claims.get("uid");
        if (!(uidObj instanceof Number n)) throw new BusinessException(ErrorCode.INVALID_TOKEN);
        long userId = n.longValue();

        String jti = claims.getId();
        if (jti == null || jti.isBlank()) throw new BusinessException(ErrorCode.INVALID_TOKEN);
        refreshTokenStore.delete(userId, jti);
    }
}