package com.hcproj.healthcareprojectbackend.profile.repository;

import com.hcproj.healthcareprojectbackend.profile.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 프로필({@link com.hcproj.healthcareprojectbackend.profile.entity.UserProfileEntity})
 * 에 대한 영속성 접근 인터페이스.
 *
 * <p>
 * userId(PK=FK) 기반의 단건 조회/저장이 주 사용 시나리오다.
 * </p>
 */
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
}
