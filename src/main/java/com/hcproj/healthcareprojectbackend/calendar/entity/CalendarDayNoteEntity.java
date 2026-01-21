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
@Table(
        name = "calendar_day_notes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_calendar_note_user_date", columnNames = {"user_id", "note_date"})
        },
        indexes = {
                @Index(name = "idx_calendar_note_user_date", columnList = "user_id,note_date")
        }
)
public class CalendarDayNoteEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_day_note_id")
    private Long calendarDayNoteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "note_date", nullable = false)
    private LocalDate noteDate;

    @Lob
    @Column(name = "note", nullable = false)
    private String note;

    public void updateNote(String note) {
        this.note = note;
    }
}
