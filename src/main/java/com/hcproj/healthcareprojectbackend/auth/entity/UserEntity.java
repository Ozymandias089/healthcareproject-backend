package com.hcproj.healthcareprojectbackend.auth.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import static com.hcproj.healthcareprojectbackend.global.util.UtilityProvider.normalizeNullable;

/**
 * 애플리케이션 사용자 계정을 나타내는 엔티티.
 *
 * <p><b>주요 특징</b></p>
 * <ul>
 *   <li>로컬 회원가입/소셜 회원가입을 정적 팩토리로 분리하여 생성 정책을 명확히 한다.</li>
 *   <li>상태({@link UserStatus})와 권한({@link UserRole})을 엔티티 내부 도메인 액션으로 변경한다.</li>
 *   <li>전화번호/프로필 이미지 URL은 공백 입력을 {@code null}로 정규화하여 "삭제" 의미로 사용한다.</li>
 * </ul>
 *
 * <p><b>검증 정책</b></p>
 * <ul>
 *   <li>필수 값(email, handle, nickname)은 blank 불가</li>
 *   <li>로컬 가입은 passwordHash가 필수</li>
 * </ul>
 *
 * <p><b>DB 제약</b></p>
 * <ul>
 *   <li>email: unique</li>
 *   <li>handle: unique</li>
 * </ul>
 *
 * <p><b>식별자 전략</b></p>
 * <ul>
 *   <li>현재 {@code IDENTITY} 사용 (H2 호환)</li>
 *   <li>Oracle 전환 시 {@code SEQUENCE} 전략으로 변경 필요</li>
 * </ul>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "users")
public class UserEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // H2에선 OK / Oracle 전환 시 SEQUENCE로 변경 필요
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "handle", nullable = false, unique = true, length = 20)
    private String handle;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /**
     * 로컬 회원가입용 사용자 엔티티를 생성한다.
     *
     * <p>
     * 로컬 계정은 비밀번호 해시를 필수로 가지며,
     * 이메일 인증 여부는 기본적으로 미인증(false) 상태로 시작한다.
     * </p>
     *
     * @param email           이메일(필수)
     * @param handle          핸들(필수)
     * @param passwordHash    비밀번호 해시(필수)
     * @param nickname        닉네임(필수)
     * @param phoneNumber     전화번호(선택, 공백은 null 처리)
     * @param profileImageUrl 프로필 이미지 URL(선택, 공백은 null 처리)
     * @return 신규 사용자 엔티티
     */
    public static UserEntity localRegister(
            String email,
            String handle,
            String passwordHash,
            String nickname,
            String phoneNumber,
            String profileImageUrl
    ) {
        validatePrecondition(email, handle, nickname);
        if (passwordHash == null || passwordHash.isBlank()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        return UserEntity.builder()
                .email(email)
                .handle(handle)
                .passwordHash(passwordHash)
                .nickname(nickname)
                .phoneNumber(normalizeNullable(phoneNumber))
                .profileImageUrl(normalizeNullable(profileImageUrl))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();
    }

    /**
     * 소셜 회원가입용 사용자 엔티티를 생성한다.
     *
     * <p>
     * 소셜 계정은 애플리케이션 로컬 비밀번호를 가지지 않으며(passwordHash = null),
     * 제공자 측에서 이메일이 검증되었다고 가정하여 기본적으로 인증 완료(true)로 설정한다.
     * </p>
     *
     * @param email           이메일(필수)
     * @param handle          핸들(필수)
     * @param nickname        닉네임(필수)
     * @param phoneNumber     전화번호(선택, 공백은 null 처리)
     * @param profileImageUrl 프로필 이미지 URL(선택, 공백은 null 처리)
     * @return 신규 사용자 엔티티
     */
    public static UserEntity socialRegister(
            String email,
            String handle,
            String nickname,
            String phoneNumber,
            String profileImageUrl
    ) {
        validatePrecondition(email, handle, nickname);

        return UserEntity.builder()
                .email(email)
                .handle(handle)
                .passwordHash(null) // 소셜은 비밀번호 없음
                .nickname(nickname)
                .phoneNumber(normalizeNullable(phoneNumber))
                .profileImageUrl(normalizeNullable(profileImageUrl))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true) // 소셜은 provider에서 검증된 이메일로 간주
                .build();
    }

    public static UserEntity registerAdmin(
            String email,
            String handle,
            String passwordHash,
            String nickname,
            String profileImageUrl
    ){
        validatePrecondition(email, handle, nickname);
        if (passwordHash == null || passwordHash.isBlank()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        return UserEntity.builder()
                .email(email)
                .handle(handle)
                .passwordHash(passwordHash)
                .nickname(nickname)
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .profileImageUrl(profileImageUrl)
                .emailVerified(true)
                .build();
    }

    /**
     * 신규 사용자 핸들을 생성한다.
     *
     * <p>
     * 형식: {@code u_ + 8자리} (총 길이 10, 컬럼 길이 20 이하)
     * </p>
     *
     * @return 생성된 핸들 문자열
     */
    public static String newHandle() {
        // "u_" + 8 chars = 10 chars (<= 20 OK)
        return "u_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static void validatePrecondition(String email, String handle, String nickname) {
        if (email == null || email.isBlank()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        if (handle == null || handle.isBlank()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        if (nickname == null || nickname.isBlank()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    /**
     * 비밀번호 해시를 변경한다.
     *
     * <p>
     * 정책(예: 로컬 계정만 허용 여부, 해시 형식 검증)은 상위 서비스에서 담당한다.
     * </p>
     *
     * @param passwordHash 변경할 비밀번호 해시
     */
    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * 닉네임을 변경한다.
     *
     * @param nickname 변경할 닉네임(빈 문자열 불가)
     * @throws BusinessException 입력값이 유효하지 않은 경우
     */
    public void changeNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        if (this.nickname.equals(nickname)) return;
        this.nickname = nickname;
    }

    /**
     * 전화번호를 변경(또는 삭제)한다.
     *
     * <p>
     * 공백 입력은 {@code null}로 정규화되어 "삭제"로 처리된다.
     * </p>
     *
     * @param phoneNumber 변경할 전화번호(공백/빈 문자열이면 삭제)
     */
    public void changePhoneNumber(String phoneNumber) {
        String normalized = normalizeNullable(phoneNumber);
        if (this.phoneNumber == null && normalized == null) return;
        if (this.phoneNumber != null && this.phoneNumber.equals(normalized)) return;
        this.phoneNumber = normalized; // null이면 삭제
    }

    /**
     * 프로필 이미지 URL을 변경(또는 삭제)한다.
     *
     * <p>
     * 공백 입력은 {@code null}로 정규화되어 "삭제"로 처리된다.
     * </p>
     *
     * @param profileImageUrl 변경할 URL(공백/빈 문자열이면 삭제)
     */
    public void changeProfileImageUrl(String profileImageUrl) {
        String normalized = normalizeNullable(profileImageUrl);
        if (this.profileImageUrl == null && normalized == null) return;
        if (this.profileImageUrl != null && this.profileImageUrl.equals(normalized)) return;
        this.profileImageUrl = normalized; // null이면 삭제
    }

    /**
     * 회원 탈퇴 처리한다.
     *
     * <p>
     * 상태를 {@link UserStatus#WITHDRAWN}로 변경하고,
     * 공통 소프트 삭제 정책({@link BaseTimeEntity#markDeleted()})을 적용한다.
     * </p>
     */
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        markDeleted();
    }

    /** 사용자를 관리자 권한으로 승격한다. (이미 ADMIN이면 무시) */
    public void makeAdmin() {
        if (this.role == UserRole.ADMIN) return;
        this.role = UserRole.ADMIN;
    }

    /** 사용자를 트레이너 권한으로 승격한다. (이미 TRAINER이면 무시) */
    public void makeTrainer() {
        if (this.role == UserRole.TRAINER) return;
        this.role = UserRole.TRAINER;
    }

    /**
     * 관리자/트레이너 권한을 일반 사용자로 강등한다.
     *
     * <p>
     * USER인 경우는 무시한다.
     * </p>
     */
    public void demote() {
        if (this.role == UserRole.USER) return;
        this.role = UserRole.USER;
    }

    /** 이메일 인증 완료 상태로 변경한다. */
    public void markVerified() {
        if (emailVerified) return;
        this.emailVerified = true;
    }

    /** 이메일 미인증 상태로 변경한다. */
    public void markUnverified() {
        if (!emailVerified) return;
        this.emailVerified = false;
    }

    /**
     * 사용자 상태를 갱신한다.
     *
     * @param newStatus 변경할 상태
     */
    public void updateStatus(UserStatus newStatus) {
        if (this.status == newStatus) return; // 이미 같은 상태면 무시
        this.status = newStatus;
    }

    // UserEntity
    public void reactivateFromSocial(String nickname, String profileImageUrl) {
        this.status = UserStatus.ACTIVE;
        undoDeletion();

        // 닉네임/프로필은 정책에 따라 갱신(원하면 조건부로만 갱신)
        if (nickname != null && !nickname.isBlank()) this.nickname = nickname;
        if (profileImageUrl != null && !profileImageUrl.isBlank()) this.profileImageUrl = profileImageUrl;

        // 소셜로 재가입이면 이메일은 보통 검증된 것으로 취급
        this.emailVerified = true;
    }

}
