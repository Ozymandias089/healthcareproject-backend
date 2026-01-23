package com.hcproj.healthcareprojectbackend.auth.repository;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);
    boolean existsByHandle(String handle);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByHandle(String handle);

    // 오늘 가입한 회원 수 (BaseTimeEntity 활용)
    long countByCreatedAtAfter(Instant startOfDay);

    // 상태별 회원 수 (ACTIVE, INACTIVE 등)
    long countByStatus(UserStatus status);

    @Query("SELECT u FROM UserEntity u " +
            "WHERE (:role IS NULL OR u.role = :role) " +
            "AND (:keyword IS NULL OR u.nickname LIKE %:keyword% OR u.email LIKE %:keyword%)")
    Page<UserEntity> findAllWithFilters(
            @Param("role") UserRole role,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
