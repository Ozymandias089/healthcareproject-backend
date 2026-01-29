package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.request.AdminNoticeCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminPostListResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.community.entity.*;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.community.repository.ReportRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.util.UtilityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 게시판 서비스
 */
@Service
@RequiredArgsConstructor
public class AdminBoardService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    /**
     * 관리자 게시판 통합 조회
     */
    @Transactional(readOnly = true)
    public AdminPostListResponseDTO getAdminPostList(
            int page,
            int size,
            String category,
            String status,
            String keyword
    ) {
        // 1) 파라미터 변환
        String categoryParam = (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category))
                ? null : category.toUpperCase();

        String statusParam = null;
        if (status != null && !status.isBlank()) {
            try {
                PostStatus.valueOf(status.toUpperCase());
                statusParam = status.toUpperCase();
            } catch (IllegalArgumentException e) {
                statusParam = null;
            }
        }

        // 2) 검색어 정규화
        String normalizedKeyword = UtilityProvider.normalizeKeyword(keyword);

        // 3) 페이지네이션 계산
        int offsetSize = page * size;

        // 4) 게시글 조회 (검색어 유무에 따라 분기)
        List<PostEntity> posts;
        long total;

        if (normalizedKeyword == null) {
            // 검색어 없음
            posts = postRepository.findAdminPostListNoKeyword(
                    categoryParam, statusParam, size, offsetSize
            );
            total = postRepository.countAdminPostListNoKeyword(categoryParam, statusParam);
        } else {
            // 검색어 있음 - 띄어쓰기 제거 + 소문자 변환 + 와일드카드 추가
            String likePattern = "%" + normalizedKeyword.toLowerCase().replace(" ", "") + "%";
            posts = postRepository.findAdminPostListWithKeyword(
                    categoryParam, statusParam, likePattern, size, offsetSize
            );
            total = postRepository.countAdminPostListWithKeyword(categoryParam, statusParam, likePattern);
        }

        // 5) 작성자 정보 조회 (N+1 방지)
        List<Long> userIds = posts.stream()
                .map(PostEntity::getUserId)
                .distinct()
                .toList();

        Map<Long, UserEntity> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        // 6) 응답 DTO 변환
        List<AdminPostListResponseDTO.AdminPostItemDTO> list = posts.stream()
                .map(post -> {
                    UserEntity user = userMap.get(post.getUserId());
                    return AdminPostListResponseDTO.AdminPostItemDTO.builder()
                            .postId(post.getPostId())
                            .author(AdminPostListResponseDTO.AuthorDTO.builder()
                                    .nickname(user != null ? user.getNickname() : "알 수 없음")
                                    .handle(user != null ? user.getHandle() : "unknown")
                                    .build())
                            .category(post.getCategory())
                            .title(post.getTitle())
                            .viewCount(post.getViewCount())
                            .isNotice(post.getIsNotice())
                            .status(post.getStatus())
                            .createdAt(post.getCreatedAt())
                            .build();
                })
                .toList();

        return AdminPostListResponseDTO.builder()
                .total(total)
                .list(list)
                .build();
    }

    /**
     * 공지사항 등록
     */
    @Transactional
    public PostResponseDTO createNotice(Long adminUserId, AdminNoticeCreateRequestDTO request) {
        UserEntity adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        PostEntity post = PostEntity.builder()
                .userId(adminUserId)
                .category(request.category().toUpperCase())
                .title(request.title())
                .content(request.content())
                .isNotice(request.isNotice())
                .status(PostStatus.POSTED)
                .viewCount(0L)
                .build();

        PostEntity savedPost = postRepository.save(post);

        return PostResponseDTO.builder()
                .postId(savedPost.getPostId())
                .author(new PostResponseDTO.AuthorDTO(adminUser.getNickname(), adminUser.getHandle()))
                .category(savedPost.getCategory())
                .isNotice(savedPost.getIsNotice())
                .title(savedPost.getTitle())
                .viewCount(savedPost.getViewCount())
                .commentCount(0)
                .content(savedPost.getContent())
                .status(savedPost.getStatus())
                .createdAt(savedPost.getCreatedAt())
                .updatedAt(null)
                .deletedAt(null)
                .comments(Collections.emptyList())
                .build();
    }

    /**
     * 게시글 복구 (Soft Delete 해제)
     */
    @Transactional
    public void restorePost(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (post.getStatus() == PostStatus.POSTED) {
            return;
        }

        post.restore();
    }

    @Transactional
    public void deletePost(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 1. 게시글 삭제 (Soft Delete)
        post.delete();

        // 2. 관련 신고 자동 처리 (PostService와 동일한 로직)
        List<ReportEntity> pendingReports = reportRepository.findByTargetIdAndTypeAndStatus(
                postId,
                ReportType.POST,
                ReportStatus.PENDING
        );

        for (ReportEntity report : pendingReports) {
            report.process(); // 신고 상태를 PROCESSED로 변경
        }
    }

    // ============================================================
    // Private Helper Methods
    // ============================================================

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}