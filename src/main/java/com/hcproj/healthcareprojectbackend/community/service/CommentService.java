package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
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

    /**
     * 댓글 생성
     */
    @Transactional
    public CommentCreateResponseDTO createComment(Long userId, Long postId, CommentCreateRequestDTO request) {
        // [수정됨 1] existsById 대신 findById로 유저 정보를 가져와야 합니다.
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!postRepository.existsById(postId)) throw new BusinessException(ErrorCode.POST_NOT_FOUND);

        // [수정됨 2] 가져온 user 변수로 상태 확인
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.USER_SUSPENDED);
        }

        // 대댓글인 경우 부모 댓글 검증
        if (request.parentId() != null) {
            CommentEntity parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
            // 부모 댓글이 같은 게시글에 있는지 확인
            if (!parent.getPostId().equals(postId)) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        CommentEntity comment = CommentEntity.builder()
                .userId(userId)
                .postId(postId)
                .parentCommentId(request.parentId())
                .content(request.content())
                .status(CommentStatus.POSTED)
                .build();

        CommentEntity saved = commentRepository.save(comment);
        return CommentCreateResponseDTO.of(saved.getCommentId(), saved.getCreatedAt());
    }

    /**
     * 특정 게시글의 댓글 목록 조회 (계층형 구조 변환 포함)
     */
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsForPost(Long postId) {
        // 1. 해당 게시글의 모든 댓글 조회
        List<CommentEntity> entities = commentRepository.findAllByPostId(postId);
        if (entities.isEmpty()) return new ArrayList<>();

        // 2. 작성자 정보 일괄 조회 (N+1 문제 방지)
        Set<Long> userIds = entities.stream().map(CommentEntity::getUserId).collect(Collectors.toSet());
        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        // 3. 계층형(Tree) 구조로 변환하여 반환
        return convertToCommentTree(entities, userMap);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentUpdateResponseDTO updateComment(Long userId, Long postId, Long commentId, CommentUpdateRequestDTO request) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 게시글 ID 검증
        if (!Objects.equals(comment.getPostId(), postId)) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        // 작성자 본인 확인
        if (!comment.getUserId().equals(userId)) throw new BusinessException(ErrorCode.NOT_COMMENT_AUTHOR);

        comment.update(request.content());
        return CommentUpdateResponseDTO.of(comment.getCommentId(), comment.getUpdatedAt());
    }

    /**
     * 댓글 삭제 (Soft Delete)
     */
    @Transactional
    public CommentDeleteResponseDTO deleteComment(Long userId, Long postId, Long commentId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 게시글 ID 검증
        if (!comment.getPostId().equals(postId)) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        // 작성자 본인이거나 관리자인 경우에만 삭제 가능
        if (!comment.getUserId().equals(userId) && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.NOT_COMMENT_AUTHOR);
        }

        // 이미 삭제된 경우 처리
        if (comment.getStatus() == CommentStatus.DELETED) {
            return CommentDeleteResponseDTO.of(comment.getDeletedAt());
        }

        // Soft Delete 수행 (status -> DELETED)
        comment.delete();

        return CommentDeleteResponseDTO.of(
                comment.getDeletedAt() != null ? comment.getDeletedAt() : Instant.now()
        );
    }

    // [내부 메서드] Entity 리스트 -> 계층형 DTO 변환 및 '삭제된 댓글' 마스킹 처리
    private List<CommentDTO> convertToCommentTree(List<CommentEntity> entities, Map<Long, UserEntity> userMap) {
        Map<Long, CommentDTO> dtoMap = new HashMap<>();
        List<CommentDTO> roots = new ArrayList<>();

        // 1. DTO 변환 및 Map 저장
        for (CommentEntity entity : entities) {
            UserEntity author = userMap.get(entity.getUserId());
            AuthorDTO authorDTO = (author != null)
                    ? new AuthorDTO(author.getNickname(), author.getHandle())
                    : new AuthorDTO("알수없음", "unknown");

            // [핵심] 삭제된 댓글(DELETED)인 경우 내용을 가린다.
            String content = (entity.getStatus() == CommentStatus.DELETED)
                    ? "삭제된 댓글입니다."
                    : entity.getContent();

            CommentDTO dto = CommentDTO.builder()
                    .commentId(entity.getCommentId())
                    .content(content) // 가려진 내용 또는 원본
                    .author(authorDTO)
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .deletedAt(entity.getDeletedAt())
                    .children(new ArrayList<>())
                    .build();

            dtoMap.put(dto.commentId(), dto);
        }

        // 2. 부모-자식 관계 연결 (Tree 구조 생성)
        for (CommentEntity entity : entities) {
            CommentDTO currentDto = dtoMap.get(entity.getCommentId());
            if (entity.getParentCommentId() == null) {
                roots.add(currentDto); // 최상위 댓글
            } else {
                CommentDTO parentDto = dtoMap.get(entity.getParentCommentId());
                if (parentDto != null) {
                    parentDto.children().add(currentDto); // 대댓글 추가
                }
            }
        }

        return roots;
    }
}