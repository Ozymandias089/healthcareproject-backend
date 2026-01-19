package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDeleteResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostListResponse;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO.AuthorDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO.CommentDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostSummaryDto;
import com.hcproj.healthcareprojectbackend.community.entity.Category;
import com.hcproj.healthcareprojectbackend.community.entity.CommentEntity;
import com.hcproj.healthcareprojectbackend.community.entity.CommentStatus;
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import com.hcproj.healthcareprojectbackend.community.repository.CommentRepository;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    // [게시글 목록 조회]
    @Transactional(readOnly = true)
    public PostListResponse getPostList(String categoryStr, String searchBy, String q, Long cursorId, int size) {
        Category category = (categoryStr == null || categoryStr.equalsIgnoreCase("ALL"))
                ? null
                : Category.valueOf(categoryStr);

        if (q != null && q.trim().isEmpty()) q = null;

        List<PostSummaryDto> notices = List.of();
        if (cursorId == null) {
            notices = postRepository.findByIsNoticeTrueAndDeletedAtIsNullOrderByPostIdDesc().stream()
                    .map(this::convertToSummaryDto)
                    .toList();
        }

        List<PostEntity> entities = postRepository.findPostList(
                cursorId, category, searchBy, q, PageRequest.of(0, size + 1)
        );

        boolean hasNext = false;
        Long nextCursorId = null;

        if (entities.size() > size) {
            hasNext = true;
            entities.remove(size);
            nextCursorId = entities.get(entities.size() - 1).getPostId();
        } else if (!entities.isEmpty()) {
            nextCursorId = entities.get(entities.size() - 1).getPostId();
        } else {
            nextCursorId = -1L;
        }

        List<PostSummaryDto> items = entities.stream()
                .map(this::convertToSummaryDto)
                .toList();

        return new PostListResponse(notices, items, new PostListResponse.PageInfo(nextCursorId, hasNext, size));
    }

    // [게시글 상세 조회]
    @Transactional
    public PostResponseDTO getPostDetail(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        post.increaseViewCount();

        List<CommentEntity> commentEntities = commentRepository.findAllByPostIdForDetail(postId);
        List<CommentDTO> commentTree = convertToCommentTree(commentEntities);

        return PostResponseDTO.builder()
                .postId(post.getPostId())
                .author(AuthorDTO.from(post.getUser()))
                .category(post.getCategory().name())
                .isNotice(post.getIsNotice())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .commentCount(commentEntities.size())
                .content(post.getContent())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .updatedAt(post.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .deletedAt(post.getDeletedAt() == null ? null : post.getDeletedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .comments(commentTree)
                .build();
    }

    // [게시글 생성]
    @Transactional
    public PostResponseDTO createPost(Long userId, PostCreateRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isNotice = Boolean.TRUE.equals(request.isNotice());
        if (user.getRole() != UserRole.ADMIN) {
            isNotice = false;
        }

        PostEntity post = PostEntity.builder()
                .user(user)
                .category(Category.valueOf(request.category()))
                .title(request.title())
                .content(request.content())
                .isNotice(isNotice)
                .status(PostStatus.POSTED)
                .viewCount(0L)
                .build();

        PostEntity savedPost = postRepository.save(post);
        return PostResponseDTO.from(savedPost);
    }

    // [게시글 수정]
    @Transactional
    public PostResponseDTO updatePost(Long userId, Long postId, PostUpdateRequestDTO request) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        boolean isWriter = post.getUser().getId().equals(userId);
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isWriter && !isAdmin) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        boolean finalIsNotice = false;
        if (isAdmin) {
            finalIsNotice = Boolean.TRUE.equals(request.isNotice());
        } else {
            finalIsNotice = false;
        }

        post.update(
                request.title(),
                request.content(),
                Category.valueOf(request.category()),
                finalIsNotice
        );
        return PostResponseDTO.from(post);
    }

    // [게시글 삭제]
    @Transactional
    public PostDeleteResponseDTO deletePost(Long userId, Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isWriter = post.getUser().getId().equals(userId);
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isWriter && !isAdmin) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        postRepository.delete(post);
        return PostDeleteResponseDTO.of(LocalDateTime.now());
    }

    // [내부 메서드] Entity -> DTO
    private PostSummaryDto convertToSummaryDto(PostEntity p) {
        return new PostSummaryDto(
                p.getPostId(),
                p.getCategory().name(),
                p.getIsNotice(),
                p.getTitle(),
                p.getUser().getNickname(),
                p.getUser().getHandle(),
                p.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                p.getCommentCount(),
                p.getViewCount()
        );
    }

    // [내부 메서드] 댓글 트리 변환
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