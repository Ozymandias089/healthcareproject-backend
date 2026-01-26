package com.hcproj.healthcareprojectbackend.auth.social;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.auth.entity.SocialProvider;
import com.hcproj.healthcareprojectbackend.global.config.properties.SocialOAuthProperties;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class RestTemplateSocialOAuthClient implements SocialOAuthClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SocialOAuthProperties props;

    @Override
    public SocialProfile fetchProfile(SocialProvider provider, String accessToken) {
        if (provider == null || accessToken == null || accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        try {
            return switch (provider) {
                case GOOGLE -> fetchGoogle(accessToken);
                case KAKAO -> fetchKakao(accessToken);
                case NAVER -> fetchNaver(accessToken);
            };
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private SocialProfile fetchGoogle(String accessToken) throws Exception {
        String url = props.getGoogle().getUserinfoUrl();

        JsonNode root = getJson(url, accessToken);

        String sub = text(root, "sub");
        String email = textOrNull(root, "email");
        String name = textOrNull(root, "name");
        String picture = textOrNull(root, "picture");

        if (sub == null || sub.isBlank()) throw new BusinessException(ErrorCode.INVALID_TOKEN);
        return new SocialProfile(sub, email, name, picture);
    }

    private SocialProfile fetchKakao(String accessToken) throws Exception {
        String url = props.getKakao().getMeUrl();

        JsonNode root = getJson(url, accessToken);

        String id = root.hasNonNull("id") ? root.get("id").asText() : null;

        JsonNode kakaoAccount = root.path("kakao_account");
        String email = kakaoAccount.path("email").isMissingNode() ? null : kakaoAccount.path("email").asText(null);

        String nickname = textOrNull(root.path("properties"), "nickname");
        if (nickname == null) nickname = textOrNull(kakaoAccount.path("profile"), "nickname");

        String profileImage = textOrNull(root.path("properties"), "profile_image");
        if (profileImage == null) profileImage = textOrNull(kakaoAccount.path("profile"), "profile_image_url");

        if (id == null || id.isBlank()) throw new BusinessException(ErrorCode.INVALID_TOKEN);
        return new SocialProfile(id, email, nickname, profileImage);
    }

    private SocialProfile fetchNaver(String accessToken) throws Exception {
        String url = props.getNaver().getMeUrl();

        JsonNode root = getJson(url, accessToken);
        JsonNode response = root.path("response");

        String id = text(response, "id");
        String email = textOrNull(response, "email");
        String nickname = textOrNull(response, "nickname");
        if (nickname == null) nickname = textOrNull(response, "name");

        String profileImage = textOrNull(response, "profile_image");

        if (id == null || id.isBlank()) throw new BusinessException(ErrorCode.INVALID_TOKEN);
        return new SocialProfile(id, email, nickname, profileImage);
    }

    private JsonNode getJson(String url, String accessToken) throws Exception {
        if (url == null || url.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return objectMapper.readTree(resp.getBody());
    }

    private String text(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    private String textOrNull(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) return null;
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }
}
