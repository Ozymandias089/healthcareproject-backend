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


public interface TrainerInfoRepository extends JpaRepository<TrainerInfoEntity, Long> {

    Page<TrainerInfoEntity> findAllByApplicationStatus(TrainerApplicationStatus status, Pageable pageable);

    // 대기중인 신청 수
    long countByApplicationStatus(TrainerApplicationStatus status);


    // [BaseTimeEntity 활용] 오늘 들어온 신청 수
    long countByCreatedAtAfter(Instant startOfDay);
    @Query("""
        select t.bio
        from TrainerInfoEntity t
        where t.userId = :trainerId
    """)
    Optional<String> findBioByTrainerId(@Param("trainerId") Long trainerId);

}

