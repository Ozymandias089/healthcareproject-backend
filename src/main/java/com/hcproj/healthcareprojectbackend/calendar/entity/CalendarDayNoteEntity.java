package com.hcproj.healthcareprojectbackend.calendar.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "calendar_day_notes", indexes = {
        @Index(name = "idx_calendar_note_user_date", columnList = "user_id,note_date")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_calendar_note_user_date_sort", columnNames = {"user_id", "note_date", "sort_order"})
})
public class CalendarDayNoteEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_day_note_id")
    private Long calendarDayNoteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "note_date", nullable = false)
    private LocalDate noteDate;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Lob
    @Column(name = "note", nullable = false)
    private String note;
}
