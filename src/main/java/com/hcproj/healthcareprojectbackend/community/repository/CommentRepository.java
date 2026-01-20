package com.hcproj.healthcareprojectbackend.community.repository;

import com.hcproj.healthcareprojectbackend.community.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    // ID 기반 조회 (순서는 댓글 ID순 = 작성순)
    List<CommentEntity> findAllByPostId(Long postId);
}