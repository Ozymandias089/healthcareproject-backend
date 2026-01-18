package com.hcproj.healthcareprojectbackend.trainer.repository;

import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerApplicationStatus;
import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerInfoRepository extends JpaRepository<TrainerInfoEntity, Long> {
    List<TrainerInfoEntity> findAllByApplicationStatus(TrainerApplicationStatus applicationStatus);
}
