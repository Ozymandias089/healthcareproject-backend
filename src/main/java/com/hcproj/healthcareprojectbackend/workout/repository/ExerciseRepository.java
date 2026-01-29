package com.hcproj.healthcareprojectbackend.workout.repository;

import com.hcproj.healthcareprojectbackend.workout.entity.ExerciseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 운동(Exercise) 정보에 대한 Repository.
 */
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long> {

    /** 활성화된 모든 운동 조회. */
    List<ExerciseEntity> findAllByIsActiveTrue();

    /** 활성화된 운동 단건 조회 (상세 조회용) */
    Optional<ExerciseEntity> findByExerciseIdAndIsActiveTrue(Long exerciseId);

    /** 대체 운동 조회 (동일 bodyPart, 자기 자신 제외, 최대 3개) */
    @Query(value = "SELECT * FROM exercises e " +
            "WHERE e.body_part = :bodyPart " +
            "AND e.exercise_id <> :excludeId " +
            "AND e.is_active = true " +
            "ORDER BY e.exercise_id ASC " +
            "FETCH FIRST 3 ROWS ONLY",
            nativeQuery = true)
    List<ExerciseEntity> findAlternatives(
            @Param("bodyPart") String bodyPart,
            @Param("excludeId") Long excludeId
    );

    // ============================================================
    // 커서 기반 운동 목록 조회 - 검색어 없음
    // ============================================================
    @Query(value = "SELECT * FROM exercises e " +
            "WHERE e.is_active = true " +
            "AND (:cursor IS NULL OR e.exercise_id > :cursor) " +
            "AND (:bodyPart IS NULL OR e.body_part = :bodyPart) " +
            "ORDER BY e.exercise_id ASC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<ExerciseEntity> findExercisesWithCursorNoKeyword(
            @Param("cursor") Long cursor,
            @Param("bodyPart") String bodyPart,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 커서 기반 운동 목록 조회 - 검색어 있음 (띄어쓰기 무시)
    // ============================================================
    @Query(value = "SELECT * FROM exercises e " +
            "WHERE e.is_active = true " +
            "AND (:cursor IS NULL OR e.exercise_id > :cursor) " +
            "AND (:bodyPart IS NULL OR e.body_part = :bodyPart) " +
            "AND REPLACE(LOWER(e.name), ' ', '') LIKE :keyword " +
            "ORDER BY e.exercise_id ASC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<ExerciseEntity> findExercisesWithCursorAndKeyword(
            @Param("cursor") Long cursor,
            @Param("keyword") String keyword,
            @Param("bodyPart") String bodyPart,
            @Param("limitSize") int limitSize
    );

    /** 활성화된 운동 페이지 단위 조회. */
    List<ExerciseEntity> findByIsActiveTrue(Pageable pageable);

    /** 여러 운동 ID로 일괄 조회. */
    List<ExerciseEntity> findByExerciseIdIn(Collection<Long> ids);
}