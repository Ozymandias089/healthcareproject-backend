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
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant; //
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentService commentService;

    @Transactional(readOnly = true)
    public PostListResponse getPostList(String categoryStr, String searchBy, String q, Long cursorId, int size) {
        String category = (categoryStr == null || categoryStr.equalsIgnoreCase("ALL")) ? null : categoryStr;
        if (q != null && q.trim().isEmpty()) q = null;

        List<PostEntity> noticeEntities = List.of();
        if (cursorId == null) {
            noticeEntities = postRepository.findByIsNoticeTrueAndStatusOrderByPostIdDesc(PostStatus.POSTED);
        }

        List<PostEntity> entities = postRepository.findPostList(cursorId, category, q, PageRequest.of(0, size + 1));

        boolean hasNext = false;
        Long nextCursorId = null;
        if (entities.size() > size) {
            hasNext = true;
            entities.remove(size);
        }
        if (!entities.isEmpty()) {
            nextCursorId = entities.get(entities.size() - 1).getPostId();
        } else {
            nextCursorId = -1L;
        }

        Set<Long> userIds = entities.stream().map(PostEntity::getUserId).collect(Collectors.toSet());
        noticeEntities.forEach(n -> userIds.add(n.getUserId()));

        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        List<PostSummaryDto> notices = noticeEntities.stream()
                .map(p -> convertToSummaryDto(p, userMap.get(p.getUserId())))
                .toList();
        List<PostSummaryDto> items = entities.stream()
                .map(p -> convertToSummaryDto(p, userMap.get(p.getUserId())))
                .toList();

        return new PostListResponse(notices, items, new PostListResponse.PageInfo(nextCursorId, hasNext, size));
    }

    @Transactional
    public PostResponseDTO getPostDetail(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        UserEntity author = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        post.increaseViewCount();
        List<CommentDTO> commentTree = commentService.getCommentsForPost(postId);

        return PostResponseDTO.builder()
                .postId(post.getPostId())
                .author(new AuthorDTO(author.getNickname(), author.getHandle()))
                .category(post.getCategory())
                .isNotice(post.getIsNotice())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .commentCount(commentTree.size())
                .content(post.getContent())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .deletedAt(post.getDeletedAt())
                .comments(commentTree)
                .build();
    }

    @Transactional
    public PostResponseDTO createPost(Long userId, PostCreateRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        PostEntity post = PostEntity.builder()
                .userId(userId)
                .category(request.category())
                .title(request.title())
                .content(request.content())
                .isNotice(Boolean.TRUE.equals(request.isNotice()) && user.getRole() == UserRole.ADMIN)
                .status(PostStatus.POSTED)
                .viewCount(0L)
                .build();

        PostEntity saved = postRepository.save(post);
        return PostResponseDTO.builder()
                .postId(saved.getPostId())
                .author(new AuthorDTO(user.getNickname(), user.getHandle()))
                .category(saved.getCategory())
                .isNotice(saved.getIsNotice())
                .title(saved.getTitle())
                .viewCount(saved.getViewCount())
                .content(saved.getContent())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .comments(List.of())
                .build();
    }

    @Transactional
    public PostResponseDTO updatePost(Long userId, Long postId, PostUpdateRequestDTO request) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!post.getUserId().equals(userId) && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        post.update(request.title(), request.content(), request.category(),
                Boolean.TRUE.equals(request.isNotice()) && user.getRole() == UserRole.ADMIN);

        return PostResponseDTO.builder()
                .postId(post.getPostId())
                .author(new AuthorDTO(user.getNickname(), user.getHandle()))
                .category(post.getCategory())
                .isNotice(post.getIsNotice())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .content(post.getContent())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    @Transactional
    public PostDeleteResponseDTO deletePost(Long userId, Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!post.getUserId().equals(userId) && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        // 서비스 레이어에서 역등성 체크 (엔티티를 건드리지 않는 방식)
        if (post.isDeleted()) {
            return PostDeleteResponseDTO.of(post.getDeletedAt());
        }

        post.delete();
        return PostDeleteResponseDTO.of(post.getDeletedAt() != null ? post.getDeletedAt() : Instant.now());
    }

    private PostSummaryDto convertToSummaryDto(PostEntity p, UserEntity author) {
        String nickname = (author != null) ? author.getNickname() : "알수없음";
        String handle = (author != null) ? author.getHandle() : "unknown";

        return new PostSummaryDto(
                p.getPostId(), p.getCategory(), p.getIsNotice(), p.getTitle(),
                nickname, handle, p.getCreatedAt(),
                0L, // Long 타입 준수
                p.getViewCount()
        );
    }
}