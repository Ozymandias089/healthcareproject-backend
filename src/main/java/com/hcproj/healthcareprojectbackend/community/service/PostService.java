package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostListResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostSummaryDto;
import com.hcproj.healthcareprojectbackend.community.entity.*;
import com.hcproj.healthcareprojectbackend.community.repository.CommentRepository;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.community.repository.ReportRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public void createPost(Long userId, PostCreateRequestDTO request) {
        // 변경: existsById → findById로 변경하여 상태 체크
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 추가: 정지된 유저 체크
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.USER_SUSPENDED);
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

    /**
     * 게시글 목록 조회 (커서 기반 페이지네이션 + 동적 필터링 + 띄어쓰기 무시 검색)
     */
    public PostListResponseDTO getPostList(Long cursorId, String category, String searchBy, String q, int size) {
        int limitSize = size + 1;  // hasNext 판단용
        List<PostEntity> entities;

        // 1. 검색어 정규화
        String keyword = normalizeKeyword(q);

        // 2. 카테고리 정규화
        String normalizedCategory = normalizeCategory(category);

        // 3. 검색 타입 결정
        String searchType = (searchBy == null) ? "TITLE" : searchBy.toUpperCase().trim();
        boolean isAuthorSearch = "NICKNAME".equals(searchType) || "AUTHOR".equals(searchType);

        // 4. 6가지 케이스 분기 (Native Query 사용)
        if (keyword == null) {
            // 검색어 없음
            if (normalizedCategory == null) {
                entities = postRepository.findPostListAll(cursorId, limitSize);
            } else {
                entities = postRepository.findPostListByCategory(cursorId, normalizedCategory, limitSize);
            }
        } else {
            // 검색어 있음 - 띄어쓰기 제거 후 와일드카드 추가
            String likePattern = "%" + keyword.replace(" ", "") + "%";

            if (isAuthorSearch) {
                // 작성자 검색
                if (normalizedCategory == null) {
                    entities = postRepository.findPostListByAuthor(cursorId, likePattern, limitSize);
                } else {
                    entities = postRepository.findPostListByAuthorAndCategory(cursorId, normalizedCategory, likePattern, limitSize);
                }
            } else {
                // 제목 검색
                if (normalizedCategory == null) {
                    entities = postRepository.findPostListByTitle(cursorId, likePattern, limitSize);
                } else {
                    entities = postRepository.findPostListByTitleAndCategory(cursorId, normalizedCategory, likePattern, limitSize);
                }
            }
        }

        // 5. hasNext 판단
        boolean hasNext = entities.size() > size;
        if (hasNext) {
            entities = entities.subList(0, size);
        }

        // 6. 작성자 정보 조회
        Map<Long, UserEntity> userMap = getUserMap(entities);

        // 7. 다음 커서 ID
        Long nextCursorId = entities.isEmpty() ? null : entities.get(entities.size() - 1).getPostId();

        // 8. DTO 변환
        List<PostSummaryDto> dtos = entities.stream()
                .map(entity -> toSummaryDto(entity, userMap))
                .toList();

        return PostListResponseDTO.builder()
                .notices(List.of())
                .list(dtos)
                .pageInfo(PostListResponseDTO.PageInfo.builder()
                        .nextCursorId(nextCursorId)
                        .hasNext(hasNext)
                        .size(size)
                        .build())
                .build();
    }

    @Transactional
    public PostDetailResponseDTO getPostDetail(Long postId, Long currentUserId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        post.increaseViewCount();

        UserEntity writer = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        long views = (post.getViewCount() != null) ? post.getViewCount() : 0L;
        boolean isOwner = currentUserId != null && currentUserId.equals(post.getUserId());

        List<PostResponseDTO.CommentDTO> comments = commentService.getCommentTree(postId);
        long commentCount = commentRepository.countByPostId(postId);

        return PostDetailResponseDTO.builder()
                .postId(post.getPostId())
                .author(PostDetailResponseDTO.AuthorDTO.builder()
                        .userId(writer.getId())
                        .nickname(writer.getNickname())
                        .handle(writer.getHandle())
                        .profileImageUrl(writer.getProfileImageUrl())
                        .build())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(views)
                .commentCount(commentCount)
                .likeCount(0L)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isOwner(isOwner)
                .comments(comments)
                .build();
    }

    @Transactional
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

    @Transactional
    public void deletePost(Long userId, Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        post.delete();

        List<ReportEntity> pendingReports = reportRepository.findByTargetIdAndTypeAndStatus(
                postId,
                ReportType.POST,
                ReportStatus.PENDING
        );

        for (ReportEntity report : pendingReports) {
            report.process();
        }
    }

    // ============================================================
    // Private Helper Methods
    // ============================================================

    private String normalizeKeyword(String q) {
        if (q == null) return null;
        String trimmed = q.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeCategory(String category) {
        if (category == null) return null;
        String trimmed = category.trim();
        if (trimmed.isEmpty() || "ALL".equalsIgnoreCase(trimmed)) return null;
        return trimmed;
    }

    private Map<Long, UserEntity> getUserMap(List<PostEntity> entities) {
        List<Long> userIds = entities.stream()
                .map(PostEntity::getUserId)
                .distinct()
                .toList();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
    }

    private PostSummaryDto toSummaryDto(PostEntity entity, Map<Long, UserEntity> userMap) {
        UserEntity author = userMap.get(entity.getUserId());
        String nickname = (author != null) ? author.getNickname() : "알 수 없음";
        String handle = (author != null) ? author.getHandle() : "";

        return new PostSummaryDto(
                entity.getPostId(),
                entity.getCategory(),
                entity.getIsNotice(),
                entity.getTitle(),
                nickname,
                handle,
                entity.getCreatedAt(),
                0L,
                entity.getViewCount(),
                0L
        );
    }
}