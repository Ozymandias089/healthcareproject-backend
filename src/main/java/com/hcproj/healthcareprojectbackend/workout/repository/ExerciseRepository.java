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

    /** 활성화된 운동 페이지 단위 조회. */
    List<ExerciseEntity> findByIsActiveTrue(Pageable pageable);

    /** 여러 운동 ID로 일괄 조회. */
    List<ExerciseEntity> findByExerciseIdIn(Collection<Long> ids);

    @Query("""
    select e
    from ExerciseEntity e
    where e.isActive = true
      and (:cursor is null or e.exerciseId < :cursor)
      and (:bodyParts is null or e.bodyPart in :bodyParts)
      and (:difficulties is null or e.difficulty in :difficulties)
    order by e.exerciseId desc
""")
    List<ExerciseEntity> findPageNoKeyword(
            @Param("cursor") Long cursor,
            @Param("bodyParts") List<String> bodyParts,
            @Param("difficulties") List<String> difficulties,
            Pageable pageable
    );

    @Query("""
    select e
    from ExerciseEntity e
    where e.isActive = true
      and (:cursor is null or e.exerciseId < :cursor)
      and (:bodyParts is null or e.bodyPart in :bodyParts)
      and (:difficulties is null or e.difficulty in :difficulties)
      and lower(function('replace', e.name, ' ', '')) like :likePattern
    order by e.exerciseId desc
""")
    List<ExerciseEntity> findPageWithKeyword(
            @Param("cursor") Long cursor,
            @Param("likePattern") String likePattern,
            @Param("bodyParts") List<String> bodyParts,
            @Param("difficulties") List<String> difficulties,
            Pageable pageable
    );
}