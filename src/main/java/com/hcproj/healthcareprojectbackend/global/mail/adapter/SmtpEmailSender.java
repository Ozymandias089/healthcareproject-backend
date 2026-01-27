package com.hcproj.healthcareprojectbackend.global.mail.adapter;

import com.hcproj.healthcareprojectbackend.global.config.properties.MailFromProperties;
import com.hcproj.healthcareprojectbackend.global.mail.port.EmailSender;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * SMTP 기반 이메일 전송 어댑터 구현체.
 *
 * <p>
 * {@link EmailSender} 포트를 구현하여
 * JavaMailSender(SMTP)를 통해 실제 이메일을 전송한다.
 *
 * <p>
 * <b>사용처</b>
 * <ul>
 *   <li>이메일 인증</li>
 *   <li>비밀번호 재설정</li>
 *   <li>시스템 알림 메일</li>
 * </ul>
 *
 * <p>
 * <b>설계 특징</b>
 * <ul>
 *   <li>발신자 정보는 {@link MailFromProperties}를 통해 외부 설정으로 분리</li>
 *   <li>메일 전송 실패 시 인프라 레벨 예외로 처리</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {
    private final JavaMailSender javaMailSender;
    private final MailFromProperties mailFromProperties;

    /**
     * SMTP를 통해 이메일을 전송한다.
     *
     * @param to       수신자 이메일 주소
     * @param subject  메일 제목
     * @param textBody 메일 본문 (Plain Text)
     * @throws IllegalStateException 메일 전송 실패 시
     */
    @Override
    public void send(String to, String subject, String textBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textBody, false);

            // name 포함 From 처리
            InternetAddress from = new InternetAddress(
                    mailFromProperties.getFromAddress(),
                    mailFromProperties.getFromName(),
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(from);

            javaMailSender.send(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            /*
              인프라 계층 예외:
              - 상위 서비스에서 BusinessException 등으로 변환 가능
              - 메일 서버 장애, 설정 오류 등을 포함
             */
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}
