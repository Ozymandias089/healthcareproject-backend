package com.hcproj.healthcareprojectbackend.profile.repository;

import com.hcproj.healthcareprojectbackend.profile.entity.UserInjuryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 사용자 부상 정보({@link com.hcproj.healthcareprojectbackend.profile.entity.UserInjuryEntity})
 * 에 대한 영속성 접근 인터페이스.
 *
 * <p><b>주요 사용 시나리오</b></p>
 * <ul>
 *   <li>사용자 기준 부상 목록 조회</li>
 *   <li>프로필 초기화/재설정 시 사용자 부상 일괄 삭제</li>
 * </ul>
 */
public interface UserInjuryRepository extends JpaRepository<UserInjuryEntity, Long> {
    /** 특정 사용자의 부상 목록을 조회한다. */
    List<UserInjuryEntity> findAllByUserId(Long userId);
    /** 특정 사용자의 부상 목록을 조회한다. */
    void deleteByUserId(Long userId);
}
