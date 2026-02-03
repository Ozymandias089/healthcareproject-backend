package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtParticipantStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * PT 방 참가자({@link com.hcproj.healthcareprojectbackend.pt.entity.PtRoomParticipantEntity})
 * 에 대한 영속성 접근 인터페이스.
 *
 * <p><b>주요 사용 시나리오</b></p>
 * <ul>
 *   <li>방/유저 단위 참가 상태 조회(입장 여부, 강퇴 여부 등)</li>
 *   <li>방 참가자 목록 조회(상태 및 입장 시간 기준 정렬 포함)</li>
 *   <li>정원/활성 참가자 수 계산</li>
 * </ul>
 */
public interface PtRoomParticipantRepository extends JpaRepository<PtRoomParticipantEntity, Long> {
    interface RoomCount {
        Long getPtRoomId();
        long getCount();
    }

    /** 방/유저로 참가 레코드를 조회한다. */
    Optional<PtRoomParticipantEntity> findByPtRoomIdAndUserId(Long ptRoomId, Long userId);

    /** 특정 방의 전체 참가자 목록을 조회한다. */
    List<PtRoomParticipantEntity> findAllByPtRoomId(Long ptRoomId);

    /**
     * 특정 방의 특정 상태 참가자 목록을 입장 시간 오름차순으로 조회한다.
     *
     * @param ptRoomId 방 ID
     * @param status   참가 상태(JOINED 등)
     */
    List<PtRoomParticipantEntity> findAllByPtRoomIdAndStatusOrderByJoinedAtAsc(Long ptRoomId, PtParticipantStatus status);

    /** 특정 방에서 특정 상태의 참가자 수를 반환한다(정원 체크 등). */
    long countByPtRoomIdAndStatus(Long ptRoomId, PtParticipantStatus status);

    /**
     * 여러 방의 특정 상태 참가자 수를 한번에 조회한다.
     */
    @Query("""
        select p.ptRoomId as ptRoomId, count(p) as count
        from PtRoomParticipantEntity p
        where p.ptRoomId in :ptRoomIds and p.status = :status
        group by p.ptRoomId
    """)
    List<RoomCount> countByPtRoomIdsAndStatus(
            @Param("ptRoomIds") List<Long> ptRoomIds,
            @Param("status") PtParticipantStatus status
    );

    /** 특정 방에서 사용자가 특정 상태로 존재하는지 확인한다(중복 입장 방지 등). */
    boolean existsByPtRoomIdAndUserIdAndStatus(Long ptRoomId, Long userId, PtParticipantStatus status);

    /** 사용자가 속한(참여 이력 포함) 방 참가 레코드 목록을 조회한다. */
    List<PtRoomParticipantEntity> findAllByUserId(Long userId);
}
