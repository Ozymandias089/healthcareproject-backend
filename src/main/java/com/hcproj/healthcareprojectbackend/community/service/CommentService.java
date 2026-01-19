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
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.repository.CommentRepository;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // [추가] 시간 생성을 위해 필요
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 1. 댓글 작성
    @Transactional
    public CommentCreateResponseDTO createComment(Long userId, Long postId, CommentCreateRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        CommentEntity parentComment = null;
        if (request.parentId() != null) {
            parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
            if (!parentComment.getPost().getPostId().equals(postId)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        CommentEntity comment = CommentEntity.builder()
                .user(user)
                .post(post)
                .parent(parentComment)
                .content(request.content())
                .status(CommentStatus.POSTED)
                .build();

        return CommentCreateResponseDTO.of(
                commentRepository.save(comment).getCommentId(),
                comment.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
    }

    // 2. 댓글 목록 조회
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsForPost(Long postId) {
        List<CommentEntity> entities = commentRepository.findAllByPostIdForDetail(postId);
        return convertToCommentTree(entities);
    }

    // 3. 댓글 수정
    @Transactional
    public CommentUpdateResponseDTO updateComment(Long userId, Long commentId, CommentUpdateRequestDTO request) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_COMMENT_AUTHOR);
        }

        comment.update(request.content());

        return CommentUpdateResponseDTO.of(
                comment.getCommentId(),
                comment.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
    }

    // 4. 댓글 삭제 (수정된 부분)
    @Transactional
    public CommentDeleteResponseDTO deleteComment(Long userId, Long postId, Long commentId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getPost().getPostId().equals(postId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        boolean isWriter = comment.getUser().getId().equals(userId);
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isWriter && !isAdmin) {
            throw new BusinessException(ErrorCode.NOT_COMMENT_AUTHOR);
        }

        // ▼ [수정됨] Entity 메서드가 아니라 Repository 쿼리를 호출합니다.
        LocalDateTime now = LocalDateTime.now();
        commentRepository.softDelete(commentId, now);

        return CommentDeleteResponseDTO.of(now);
    }

    // [내부 메서드]
    private List<CommentDTO> convertToCommentTree(List<CommentEntity> entities) {
        Map<Long, CommentDTO> dtoMap = new HashMap<>();
        List<CommentDTO> roots = new ArrayList<>();

        for (CommentEntity entity : entities) {
            String content = (entity.getStatus() == CommentStatus.DELETED || entity.getDeletedAt() != null)
                    ? "삭제된 댓글입니다."
                    : entity.getContent();

            CommentDTO dto = CommentDTO.builder()
                    .commentId(entity.getCommentId())
                    .content(content)
                    .author(AuthorDTO.from(entity.getUser()))
                    .createdAt(entity.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .updatedAt(entity.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .deletedAt(entity.getDeletedAt() == null ? null : entity.getDeletedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .children(new ArrayList<>())
                    .build();
            dtoMap.put(dto.commentId(), dto);
        }

        for (CommentEntity entity : entities) {
            CommentDTO currentDto = dtoMap.get(entity.getCommentId());
            if (entity.getParent() == null) {
                roots.add(currentDto);
            } else {
                CommentDTO parentDto = dtoMap.get(entity.getParent().getCommentId());
                if (parentDto != null) {
                    parentDto.children().add(currentDto);
                }
            }
        }
        return roots;
    }
}