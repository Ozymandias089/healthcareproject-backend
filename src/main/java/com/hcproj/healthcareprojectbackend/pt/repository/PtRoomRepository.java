package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PtRoomRepository extends JpaRepository<PtRoomEntity, Long> {
    List<PtRoomEntity> findAllByTrainerId(Long trainerId);
}
