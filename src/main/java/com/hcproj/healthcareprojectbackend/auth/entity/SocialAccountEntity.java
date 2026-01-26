package com.hcproj.healthcareprojectbackend.auth.entity;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(
        name = "social_accounts",
        uniqueConstraints = {
                // 같은 소셜 계정(provider_user_id)은 한 user에만 귀속
                @UniqueConstraint(
                        name = "uk_social_provider_user",
                        columnNames = {"provider", "provider_user_id"}
                ),
                // 같은 유저는 같은 provider를 2개 연결 불가 (구글 2개 비허용)
                @UniqueConstraint(
                        name = "uk_social_user_provider",
                        columnNames = {"user_id", "provider"}
                )
        },
        indexes = {
                @Index(name = "idx_social_user", columnList = "user_id")
        }
)
public class SocialAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private SocialProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;

    public static SocialAccountEntity connect(Long userId, SocialProvider provider, String providerUserId) {
        if (userId == null) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        if (provider == null) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        if (providerUserId == null || providerUserId.isBlank())
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        return SocialAccountEntity.builder()
                .userId(userId)
                .provider(provider)
                .providerUserId(providerUserId)
                .connectedAt(Instant.now())
                .build();
    }
}
