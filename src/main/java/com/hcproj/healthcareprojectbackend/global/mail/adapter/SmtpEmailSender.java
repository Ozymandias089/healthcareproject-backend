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

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {
    private final JavaMailSender javaMailSender;
    private final MailFromProperties mailFromProperties;

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
            // 여기서는 인프라 예외로 래핑 추천 (프로젝트 예외 체계가 있으면 그걸로)
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}
