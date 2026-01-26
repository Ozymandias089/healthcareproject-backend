package com.hcproj.healthcareprojectbackend.me.service;

import com.hcproj.healthcareprojectbackend.auth.dto.internal.SocialConnectionDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.request.SocialConnectRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.request.SocialDisconnectRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.response.SocialConnectionsResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.SocialAccountEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.SocialAccountRepository;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.auth.social.SocialOAuthClient;
import com.hcproj.healthcareprojectbackend.auth.social.SocialProfile;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeSocialService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final SocialOAuthClient socialOAuthClient;

    @Transactional
    public void connectSocial(Long userId, SocialConnectRequestDTO request) {
        SocialProfile profile = socialOAuthClient.fetchProfileByCode(
                request.provider(),
                request.code(),
                request.redirectUri(),
                request.state()
        );

        // 1) 이 provider 계정이 이미 누군가에 연결되어 있나?
        var byProviderUser = socialAccountRepository.findByProviderAndProviderUserId(
                request.provider(), profile.providerUserId()
        );
        if (byProviderUser.isPresent()) {
            if (!byProviderUser.get().getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.SOCIAL_ACCOUNT_TAKEN);
            }
            // 이미 본인에게 연결된 케이스
            return;
        }

        // 2) 이 유저가 이미 같은 provider를 연결했나? (구글 2개 비허용)
        if (socialAccountRepository.existsByUserIdAndProvider(userId, request.provider())) {
            throw new BusinessException(ErrorCode.SOCIAL_ALREADY_CONNECTED);
        }

        SocialAccountEntity link = SocialAccountEntity.connect(userId, request.provider(), profile.providerUserId());
        socialAccountRepository.save(link);
    }


    @Transactional
    public void disconnectSocial(Long userId, SocialDisconnectRequestDTO request) {
        SocialAccountEntity link = socialAccountRepository.findByUserIdAndProvider(userId, request.provider())
                .orElseThrow(() -> new BusinessException(ErrorCode.SOCIAL_ACCOUNT_NOT_CONNECTED));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        long connectedCount = socialAccountRepository.countByUserId(userId);

        boolean hasNoPassword = (user.getPasswordHash() == null || user.getPasswordHash().isBlank());

        // 비밀번호 없으면 최소 1개는 연결 유지
        if (hasNoPassword && connectedCount <= 1) {
            throw new BusinessException(ErrorCode.CANNOT_DISCONNECT_LAST_LOGIN_METHOD);
        }

        socialAccountRepository.delete(link);
    }

    @Transactional(readOnly = true)
    public SocialConnectionsResponseDTO getSocialConnections(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<SocialAccountEntity> socials = socialAccountRepository.findAllByUserId(userId);

        return SocialConnectionsResponseDTO.builder()
                .handle(user.getHandle())
                .hasPassword(user.getPasswordHash() != null && !user.getPasswordHash().isBlank())
                .connections(
                        socials.stream()
                                .map(connection ->
                                        SocialConnectionDTO.builder()
                                                .provider(connection.getProvider())
                                                .providerUserId(connection.getProviderUserId())
                                                .connectedAt(connection.getConnectedAt())
                                                .build()
                                ).toList()
                )
                .build();
    }
}
