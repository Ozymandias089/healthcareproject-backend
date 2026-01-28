package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostListResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO; // [수정] Import 추가
import com.hcproj.healthcareprojectbackend.community.dto.response.PostSummaryDto;
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import com.hcproj.healthcareprojectbackend.community.repository.CommentRepository; // [수정] Import 추가
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 커뮤니티 게시글 관련 비즈니스 로직 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // [수정] 클래스 레벨에 읽기 전용 트랜잭션 적용 (성능 최적화)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // [수정] 댓글 조회 기능을 위해 의존성 주입 추가
    private final CommentService commentService;
    private final CommentRepository commentRepository;

    @Transactional // [수정] 쓰기 작업이므로 트랜잭션 허용 (readOnly = false)
    public void createPost(Long userId, PostCreateRequestDTO request) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        PostEntity post = PostEntity.builder()
                .userId(userId)
                .category(request.category())
                .title(request.title())
                .content(request.content())
                .status(PostStatus.POSTED)
                .viewCount(0L)
                .isNotice(false)
                .build();

        postRepository.save(post);
    }

    // getPostList는 클래스 레벨의 @Transactional(readOnly = true)가 적용됩니다.
    public PostListResponseDTO getPostList(Long cursorId, String category, String searchBy, String q, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);
        List<PostEntity> entities;

        if (q == null || q.isBlank()) {
            entities = postRepository.findPostList(cursorId, category, pageable);
        } else {
            String type = (searchBy == null) ? "TITLE" : searchBy.toUpperCase();

            switch (type) {
                case "NICKNAME", "AUTHOR" ->
                        entities = postRepository.searchByAuthor(cursorId, category, q, pageable);

                case "TITLE" ->
                        entities = postRepository.searchByTitle(cursorId, category, q, pageable);

                default ->
                        entities = postRepository.searchByTitle(cursorId, category, q, pageable);
            }
        }

        boolean hasNext = entities.size() > size;
        if (hasNext) {
            entities.remove(size);
        }

        List<Long> userIds = entities.stream()
                .map(PostEntity::getUserId)
                .distinct()
                .toList();

        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        Long nextCursorId = entities.isEmpty() ? null : entities.get(entities.size() - 1).getPostId();

        List<PostSummaryDto> dtos = entities.stream()
                .map(entity -> {
                    UserEntity author = userMap.get(entity.getUserId());
                    String nickname = (author != null) ? author.getNickname() : "알 수 없음";
                    String handle = (author != null) ? author.getHandle() : "";

                    // 목록에서는 성능을 위해 댓글 수를 일단 0으로 둡니다.
                    // (필요 시 PostEntity에 commentCount 컬럼을 추가하거나 @Formula 사용 권장)
                    long commentCount = 0L;

                    return new PostSummaryDto(
                            entity.getPostId(),
                            entity.getCategory(),
                            entity.getIsNotice(),
                            entity.getTitle(),
                            nickname,
                            handle,
                            entity.getCreatedAt(),
                            commentCount,
                            entity.getViewCount(),
                            0L
                    );
                })
                .toList();

        return PostListResponseDTO.builder()
                .notices(List.of())
                .items(dtos)
                .pageInfo(PostListResponseDTO.PageInfo.builder()
                        .nextCursorId(nextCursorId)
                        .hasNext(hasNext)
                        .size(size)
                        .build())
                .build();
    }

    @Transactional // [수정] 조회수 증가(Dirty Checking)가 DB에 반영되려면 필수입니다.
    public PostDetailResponseDTO getPostDetail(Long postId, Long currentUserId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        post.increaseViewCount(); // [동작] @Transactional 덕분에 메서드 종료 시 DB Update 쿼리가 실행됩니다.

        UserEntity writer = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        long views = (post.getViewCount() != null) ? post.getViewCount() : 0L;
        boolean isOwner = currentUserId != null && currentUserId.equals(post.getUserId());

        // [수정] CommentService를 통해 실제 댓글 계층 구조(대댓글 포함)를 조회합니다. (기존: 빈 리스트 반환 문제 해결)
        List<PostResponseDTO.CommentDTO> comments = commentService.getCommentTree(postId);

        // [수정] 댓글 개수를 조회합니다. (Repository에 countByPostId가 없으면 이렇게 리스트 사이즈로 대체 가능)
        // 추후 성능 최적화를 위해 CommentRepository에 'long countByPostId(Long postId);' 메서드 추가를 권장합니다.
        long commentCount = commentRepository.findAllByPostId(postId).size();

        return PostDetailResponseDTO.builder()
                .postId(post.getPostId())
                .author(PostDetailResponseDTO.AuthorDTO.builder()
                        .userId(writer.getId())
                        .nickname(writer.getNickname())
                        .handle(writer.getHandle())
                        .profileImageUrl(writer.getProfileImageUrl()) // [수정] null 대신 실제 이미지 URL 반환
                        .build())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(views)
                .commentCount(commentCount) // [수정] 실제 댓글 개수 반영
                .likeCount(0L)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt()) // [수정] null 대신 실제 수정일 반환
                .isOwner(isOwner)
                .comments(comments) // [수정] 빈 리스트(List.of()) 대신 실제 조회된 댓글 리스트 반환
                .build();
    }

    @Transactional // [수정] 쓰기 작업 명시
    public void updatePost(Long userId, Long postId, PostUpdateRequestDTO request) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        post.update(
                request.title(),
                request.content(),
                request.category(),
                request.isNotice()
        );
    }

    @Transactional // [수정] 쓰기 작업 명시
    public void deletePost(Long userId, Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        post.delete();
    }
}