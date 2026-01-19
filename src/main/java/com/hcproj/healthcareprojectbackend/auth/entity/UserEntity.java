package com.hcproj.healthcareprojectbackend.auth.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        markDeleted();
    }
}
