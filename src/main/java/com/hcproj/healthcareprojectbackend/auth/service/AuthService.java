package com.hcproj.healthcareprojectbackend.auth.service;

import com.hcproj.healthcareprojectbackend.auth.dto.request.EmailCheckRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.request.LoginRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.request.SignupRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.request.TokenReissueRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.response.TokenResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.security.jwt.JwtProperties;
import com.hcproj.healthcareprojectbackend.global.security.jwt.JwtTokenProvider;
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
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .profileImageUrl(request.profileImageUrl())
                .build();

        UserEntity saved = userRepository.save(user);

        String access = jwtTokenProvider.createAccessToken(saved.getId(), saved.getHandle(), saved.getRole().name());
        String refresh = jwtTokenProvider.createRefreshToken(saved.getId(), saved.getHandle(), saved.getRole().name());

        return TokenResponseDTO.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenValiditySeconds())
                .build();
    }

    @Transactional(readOnly = true)
    public void checkEmailDuplicate(EmailCheckRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }
    }

    @Transactional(readOnly = true)
    public TokenResponseDTO login(LoginRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String access = jwtTokenProvider.createAccessToken(user.getId(), user.getHandle(), user.getRole().name());
        String refresh = jwtTokenProvider.createRefreshToken(user.getId(), user.getHandle(), user.getRole().name());

        return TokenResponseDTO.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenValiditySeconds())
                .build();
    }

    @Transactional
    public TokenResponseDTO reissue(TokenReissueRequestDTO request) {
        jwtTokenProvider.validate(request.refreshToken());
        var auth = jwtTokenProvider.getAuthentication(request.refreshToken());

        String handle = (String) auth.getPrincipal(); // ✅ principal = handle
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        Long userId = null;
        Object details = auth.getDetails();           // ✅ details = uid
        if (details instanceof Number n) {
            userId = n.longValue();
        }

        // uid가 없으면(예: 구버전 토큰) handle로 조회 fallback도 가능
        if (userId == null) {
            // userId = userRepository.findByHandle(handle).orElseThrow(...).getId();
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String newAccess = jwtTokenProvider.createAccessToken(userId, handle, role);
        String newRefresh = jwtTokenProvider.createRefreshToken(userId, handle, role);

        return TokenResponseDTO.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenValiditySeconds())
                .build();
    }

    @Transactional
    public void logout(String authorizationHeader) {
        // MVP: refreshToken 저장 전략 전에는 실질적 무효화가 어려움
        // 추후: refreshToken DB 저장 + 삭제
    }
}