package com.hcproj.healthcareprojectbackend.admin.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminPtRoomRepository extends JpaRepository<PtRoomEntity, Long> {

    // PtRoomRepository를 수정하지 않고 여기서 조회
    long countByStatus(PtRoomStatus status);
}