package com.hcproj.healthcareprojectbackend.auth.repository;

import com.hcproj.healthcareprojectbackend.auth.entity.SocialAccountEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccountEntity, Long> {
    Optional<SocialAccountEntity> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
}