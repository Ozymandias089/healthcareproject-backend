package com.hcproj.healthcareprojectbackend.workout.repository;

import com.hcproj.healthcareprojectbackend.workout.entity.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long> {
    List<ExerciseEntity> findAllByIsActiveTrue();

    /**
     * 활성화된 운동 단건 조회 (상세 조회용)
     *
     * @param exerciseId 운동 ID
     * @return 활성화된 운동 (Optional)
     */
    Optional<ExerciseEntity> findByExerciseIdAndIsActiveTrue(Long exerciseId);

    /**
     * 대체 운동 조회
     * - 동일 bodyPart
     * - 자기 자신 제외
     * - 활성화된 운동만
     * - 최대 3개
     *
     * @param bodyPart 운동 부위
     * @param excludeId 제외할 운동 ID (자기 자신)
     * @return 대체 운동 목록 (최대 3개)
     */
    @Query("""
            SELECT e FROM ExerciseEntity e
            WHERE e.bodyPart = :bodyPart
              AND e.exerciseId <> :excludeId
              AND e.isActive = true
            ORDER BY e.exerciseId ASC
            LIMIT 3
            """)
    List<ExerciseEntity> findAlternatives(
            @Param("bodyPart") String bodyPart,
            @Param("excludeId") Long excludeId
    );
    /**
     * 운동 리스트 조회 (무한 스크롤, 검색, 필터)
     */
    @Query("""
            SELECT e FROM ExerciseEntity e
            WHERE e.isActive = true
              AND (:cursor IS NULL OR e.exerciseId > :cursor)
              AND (:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:bodyPart IS NULL OR e.bodyPart = :bodyPart)
            ORDER BY e.exerciseId ASC
            LIMIT :limit
            """)
    List<ExerciseEntity> findExercisesWithCursor(
            @Param("cursor") Long cursor,
            @Param("keyword") String keyword,
            @Param("bodyPart") String bodyPart,
            @Param("limit") int limit
    );
}
