package com.hcproj.healthcareprojectbackend.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 애플리케이션 전역에서 사용하는 문자열 관련 유틸리티 클래스.
 *
 * <p>
 * <b>역할</b>
 * <ul>
 *   <li>null / 공백 문자열 처리 정책을 일관되게 유지</li>
 *   <li>입력값 정규화(normalization) 로직을 중앙화</li>
 * </ul>
 *
 * <p>
 * <b>설계 원칙</b>
 * <ul>
 *   <li>상태를 가지지 않는 순수 유틸 클래스</li>
 *   <li>상속 및 인스턴스화 방지를 위해 {@code final} 클래스 + static 메서드만 제공</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UtilityProvider {

    /**
     * 문자열을 trim 한 뒤, 빈 문자열이면 {@code null}로 정규화한다.
     *
     * <p>
     * 주로 DTO → 도메인 변환 시
     * "빈 문자열"과 "값 없음(null)"을 동일하게 취급하기 위해 사용된다.
     *
     * @param s 입력 문자열
     * @return trim 결과가 비어 있으면 null, 그렇지 않으면 trim 된 문자열
     */
    public static String normalizeNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * 이메일 문자열을 정규화한다.
     *
     * <p>
     * <ul>
     *   <li>앞뒤 공백 제거</li>
     *   <li>소문자 변환</li>
     * </ul>
     *
     * <p>
     * 이메일 비교, 중복 체크, 저장 시 일관성을 유지하기 위해 사용된다.
     *
     * @param email 입력 이메일
     * @return 정규화된 이메일 (null 입력 시 null)
     */
    public static String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    // ============================================================
    // Private Helper Methods
    // ============================================================

    public static String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

        public static String normalizeBodyPart(String bodyPart) {
        if (bodyPart == null) return null;
        String trimmed = bodyPart.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
