package com.hcproj.healthcareprojectbackend.calendar.service;

import com.hcproj.healthcareprojectbackend.calendar.dto.internal.MemoDTO;
import com.hcproj.healthcareprojectbackend.calendar.dto.response.MemoResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.entity.CalendarDayNoteEntity;
import com.hcproj.healthcareprojectbackend.calendar.repository.CalendarDayNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarDayNoteRepository calendarDayNoteRepository;

    @Transactional
    public MemoResponseDTO putMemo(Long userId, LocalDate date, String content) {

        // 1) 삭제 정책
        if (content == null || content.isBlank()) {
            calendarDayNoteRepository.deleteByUserIdAndNoteDate(userId, date);

            return MemoResponseDTO.builder()
                    .message("메모가 삭제되었습니다.")
                    .memo(MemoDTO.builder()
                            .memoId(null)
                            .date(date.toString())
                            .content("") // 프론트가 삭제 상태로 처리하기 쉽게 빈값 반환
                            .build())
                    .build();
        }

        // 2) 업서트
        CalendarDayNoteEntity entity = calendarDayNoteRepository
                .findByUserIdAndNoteDate(userId, date)
                .map(existing -> {
                    existing.updateNote(content);
                    return existing;
                })
                .orElseGet(() -> CalendarDayNoteEntity.builder()
                        .userId(userId)
                        .noteDate(date)
                        .note(content)
                        .build()
                );

        CalendarDayNoteEntity saved = calendarDayNoteRepository.save(entity);

        return MemoResponseDTO.builder()
                .message("메모가 저장되었습니다.")
                .memo(MemoDTO.builder()
                        .memoId(String.valueOf(saved.getCalendarDayNoteId()))
                        .date(saved.getNoteDate().toString())
                        .content(saved.getNote())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public MemoResponseDTO getMemo(Long userId, LocalDate date) {

        return calendarDayNoteRepository
                .findByUserIdAndNoteDate(userId, date)
                .map(entity -> MemoResponseDTO.builder()
                        .message("메모 조회 성공")
                        .memo(MemoDTO.builder()
                                .memoId(entity.getCalendarDayNoteId().toString())
                                .date(entity.getNoteDate().toString())
                                .content(entity.getNote())
                                .build())
                        .build()
                )
                .orElseGet(() -> MemoResponseDTO.builder()
                        .message("해당 날짜에 메모가 없습니다.")
                        .memo(null)
                        .build()
                );
    }

}
