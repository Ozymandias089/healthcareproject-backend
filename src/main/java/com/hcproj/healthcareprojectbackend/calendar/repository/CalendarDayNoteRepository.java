package com.hcproj.healthcareprojectbackend.calendar.repository;

import com.hcproj.healthcareprojectbackend.calendar.entity.CalendarDayNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CalendarDayNoteRepository extends JpaRepository<CalendarDayNoteEntity, Long> {
    Optional<CalendarDayNoteEntity> findByUserIdAndNoteDate(Long userId, LocalDate noteDate);
    void deleteByUserIdAndNoteDate(Long userId, LocalDate noteDate);
    @Query("""
    select n.noteDate
    from CalendarDayNoteEntity n
    where n.userId = :userId
      and n.noteDate between :startDate and :endDate
""")
    List<LocalDate> findNoteDatesInRange(Long userId, LocalDate startDate, LocalDate endDate);

}
