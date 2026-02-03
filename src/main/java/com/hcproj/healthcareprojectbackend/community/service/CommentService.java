package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.CommentCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.CommentUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentCreateResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentDeleteResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentUpdateResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 작성
     * - 부모 댓글 유효성 검증 포함
     */
    @Transactional
    public CommentCreateResponseDTO createComment(Long userId, Long postId, CommentCreateRequestDTO request) {
        // 변경: existsById → findById로 변경하여 상태 체크
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 추가: 정지된 유저 체크
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.USER_SUSPENDED);
        }
        // 2. 게시글 존재 확인
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 3. 부모 댓글 검증 (대댓글인 경우)
        Long parentId = null;
        if (request.parentId() != null && request.parentId() != 0) {
            CommentEntity parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

            // [방어 로직] 부모 댓글이 다른 게시글에 속해 있다면 에러 (데이터 꼬임 방지)
            if (!parent.getPostId().equals(postId)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST);
            }
            // [방어 로직] 삭제된 댓글에는 대댓글 작성 불가 (정책에 따라 다름, 여기선 금지)
            if (parent.getStatus() == CommentStatus.DELETED) {
                throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND); // 혹은 적절한 에러코드
            }

            parentId = parent.getCommentId();
        }

        // 4. 저장
        CommentEntity comment = CommentEntity.builder()
                .postId(postId)
                .userId(userId)
                .parentCommentId(parentId)
                .content(request.content())
                .status(CommentStatus.POSTED)
                .build();

        CommentEntity saved = commentRepository.save(comment);

        return CommentCreateResponseDTO.of(saved.getCommentId(), saved.getCreatedAt());
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentUpdateResponseDTO updateComment(Long userId, Long postId, Long commentId, CommentUpdateRequestDTO request) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 이미 삭제된 댓글은 수정 불가
        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        comment.update(request.content());

        return CommentUpdateResponseDTO.of(comment.getCommentId(), comment.getUpdatedAt()); // createdAt -> updatedAt 권장
    }

    /**
     * 댓글 삭제
     * - 물리 삭제가 아닌 상태 변경(DELETED) -> 멱등성 보장
     */
    @Transactional
    public CommentDeleteResponseDTO deleteComment(Long userId, Long postId, Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            // 관리자라면 삭제 가능하게 할 수도 있음 (여기선 본인만)
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 이미 삭제된 경우에도 에러 없이 성공 응답 (멱등성)
        if (comment.getStatus() != CommentStatus.DELETED) {
            comment.delete(); // 상태 변경 및 deletedAt 기록
        }

        return CommentDeleteResponseDTO.of(comment.getDeletedAt());
    }

    /**
     * [PostService에서 호출]
     * 게시글의 댓글을 계층형(Tree) 구조로 변환하여 반환
     */
    public List<PostResponseDTO.CommentDTO> getCommentTree(Long postId) {
        List<CommentEntity> entities = commentRepository.findAllByPostId(postId);

        // 작성자 정보 조회를 위해 ID 수집 (N+1 방지)
        List<Long> userIds = entities.stream().map(CommentEntity::getUserId).distinct().toList();
        Map<Long, UserEntity> userMap = new HashMap<>();
        userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getId(), u));

        // DTO 변환 및 Map 저장
        Map<Long, PostResponseDTO.CommentDTO> dtoMap = new HashMap<>();
        List<PostResponseDTO.CommentDTO> roots = new ArrayList<>();

        // 1. Entity -> DTO 변환 (부모/자식 연결 전)
        for (CommentEntity entity : entities) {
            UserEntity author = userMap.get(entity.getUserId());
            AuthorDTO authorDTO = (author != null)
                    ? new AuthorDTO(author.getNickname(), author.getHandle())
                    : new AuthorDTO("알수없음", "unknown");

            // [핵심] 삭제된 댓글 처리: "삭제된 댓글입니다" 마스킹
            String content = (entity.getStatus() == CommentStatus.DELETED)
                    ? "삭제된 댓글입니다."
                    : entity.getContent();

            CommentDTO dto = CommentDTO.builder()
                    .commentId(entity.getCommentId())
                    .content(content)
                    .author(authorDTO)
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .deletedAt(entity.getDeletedAt())
                    .children(new ArrayList<>()) // 자식 리스트 초기화
                    .build();

            dtoMap.put(entity.getCommentId(), dto);
        }

        // 2. 트리 구조 조립
        for (CommentEntity entity : entities) {
            CommentDTO currentDto = dtoMap.get(entity.getCommentId());
            if (entity.getParentCommentId() == null) {
                // 부모가 없으면 최상위 댓글
                roots.add(currentDto);
            } else {
                // 부모가 있으면 부모 DTO의 children에 추가
                CommentDTO parentDto = dtoMap.get(entity.getParentCommentId());
                if (parentDto != null) {
                    parentDto.children().add(currentDto);
                } else {
                    // 부모가 DB에는 없는데 자식만 있는 경우(고아 객체) -> 최상위로 취급하거나 버림
                    // 여기선 안전하게 최상위로 노출
                    roots.add(currentDto);
                }
            }
        }

        return roots;
    }
}