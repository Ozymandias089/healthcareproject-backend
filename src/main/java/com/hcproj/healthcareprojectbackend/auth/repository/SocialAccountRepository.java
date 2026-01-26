package com.hcproj.healthcareprojectbackend.auth.repository;

import com.hcproj.healthcareprojectbackend.auth.entity.SocialAccountEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccountEntity, Long> {
    Optional<SocialAccountEntity> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
    Optional<SocialAccountEntity> findByUserIdAndProvider(Long userId, SocialProvider provider);
    long countByUserId(Long userId);
    boolean existsByUserIdAndProvider(Long userId, SocialProvider provider);
    List<SocialAccountEntity> findAllByUserId(Long userId);
    boolean existsByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
}