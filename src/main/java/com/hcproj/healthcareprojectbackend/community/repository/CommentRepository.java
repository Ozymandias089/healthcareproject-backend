package com.hcproj.healthcareprojectbackend.community.repository;

import com.hcproj.healthcareprojectbackend.community.entity.CommentEntity;
import com.hcproj.healthcareprojectbackend.community.entity.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findAllByPostIdAndStatusOrderByCommentIdAsc(Long postId, CommentStatus status);
}
