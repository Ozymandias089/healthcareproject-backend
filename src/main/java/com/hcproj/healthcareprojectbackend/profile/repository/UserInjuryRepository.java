package com.hcproj.healthcareprojectbackend.profile.repository;

import com.hcproj.healthcareprojectbackend.profile.entity.UserInjuryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInjuryRepository extends JpaRepository<UserInjuryEntity, Long> {
    List<UserInjuryEntity> findAllByUserId(Long userId);
}
