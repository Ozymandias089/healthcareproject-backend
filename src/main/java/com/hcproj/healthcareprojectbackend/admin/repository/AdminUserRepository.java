package com.hcproj.healthcareprojectbackend.admin.repository;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AdminUserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByHandle(String handle);

    // 권한 강제 변경 <----- 사용하면 안됩니다. 도메인 액션으로 직접 변경해서 더티체킹으로 변경해야 함.
    //@Modifying
    //@Query("UPDATE UserEntity u SET u.role = :role WHERE u.handle = :handle")
    //void updateUserRole(@Param("handle") String handle, @Param("role") UserRole role);

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
