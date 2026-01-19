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

/**
 * 인증/인가(Auth) 관련 유스케이스를 담당하는 서비스.
 *
 * <p><b>토큰 전략</b></p>
 * <ul>
 *   <li>Access Token: 짧은 만료 시간(서버 저장 X)</li>
 *   <li>Refresh Token: Redis에 whitelist 방식으로 저장(=유효한 토큰만 저장)</li>
 * </ul>
 *
 * <p><b>Refresh Token whitelist + 회전(Rotation)</b></p>
 * <ul>
 *   <li>로그인/회원가입/재발급 시 refreshToken(jti 포함)을 발급하고 Redis에 저장한다.</li>
 *   <li>재발급 시 기존 refreshToken(jti)이 Redis에 존재하는지 확인한다.</li>
 *   <li>재발급 성공 시 기존 refreshToken을 Redis에서 삭제(무효화)하고 새 refreshToken을 발급/저장한다.</li>
 *   <li>로그아웃 시 refreshToken(jti)을 Redis에서 삭제하여 이후 재발급을 차단한다.</li>
 * </ul>
 *
 * <p><b>에러 처리 원칙</b></p>
 * <ul>
 *   <li>JWT 위변조/형식 오류/Redis에 없는(폐기된) refreshToken: {@link ErrorCode#INVALID_TOKEN}</li>
 *   <li>JWT 만료: {@link ErrorCode#EXPIRED_TOKEN}</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenStore refreshTokenStore;

    /**
     * 일반 회원가입.
     *
     * <p>이메일/비밀번호 기반으로 사용자를 생성하고, 즉시 로그인에 필요한 토큰을 반환한다.</p>
     * <ul>
     *   <li>role = USER</li>
     *   <li>status = ACTIVE</li>
     *   <li>handle은 현재 임시 정책(UUID 일부)으로 생성</li>
     * </ul>
     *
     * @throws BusinessException 이메일이 이미 존재하는 경우 {@link ErrorCode#EMAIL_DUPLICATED}
     */
    @Transactional
    public TokenResponseDTO signup(SignupRequestDTO request) {
        // 이메일은 unique 제약이므로 사전 중복 체크 (UX + 의미있는 에러코드 반환 목적)
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }

        // handle 임시값 사용
        String handle = "u_" + UUID.randomUUID().toString().substring(0, 8);

        // 비밀번호는 평문 저장 금지 -> BCrypt 등으로 해시 처리
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

        // 회원가입 직후 로그인까지 완료한 경험 제공(Access/Refresh 반환)
        return issueTokens(saved.getId(), saved.getHandle(), saved.getRole().name());
    }

    /**
     * 이메일 중복 체크.
     *
     * <p>회원가입 전 이메일이 이미 사용 중인지 확인한다.</p>
     *
     * @throws BusinessException 이메일이 이미 존재하는 경우 {@link ErrorCode#EMAIL_DUPLICATED}
     */
    @Transactional(readOnly = true)
    public EmailCheckResponseDTO checkEmailDuplicate(EmailCheckRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }
        return new EmailCheckResponseDTO(true);
    }

    /**
     * 로그인.
     *
     * <p>이메일/비밀번호 검증 후 토큰을 발급한다.</p>
     *
     * @throws BusinessException 이메일이 없거나 비밀번호 불일치 시 {@link ErrorCode#LOGIN_FAILED}
     */
    @Transactional(readOnly = true)
    public TokenResponseDTO login(LoginRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        return issueTokens(user.getId(), user.getHandle(), user.getRole().name());
    }

    /**
     * 토큰 재발급.
     *
     * <p>Refresh Token으로 Access Token / Refresh Token을 재발급한다.</p>
     * <p><b>중요:</b> Refresh Token은 Redis whitelist에 존재해야만 재발급 가능하다.</p>
     * <p>재발급 성공 시 기존 refreshToken은 폐기(회전)하고 새 refreshToken을 저장한다.</p>
     *
     * @throws BusinessException
     * <ul>
     *   <li>Refresh Token 위변조/형식 오류/Redis에 없음: {@link ErrorCode#INVALID_TOKEN}</li>
     *   <li>Refresh Token 만료: {@link ErrorCode#EXPIRED_TOKEN}</li>
     * </ul>
     */
    @Transactional
    public TokenResponseDTO reissue(TokenReissueRequestDTO request) {
        // 1) Refresh Token 검증(서명/만료) + Claims 파싱
        Claims claims = parseRefreshClaimsOrThrow(request.refreshToken());

        // 2) Claims에서 필요한 정보 추출
        long userId = extractUserIdOrThrow(claims);
        String handle = claims.getSubject();
        String role = String.valueOf(claims.get("role"));
        String oldJti = extractJtiOrThrow(claims);

        // 3) Redis whitelist 확인: 저장소에 없으면 이미 로그아웃/회전/폐기된 토큰으로 간주
        if (!refreshTokenStore.exists(userId, oldJti)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 4) 회전(Rotation): 기존 refresh 무효화
        //    - 여기서 delete를 먼저 하는 이유:
        //      재발급 응답 전 탈취자가 같은 refresh를 재사용하는 것을 방지(재사용 공격 감소)
        refreshTokenStore.delete(userId, oldJti);

        // 5) 새 토큰 발급 + 새 refresh 저장
        return issueTokens(userId, handle, role);
    }

    /**
     * 로그아웃.
     *
     * <p>클라이언트가 보유한 Refresh Token을 무효화하여 이후 재발급을 차단한다.</p>
     * <p>구현상으로는 refreshToken의 jti를 기준으로 Redis whitelist에서 삭제한다.</p>
     *
     * <p><b>idempotent</b>하게 처리한다:
     * 이미 삭제된 토큰으로 로그아웃을 시도해도 결과는 동일(성공)하게 처리할 수 있다.</p>
     *
     * @throws BusinessException refreshToken 검증 실패 시
     * <ul>
     *   <li>위변조/형식 오류: {@link ErrorCode#INVALID_TOKEN}</li>
     *   <li>만료: {@link ErrorCode#EXPIRED_TOKEN}</li>
     * </ul>
     */
    @Transactional
    public void logout(LogoutRequestDTO request) {
        // Refresh Token 검증 + Claims 파싱
        Claims claims = parseRefreshClaimsOrThrow(request.refreshToken());

        // uid/jti 추출

        long userId = extractUserIdOrThrow(claims);
        String jti = extractJtiOrThrow(claims);

        // Redis whitelist에서 제거 -> 이후 동일 refreshToken은 재발급 불가
        refreshTokenStore.delete(userId, jti);
    }

    // -------------------------
    // Private helpers
    // -------------------------

    /**
     * Access/Refresh 토큰을 발급하고, Refresh Token을 Redis whitelist에 저장한다.
     *
     * <p>Refresh Token에는 고유 식별자인 jti를 포함한다.</p>
     * <ul>
     *   <li>Redis Key 예시: rt:{userId}:{jti}</li>
     *   <li>TTL: refreshTokenValiditySeconds와 동일</li>
     * </ul>
     */
    private TokenResponseDTO issueTokens(long userId, String handle, String role) {

        // Access Token: 서버 저장 X (짧은 만료)
        String at = jwtTokenProvider.createAccessToken(userId, handle, role);

        // Refresh Token: jti 생성 후 토큰에 포함, Redis에 저장(whitelist)
        String rJti = UUID.randomUUID().toString();
        String rt =  jwtTokenProvider.createRefreshToken(userId, handle, role, rJti);

        // Refresh TTL은 토큰 exp와 동일하게 가져간다(만료 후 자동 삭제)
        refreshTokenStore.save(userId, rJti, jwtProperties.refreshTokenValiditySeconds());
        return TokenResponseDTO.builder()
                .accessToken(at)
                .refreshToken(rt)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenValiditySeconds())
                .build();
    }

    /**
     * Refresh Token을 검증한 뒤 Claims를 반환한다.
     *
     * <p>validate()에서 만료/위변조를 구분하여 BusinessException을 던진다.</p>
     */
    private Claims parseRefreshClaimsOrThrow(String refreshToken) {
        jwtTokenProvider.validate(refreshToken);
        return jwtTokenProvider.parseClaims(refreshToken);
    }

    /**
     * Claims에서 uid(userId)를 추출한다.
     *
     * @throws BusinessException uid가 없거나 숫자형이 아닌 경우 {@link ErrorCode#INVALID_TOKEN}
     */
    private long extractUserIdOrThrow(Claims claims) {
        Object uidObj = claims.get("uid");
        if (!(uidObj instanceof Number n)) throw new BusinessException(ErrorCode.INVALID_TOKEN);
        return n.longValue();
    }

    /**
     * Claims에서 jti(토큰 고유 ID)를 추출한다.
     *
     * @throws BusinessException jti가 없거나 빈 값인 경우 {@link ErrorCode#INVALID_TOKEN}
     */
    private String extractJtiOrThrow(Claims claims) {
        String jti = claims.getId();
        if (jti == null || jti.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return jti;
    }
}