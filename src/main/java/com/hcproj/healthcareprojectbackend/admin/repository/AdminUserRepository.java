package com.hcproj.healthcareprojectbackend.admin.repository;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AdminUserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByHandle(String handle);

    // 권한 강제 변경 (Entity 수정 없이 DB 직접 업데이트)
    @Modifying
    @Query("UPDATE UserEntity u SET u.role = :role WHERE u.handle = :handle")
    void updateUserRole(@Param("handle") String handle, @Param("role") UserRole role);

    // 대시보드용 가입자 수 집계
    long countByCreatedAtAfter(Instant startOfDay);
}