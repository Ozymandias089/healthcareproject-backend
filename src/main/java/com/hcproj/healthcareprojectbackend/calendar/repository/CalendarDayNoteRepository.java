package com.hcproj.healthcareprojectbackend.calendar.repository;

import com.hcproj.healthcareprojectbackend.calendar.entity.CalendarDayNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CalendarDayNoteRepository extends JpaRepository<CalendarDayNoteEntity, Long> {
    List<CalendarDayNoteEntity> findAllByUserIdAndNoteDateOrderBySortOrderAsc(Long userId, LocalDate noteDate);
}
