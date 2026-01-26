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

    @Override
    public SocialProfile fetchProfileByCode(SocialProvider provider, String code, String redirectUri, String state) {
        if (provider == null || code == null || code.isBlank() || redirectUri == null || redirectUri.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String accessToken = exchangeAccessToken(provider, code, redirectUri, state);
        return fetchProfile(provider, accessToken); // 기존 로직 재사용
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

    private String exchangeAccessToken(SocialProvider provider, String code, String redirectUri, String state) {
        return switch (provider) {
            case GOOGLE -> exchangeGoogle(code, redirectUri);
            case KAKAO -> exchangeKakao(code, redirectUri);
            case NAVER -> exchangeNaver(code, state);
        };
    }

    private String exchangeGoogle(String code, String redirectUri) {
        // Google: https://oauth2.googleapis.com/token, grant_type=authorization_code :contentReference[oaicite:3]{index=3}
        String tokenUrl = props.getGoogle().getTokenUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", props.getGoogle().getClientId());
        form.add("client_secret", props.getGoogle().getClientSecret());
        form.add("redirect_uri", redirectUri);

        ResponseEntity<String> resp = restTemplate.postForEntity(tokenUrl, new HttpEntity<>(form, headers), String.class);
        return extractAccessTokenOrThrow(resp);
    }

    private String exchangeKakao(String code, String redirectUri) {
        // Kakao: https://kauth.kakao.com/oauth/token, form-urlencoded :contentReference[oaicite:4]{index=4}
        String tokenUrl = props.getKakao().getTokenUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", props.getKakao().getClientId());      // REST API key
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        // client_secret 기능 ON이면 반드시 포함 필요(카카오 문서 주의사항) :contentReference[oaicite:5]{index=5}
        String clientSecret = props.getKakao().getClientSecret();
        if (clientSecret != null && !clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }

        ResponseEntity<String> resp = restTemplate.postForEntity(tokenUrl, new HttpEntity<>(form, headers), String.class);
        return extractAccessTokenOrThrow(resp);
    }

    private String exchangeNaver(String code, String state) {
        // Naver 예시: https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&client_id=...&client_secret=...&code=...&state=...
        // :contentReference[oaicite:6]{index=6}
        if (state == null || state.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE); // NAVER는 state 권장(검증까지 하면 더 좋음)
        }

        String tokenUrl = props.getNaver().getTokenUrl(); // ex) https://nid.naver.com/oauth2.0/token

        String url = org.springframework.web.util.UriComponentsBuilder
                .fromHttpUrl(tokenUrl)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", props.getNaver().getClientId())
                .queryParam("client_secret", props.getNaver().getClientSecret())
                .queryParam("code", code)
                .queryParam("state", state)
                .build(true)
                .toUriString();

        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        return extractAccessTokenOrThrow(resp);
    }

    private String extractAccessTokenOrThrow(ResponseEntity<String> resp) {
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        try {
            JsonNode root = objectMapper.readTree(resp.getBody());
            String accessToken = root.path("access_token").asText(null);
            if (accessToken == null || accessToken.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
            return accessToken;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
