package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtJanusKeyStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtJanusRoomKeyEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Janus roomKey 풀({@link com.hcproj.healthcareprojectbackend.pt.entity.PtJanusRoomKeyEntity})
 * 에 대한 영속성 접근 인터페이스.
 *
 * <p><b>핵심 관심사: 동시성</b></p>
 * <ul>
 *   <li>roomKey 할당은 경쟁이 발생할 수 있으므로 비관적 락을 사용한다.</li>
 *   <li>{@link #findAvailableForUpdate(PtJanusKeyStatus, org.springframework.data.domain.Pageable)}는
 *       SELECT ... FOR UPDATE 성격으로 "사용 가능한 키"를 안전하게 선점하기 위한 조회다.</li>
 * </ul>
 */
public interface PtJanusRoomKeyRepository extends JpaRepository<PtJanusRoomKeyEntity, Integer> {

    /**
     * 특정 상태의 roomKey를 비관적 락(PESSIMISTIC_WRITE)으로 조회한다.
     *
     * <p><b>사용 의도</b></p>
     * <ul>
     *   <li>AVAILABLE 키를 1개(또는 N개) 선점하여 할당(ALLOCATED) 처리하기 위한 용도</li>
     *   <li>Pageable로 조회 개수 제한을 걸어 "가장 작은 roomKey부터" 가져온다.</li>
     * </ul>
     *
     * @param status   조회할 키 상태(보통 AVAILABLE)
     * @param pageable 조회 개수 제한용(정렬은 쿼리에서 roomKey ASC 고정)
     * @return 락이 걸린 roomKey 엔티티 목록
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT k FROM PtJanusRoomKeyEntity k
    WHERE k.status = :status
    ORDER BY k.roomKey ASC
""")
    java.util.List<PtJanusRoomKeyEntity> findAvailableForUpdate(
            @Param("status") PtJanusKeyStatus status,
            org.springframework.data.domain.Pageable pageable
    );

    /**
     * 특정 PT 방에 할당된 roomKey를 조회한다.
     *
     * @param ptRoomId PT 방 ID
     * @return 할당된 키가 있으면 Optional로 반환
     */
    Optional<PtJanusRoomKeyEntity> findByPtRoomId(Long ptRoomId);
}
