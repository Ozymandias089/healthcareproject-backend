package com.hcproj.healthcareprojectbackend.trainer.repository;

import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerApplicationStatus;
import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerInfoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerInfoRepository extends JpaRepository<TrainerInfoEntity, Long> {

    Page<TrainerInfoEntity> findAllByApplicationStatus(TrainerApplicationStatus status, Pageable pageable);

    long countByApplicationStatus(TrainerApplicationStatus status);
}

