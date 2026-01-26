package com.hcproj.healthcareprojectbackend.auth.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import static com.hcproj.healthcareprojectbackend.global.util.UtilityProvider.normalizeNullable;

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

    public static String newHandle() {
        // "u_" + 8 chars = 10 chars (<= 20 OK)
        return "u_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static void validatePrecondition(String email, String handle, String nickname) {
        if (email == null || email.isBlank()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        if (handle == null || handle.isBlank()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        if (nickname == null || nickname.isBlank()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void changeNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        if (this.nickname.equals(nickname)) return;
        this.nickname = nickname;
    }

    public void changePhoneNumber(String phoneNumber) {
        String normalized = normalizeNullable(phoneNumber);
        if (this.phoneNumber == null && normalized == null) return;
        if (this.phoneNumber != null && this.phoneNumber.equals(normalized)) return;
        this.phoneNumber = normalized; // null이면 삭제
    }

    public void changeProfileImageUrl(String profileImageUrl) {
        String normalized = normalizeNullable(profileImageUrl);
        if (this.profileImageUrl == null && normalized == null) return;
        if (this.profileImageUrl != null && this.profileImageUrl.equals(normalized)) return;
        this.profileImageUrl = normalized; // null이면 삭제
    }

    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        markDeleted();
    }

    public void makeAdmin() {
        if (this.role == UserRole.ADMIN) return;
        this.role = UserRole.ADMIN;
    }

    public void makeTrainer() {
        if (this.role == UserRole.TRAINER) return;
        this.role = UserRole.TRAINER;
    }

    /**
     * ADMIN, TRAINER인 유저를 일반 사용자로 강등시키는 메서드
     */
    public void demote() {
        if (this.role == UserRole.USER) return;
        this.role = UserRole.USER;
    }

    public void markVerified() {
        if (emailVerified) return;
        this.emailVerified = true;
    }

    public void markUnverified() {
        if (!emailVerified) return;
        this.emailVerified = false;
    }
}
