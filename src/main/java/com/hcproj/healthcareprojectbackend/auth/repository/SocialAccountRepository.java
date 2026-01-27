package com.hcproj.healthcareprojectbackend.auth.repository;

import com.hcproj.healthcareprojectbackend.auth.entity.SocialAccountEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 소셜 계정 연동({@link SocialAccountEntity})에 대한 영속성 접근 인터페이스.
 *
 * <p><b>주요 사용 시나리오</b></p>
 * <ul>
 *   <li>소셜 로그인 시 provider/providerUserId로 연동 정보 조회</li>
 *   <li>특정 사용자의 특정 provider 연동 여부 확인</li>
 *   <li>사용자 기준 연동 목록 조회 및 개수 집계</li>
 * </ul>
 *
 * <p>
 * 중복 연동 방지는 엔티티의 유니크 제약과 함께,
 * 서비스 레이어에서 {@code existsBy...} 기반 사전 검증으로 보완한다.
 * </p>
 */
public interface SocialAccountRepository extends JpaRepository<SocialAccountEntity, Long> {

    /** provider + providerUserId로 소셜 연동 정보를 조회한다. */
    Optional<SocialAccountEntity> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    /** userId + provider로 해당 사용자의 특정 소셜 연동 정보를 조회한다. */
    Optional<SocialAccountEntity> findByUserIdAndProvider(Long userId, SocialProvider provider);

    /** 특정 사용자가 연결한 소셜 계정 개수를 반환한다. */
    long countByUserId(Long userId);

    /** 특정 사용자가 해당 provider를 이미 연결했는지 여부를 확인한다. */
    boolean existsByUserIdAndProvider(Long userId, SocialProvider provider);

    /** 특정 사용자의 모든 소셜 연동 정보를 조회한다. */
    List<SocialAccountEntity> findAllByUserId(Long userId);

    /** provider + providerUserId 조합의 연동 정보 존재 여부를 확인한다. */
    boolean existsByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
}