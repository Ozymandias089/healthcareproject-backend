package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.*;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO.AuthorDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO.CommentDTO;
import com.hcproj.healthcareprojectbackend.community.entity.Category;
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentService commentService; // ★ 댓글 가져올 때 필요함

    // 1. 목록 조회
    @Transactional(readOnly = true)
    public PostListResponse getPostList(String categoryStr, String searchBy, String q, Long cursorId, int size) {
        Category category = (categoryStr == null || categoryStr.equalsIgnoreCase("ALL")) ? null : Category.valueOf(categoryStr);
        if (q != null && q.trim().isEmpty()) q = null;

        List<PostSummaryDto> notices = List.of();
        if (cursorId == null) {
            notices = postRepository.findByIsNoticeTrueAndDeletedAtIsNullOrderByPostIdDesc().stream()
                    .map(this::convertToSummaryDto)
                    .toList();
        }

        List<PostEntity> entities = postRepository.findPostList(cursorId, category, searchBy, q, PageRequest.of(0, size + 1));
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

        List<PostSummaryDto> items = entities.stream().map(this::convertToSummaryDto).toList();
        return new PostListResponse(notices, items, new PostListResponse.PageInfo(nextCursorId, hasNext, size));
    }

    // 2. 상세 조회
    @Transactional
    public PostResponseDTO getPostDetail(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        post.increaseViewCount();

        // ★ 댓글 목록은 CommentService한테 "가져와!" 하고 시킵니다.
        List<CommentDTO> commentTree = commentService.getCommentsForPost(postId);

        return PostResponseDTO.builder()
                .postId(post.getPostId())
                .author(AuthorDTO.from(post.getUser()))
                .category(post.getCategory().name())
                .isNotice(post.getIsNotice())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .commentCount(commentTree.size()) // 트리 루트 개수 (전체 개수가 필요하면 로직 수정 필요)
                .content(post.getContent())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .updatedAt(post.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .deletedAt(post.getDeletedAt() == null ? null : post.getDeletedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .comments(commentTree)
                .build();
    }

    // 3. 게시글 작성
    @Transactional
    public PostResponseDTO createPost(Long userId, PostCreateRequestDTO request) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        boolean isNotice = Boolean.TRUE.equals(request.isNotice()) && user.getRole() == UserRole.ADMIN;

        PostEntity post = PostEntity.builder()
                .user(user)
                .category(Category.valueOf(request.category()))
                .title(request.title())
                .content(request.content())
                .isNotice(isNotice)
                .status(PostStatus.POSTED)
                .viewCount(0L)
                .build();

        return PostResponseDTO.from(postRepository.save(post));
    }

    // 4. 게시글 수정
    @Transactional
    public PostResponseDTO updatePost(Long userId, Long postId, PostUpdateRequestDTO request) {
        PostEntity post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!post.getUser().getId().equals(userId) && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        boolean finalIsNotice = user.getRole() == UserRole.ADMIN && Boolean.TRUE.equals(request.isNotice());
        post.update(request.title(), request.content(), Category.valueOf(request.category()), finalIsNotice);
        return PostResponseDTO.from(post);
    }

    // 5. 게시글 삭제
    @Transactional
    public PostDeleteResponseDTO deletePost(Long userId, Long postId) {
        PostEntity post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!post.getUser().getId().equals(userId) && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        postRepository.delete(post);
        return PostDeleteResponseDTO.of(LocalDateTime.now());
    }

    private PostSummaryDto convertToSummaryDto(PostEntity p) {
        return new PostSummaryDto(p.getPostId(), p.getCategory().name(), p.getIsNotice(), p.getTitle(), p.getUser().getNickname(), p.getUser().getHandle(), p.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(), p.getCommentCount(), p.getViewCount());
    }
}