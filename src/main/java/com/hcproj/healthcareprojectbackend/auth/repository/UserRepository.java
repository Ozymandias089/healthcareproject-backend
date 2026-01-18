package com.hcproj.healthcareprojectbackend.auth.repository;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);
    boolean existsByHandle(String handle);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByHandle(String handle);
}
