package com.hcproj.healthcareprojectbackend.trainer.repository;

import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerApplicationStatus;
import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerInfoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

import java.util.Optional;

/**
 * 트레이너 신청 및 승인 정보에 대한 Repository.
 *
 * <p>
 * 트레이너 신청 상태(PENDING / APPROVED / REJECTED) 기반 조회,
 * 관리자 대시보드 통계 및 트레이너 프로필 조회에 사용된다.
 * </p>
 */
public interface TrainerInfoRepository extends JpaRepository<TrainerInfoEntity, Long> {

    /**
     * 특정 신청 상태의 트레이너 신청 목록 조회 (관리자용).
     *
     * @param status 신청 상태
     * @param pageable 페이지 정보
     * @return 트레이너 신청 목록
     */
    Page<TrainerInfoEntity> findAllByApplicationStatus(TrainerApplicationStatus status, Pageable pageable);

    /**
     * 특정 상태의 트레이너 신청 수 조회 (대시보드용).
     *
     * @param status 신청 상태
     * @return 신청 수
     */
    long countByApplicationStatus(TrainerApplicationStatus status);


    /**
     * 오늘 접수된 트레이너 신청 수 조회.
     *
     * <p>
     * {@link com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity}
     * 의 createdAt 필드를 기준으로 한다.
     * </p>
     *
     * @param startOfDay 오늘 00:00 기준 시각
     * @return 오늘 접수된 신청 수
     */
    long countByCreatedAtAfter(Instant startOfDay);

    /**
     * 특정 트레이너의 소개글(bio)만 단건 조회.
     *
     * <p>
     * 전체 엔티티 로딩 없이 텍스트만 필요할 때 사용한다.
     * </p>
     *
     * @param trainerId 트레이너 사용자 ID
     * @return 소개글 (Optional)
     */
    @Query("""
        select t.bio
        from TrainerInfoEntity t
        where t.userId = :trainerId
    """)
    Optional<String> findBioByTrainerId(@Param("trainerId") Long trainerId);

}

