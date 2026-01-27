package com.hcproj.healthcareprojectbackend.global.mail.port;

/**
 * 이메일 전송을 위한 포트 인터페이스.
 *
 * <p>
 * <b>역할</b>
 * <ul>
 *   <li>도메인/서비스 레이어에서 이메일 전송 기능을 추상화</li>
 *   <li>구현체(SMTP, 외부 메일 API 등)에 대한 의존성 제거</li>
 * </ul>
 *
 * <p>
 * <b>설계 의도</b>
 * <ul>
 *   <li>포트-어댑터(헥사고날) 구조를 통해 인프라 교체 용이</li>
 *   <li>테스트 시 Mock 구현체로 대체 가능</li>
 * </ul>
 */
public interface EmailSender {

    /**
     * 이메일을 전송한다.
     *
     * @param to       수신자 이메일 주소
     * @param subject  메일 제목
     * @param textBody 메일 본문 (Plain Text)
     */
    void send(String to, String subject, String textBody);
}
