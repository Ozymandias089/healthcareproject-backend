package com.hcproj.healthcareprojectbackend.profile.repository;

import com.hcproj.healthcareprojectbackend.profile.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
}
