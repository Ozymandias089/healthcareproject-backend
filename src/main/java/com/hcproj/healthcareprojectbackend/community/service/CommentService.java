package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.CommentCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.CommentUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentCreateResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentDeleteResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentUpdateResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO.AuthorDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO.CommentDTO;
import com.hcproj.healthcareprojectbackend.community.entity.CommentEntity;
import com.hcproj.healthcareprojectbackend.community.entity.CommentStatus;
import com.hcproj.healthcareprojectbackend.community.repository.CommentRepository;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public CommentCreateResponseDTO createComment(Long userId, Long postId, CommentCreateRequestDTO request) {
        if (!userRepository.existsById(userId)) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (!postRepository.existsById(postId)) throw new BusinessException(ErrorCode.POST_NOT_FOUND);

        if (request.parentId() != null) {
            CommentEntity parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
            if (!parent.getPostId().equals(postId)) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        CommentEntity comment = CommentEntity.builder()
                .userId(userId).postId(postId).parentCommentId(request.parentId())
                .content(request.content()).status(CommentStatus.POSTED).build();

        CommentEntity saved = commentRepository.save(comment);
        return CommentCreateResponseDTO.of(saved.getCommentId(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsForPost(Long postId) {
        List<CommentEntity> entities = commentRepository.findAllByPostId(postId);
        if (entities.isEmpty()) return new ArrayList<>();

        Set<Long> userIds = entities.stream().map(CommentEntity::getUserId).collect(Collectors.toSet());
        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        return convertToCommentTree(entities, userMap);
    }

    @Transactional
    public CommentUpdateResponseDTO updateComment(Long userId, Long commentId, CommentUpdateRequestDTO request) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) throw new BusinessException(ErrorCode.NOT_COMMENT_AUTHOR);

        comment.update(request.content());
        return CommentUpdateResponseDTO.of(comment.getCommentId(), comment.getUpdatedAt());
    }

    @Transactional
    public CommentDeleteResponseDTO deleteComment(Long userId, Long postId, Long commentId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getPostId().equals(postId)) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        if (!comment.getUserId().equals(userId) && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.NOT_COMMENT_AUTHOR);
        }

        if (comment.isDeleted()) {
            return CommentDeleteResponseDTO.of(comment.getDeletedAt());
        }

        comment.delete();
        return CommentDeleteResponseDTO.of(comment.getDeletedAt() != null ? comment.getDeletedAt() : Instant.now());
    }

    private List<CommentDTO> convertToCommentTree(List<CommentEntity> entities, Map<Long, UserEntity> userMap) {
        Map<Long, CommentDTO> dtoMap = new HashMap<>();
        List<CommentDTO> roots = new ArrayList<>();

        for (CommentEntity entity : entities) {
            UserEntity author = userMap.get(entity.getUserId());
            AuthorDTO authorDTO = (author != null) ? new AuthorDTO(author.getNickname(), author.getHandle()) : new AuthorDTO("알수없음", "unknown");

            String content = (entity.getStatus() == CommentStatus.DELETED || entity.isDeleted())
                    ? "삭제된 댓글입니다." : entity.getContent();

            CommentDTO dto = CommentDTO.builder()
                    .commentId(entity.getCommentId()).content(content).author(authorDTO)
                    .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).deletedAt(entity.getDeletedAt())
                    .children(new ArrayList<>()).build();
            dtoMap.put(dto.commentId(), dto);
        }

        for (CommentEntity entity : entities) {
            CommentDTO currentDto = dtoMap.get(entity.getCommentId());
            if (entity.getParentCommentId() == null) roots.add(currentDto);
            else {
                CommentDTO parentDto = dtoMap.get(entity.getParentCommentId());
                if (parentDto != null) parentDto.children().add(currentDto);
            }
        }
        return roots;
    }
}