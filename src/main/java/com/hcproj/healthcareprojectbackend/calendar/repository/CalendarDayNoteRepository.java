package com.hcproj.healthcareprojectbackend.calendar.repository;

import com.hcproj.healthcareprojectbackend.calendar.entity.CalendarDayNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 일일 메모({@link CalendarDayNoteEntity})에 대한 영속성 접근 인터페이스.
 *
 * <p><b>주요 사용 시나리오</b></p>
 * <ul>
 *   <li>특정 날짜 메모 조회/삭제</li>
 *   <li>달력 UI를 위한 "메모가 존재하는 날짜 목록" 조회</li>
 * </ul>
 */
public interface CalendarDayNoteRepository extends JpaRepository<CalendarDayNoteEntity, Long> {
    /** 사용자/날짜로 메모를 조회한다. */
    Optional<CalendarDayNoteEntity> findByUserIdAndNoteDate(Long userId, LocalDate noteDate);
    /** 사용자/날짜로 메모를 삭제한다. */
    void deleteByUserIdAndNoteDate(Long userId, LocalDate noteDate);

    /**
     * 특정 기간 내 메모가 존재하는 날짜 목록을 조회한다.
     *
     * <p>
     * 달력 월/주 단위 뷰에서 "메모 표시(dot)" 등에 사용한다.
     * </p>
     *
     * @param userId    사용자 ID
     * @param startDate 시작 날짜(포함)
     * @param endDate   종료 날짜(포함)
     * @return 메모가 존재하는 날짜 리스트
     */
    @Query("""
    select n.noteDate
    from CalendarDayNoteEntity n
    where n.userId = :userId
      and n.noteDate between :startDate and :endDate
""")
    List<LocalDate> findNoteDatesInRange(Long userId, LocalDate startDate, LocalDate endDate);

}
