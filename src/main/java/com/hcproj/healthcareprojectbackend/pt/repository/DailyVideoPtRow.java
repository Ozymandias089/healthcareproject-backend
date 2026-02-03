package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;

import java.time.Instant;

/**
 * 일간(하루) 화상 PT 목록 조회를 위한 프로젝션 인터페이스.
 *
 * <p>
 * {@link com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity}의 일부 컬럼만 조회하여
 * 캘린더/리스트 화면 렌더링에 필요한 최소 정보만 전달한다.
 * </p>
 *
 * <p><b>주의</b></p>
 * <ul>
 *   <li>JPQL에서 {@code select ... as <alias>}로 매핑되는 별칭은 getter 이름과 일치해야 한다.</li>
 *   <li>필드는 엔티티 전체가 아닌 필요한 값만 포함한다.</li>
 * </ul>
 */
public interface DailyVideoPtRow {
    /** PT 방 ID */
    Long getPtRoomId();
    /** PT 방 ID */
    Instant getScheduledStartAt();
    /** 방 상태 */
    PtRoomStatus getStatus();
    /** 방 제목 */
    String getTitle();
}