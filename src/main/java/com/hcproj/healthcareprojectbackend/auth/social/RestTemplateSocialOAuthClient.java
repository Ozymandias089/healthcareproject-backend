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

/**
 * {@link SocialOAuthClient}의 RestTemplate 기반 구현체(Adapter).
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>공급자별 토큰 교환(Authorization Code → Access Token) 및 사용자 정보 조회(userinfo/me) API를 호출한다.</li>
 *   <li>응답 JSON을 파싱하여 {@link SocialProfile}로 표준화한다.</li>
 *   <li>공급자별 필드명/응답 구조 차이를 내부 메서드로 캡슐화한다.</li>
 * </ul>
 *
 * <p><b>설정 의존성</b></p>
 * {@link SocialOAuthProperties}를 통해 각 공급자의 token/userinfo/me URL 및 client 설정을 주입받는다.
 *
 * <p><b>예외 정책</b></p>
 * <ul>
 *   <li>입력값 검증 실패: {@link ErrorCode#INVALID_INPUT_VALUE}</li>
 *   <li>외부 호출 실패/토큰 문제: {@link ErrorCode#INVALID_TOKEN}</li>
 * </ul>
 *
 * <p><b>보안 메모</b></p>
 * <ul>
 *   <li>Naver의 state는 CSRF 방지를 위해 검증이 권장된다. 현재 구현은 존재 여부만 검사한다.</li>
 *   <li>Access Token은 외부로 로그/노출하지 않도록 주의한다.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class RestTemplateSocialOAuthClient implements SocialOAuthClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SocialOAuthProperties props;

    /**
     * Access Token으로 소셜 프로필을 조회한다.
     *
     * <p>
     * provider에 따라 Google/Kakao/Naver의 프로필 조회 엔드포인트를 호출한다.
     * </p>
     *
     * @param provider    소셜 공급자
     * @param accessToken OAuth Access Token
     * @return 표준화된 소셜 프로필
     * @throws BusinessException 입력값이 잘못된 경우({@link ErrorCode#INVALID_INPUT_VALUE}),
     *                           토큰이 유효하지 않거나 외부 호출 실패 시({@link ErrorCode#INVALID_TOKEN})
     */
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

    /**
     * Authorization Code를 Access Token으로 교환한 후 소셜 프로필을 조회한다.
     *
     * @param provider    소셜 공급자
     * @param code        Authorization Code
     * @param redirectUri 등록된 Redirect URI
     * @param state       state (provider별 정책에 따라 필수/권장)
     * @return 표준화된 소셜 프로필
     */
    @Override
    public SocialProfile fetchProfileByCode(SocialProvider provider, String code, String redirectUri, String state) {
        if (provider == null || code == null || code.isBlank() || redirectUri == null || redirectUri.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String accessToken = exchangeAccessToken(provider, code, redirectUri, state);
        return fetchProfile(provider, accessToken); // 기존 로직 재사용
    }

    /**
     * Google userinfo 엔드포인트에서 프로필을 조회한다.
     *
     * @param accessToken OAuth Access Token
     * @return Google 프로필을 표준화한 결과
     * @throws Exception JSON 파싱 실패 등
     */
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

    /**
     * Kakao me 엔드포인트에서 프로필을 조회한다.
     *
     * @param accessToken OAuth Access Token
     * @return Kakao 프로필을 표준화한 결과
     * @throws Exception JSON 파싱 실패 등
     */
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

    /**
     * Naver me 엔드포인트에서 프로필을 조회한다.
     *
     * @param accessToken OAuth Access Token
     * @return Naver 프로필을 표준화한 결과
     * @throws Exception JSON 파싱 실패 등
     */
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

    /**
     * Bearer 토큰으로 GET 요청을 수행하고 JSON 응답을 {@link JsonNode}로 파싱한다.
     *
     * @param url         호출 URL
     * @param accessToken OAuth Access Token
     * @return 응답 JSON 루트 노드
     * @throws Exception JSON 파싱 실패 등
     * @throws BusinessException 2xx가 아니거나 body가 없으면 {@link ErrorCode#INVALID_TOKEN}
     */
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

    /**
     * JSON 노드에서 필드를 문자열로 추출한다(없으면 null).
     *
     * @param node  대상 노드
     * @param field 필드명
     * @return 텍스트 값 또는 null
     */
    private String text(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    /**
     * JSON 노드에서 필드를 문자열로 추출한다.
     * node가 null/누락이면 null을 반환한다.
     *
     * @param node  대상 노드
     * @param field 필드명
     * @return 텍스트 값 또는 null
     */
    private String textOrNull(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) return null;
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    /**
     * 공급자별 토큰 교환(authorization_code → access_token)을 수행한다.
     *
     * @param provider    소셜 공급자
     * @param code        Authorization Code
     * @param redirectUri Redirect URI
     * @param state       Naver 등에서 사용하는 state
     * @return Access Token
     */
    private String exchangeAccessToken(SocialProvider provider, String code, String redirectUri, String state) {
        return switch (provider) {
            case GOOGLE -> exchangeGoogle(code, redirectUri);
            case KAKAO -> exchangeKakao(code, redirectUri);
            case NAVER -> exchangeNaver(code, state);
        };
    }

    /**
     * Google 토큰 교환 엔드포인트로 Access Token을 발급받는다.
     *
     * @param code        Authorization Code
     * @param redirectUri Redirect URI
     * @return Access Token
     */
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

    /**
     * Kakao 토큰 교환 엔드포인트로 Access Token을 발급받는다.
     *
     * <p>
     * Kakao는 client_secret 설정이 ON인 경우 반드시 함께 전송해야 한다.
     * </p>
     *
     * @param code        Authorization Code
     * @param redirectUri Redirect URI
     * @return Access Token
     */
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

    /**
     * Naver 토큰 교환 엔드포인트로 Access Token을 발급받는다.
     *
     * <p>
     * Naver는 state 사용이 권장되며, 실제 운영에서는 state 값을 세션/서명 등으로 검증하는 것이 좋다.
     * </p>
     *
     * @param code  Authorization Code
     * @param state state 값
     * @return Access Token
     */
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

    /**
     * 토큰 응답에서 access_token을 추출한다.
     *
     * @param resp 토큰 엔드포인트 응답
     * @return access_token
     * @throws BusinessException 응답이 비정상이거나 access_token이 없으면 {@link ErrorCode#INVALID_TOKEN}
     */
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
