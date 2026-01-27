package com.hcproj.healthcareprojectbackend.auth.entity;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 사용자 계정과 소셜 OAuth 계정 간의 연결(연동) 정보를 나타내는 엔티티.
 *
 * <p><b>모델링 의도</b></p>
 * <ul>
 *   <li>하나의 사용자({@code userId})는 각 소셜 제공자(provider)를 최대 1개만 연결한다.</li>
 *   <li>하나의 소셜 계정({@code provider + providerUserId})은 오직 한 사용자에게만 귀속된다.</li>
 * </ul>
 *
 * <p><b>DB 제약</b></p>
 * <ul>
 *   <li>{@code uk_social_provider_user}: (provider, provider_user_id) 유니크</li>
 *   <li>{@code uk_social_user_provider}: (user_id, provider) 유니크</li>
 *   <li>{@code idx_social_user}: user_id 인덱스</li>
 * </ul>
 *
 * <p><b>생성 정책</b></p>
 * <ul>
 *   <li>생성은 {@link #connect(Long, SocialProvider, String)} 정적 팩토리를 통해서만 수행한다.</li>
 *   <li>connectedAt은 생성 시점({@link Instant#now()})으로 설정한다.</li>
 * </ul>
 */
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

    /**
     * 소셜 계정을 사용자에 연결하는 엔티티를 생성한다.
     *
     * <p>
     * 입력값이 유효하지 않으면 {@link BusinessException}({@link ErrorCode#INVALID_INPUT_VALUE})을 발생시킨다.
     * 실제 중복/충돌은 DB 유니크 제약 또는 상위 서비스의 사전 검증으로 방지한다.
     * </p>
     *
     * @param userId         내부 사용자 ID
     * @param provider       소셜 제공자
     * @param providerUserId 제공자 측 사용자 식별자(고유)
     * @return 새로 생성된 SocialAccountEntity
     * @throws BusinessException 입력값이 유효하지 않은 경우
     */
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
