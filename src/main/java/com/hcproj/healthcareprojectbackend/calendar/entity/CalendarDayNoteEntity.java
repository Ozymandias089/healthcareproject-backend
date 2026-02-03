package com.hcproj.healthcareprojectbackend.calendar.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 특정 사용자의 특정 날짜에 대한 메모(일일 노트)를 나타내는 엔티티.
 *
 * <p><b>모델링 규칙</b></p>
 * <ul>
 *   <li>한 사용자(userId)는 하루(noteDate)에 하나의 노트만 가진다.</li>
 * </ul>
 *
 * <p><b>DB 제약</b></p>
 * <ul>
 *   <li>{@code uk_calendar_note_user_date}: (user_id, note_date) 유니크</li>
 *   <li>{@code idx_calendar_note_user_date}: (user_id, note_date) 인덱스</li>
 * </ul>
 *
 * <p><b>내용 저장</b></p>
 * <ul>
 *   <li>{@link Lob}으로 저장되어 길이 제한을 완화한다.</li>
 *   <li>note는 nullable=false 이므로 "빈 메모" 정책은 상위 서비스에서 결정한다.</li>
 * </ul>
 */
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

    @Column(name = "note",columnDefinition = "text", nullable = false)
    private String note;

    /**
     * 메모 내용을 갱신한다.
     *
     * <p>
     * note가 비어있어도 허용할지/삭제로 볼지는 상위 레이어 정책에 따른다.
     * </p>
     *
     * @param note 변경할 메모 내용
     */
    public void updateNote(String note) {
        this.note = note;
    }
}
