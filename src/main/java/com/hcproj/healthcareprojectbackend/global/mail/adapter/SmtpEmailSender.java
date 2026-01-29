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
    public void sendText(String to, String subject, String textBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_NO,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textBody, false);
            helper.setFrom(buildFrom());

            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send TEXT email. to={}, subject={}", to, subject, e);
            throw new IllegalStateException("Failed to send email", e);
        }
    }

    @Override
    public void sendHtml(String to, String subject, String htmlBody) {
        // 텍스트 fallback 없이 HTML만 보내는 버전
        sendHtml(to, subject, stripToTextFallback(htmlBody), htmlBody);
    }

    @Override
    public void sendHtml(String to, String subject, String textFallback, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);

            // 핵심: text + html 같이
            helper.setText(textFallback, htmlBody);

            helper.setFrom(buildFrom());

            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send HTML email. to={}, subject={}", to, subject, e);
            throw new IllegalStateException("Failed to send email", e);
        }
    }

    private InternetAddress buildFrom() {
        try {
            return new InternetAddress(
                    mailFromProperties.getFromAddress(),
                    mailFromProperties.getFromName(),
                    StandardCharsets.UTF_8.name()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Invalid from address/name", e);
        }
    }

    /**
     * HTML에서 대충 텍스트 fallback을 뽑는 간단 버전.
     * (완벽할 필요 없음. “링크/코드”가 포함되면 충분)
     */
    private String stripToTextFallback(String html) {
        if (html == null) return "";
        return html
                .replaceAll("(?is)<style.*?>.*?</style>", "")
                .replaceAll("(?is)<script.*?>.*?</script>", "")
                .replaceAll("(?is)<br\\s*/?>", "\n")
                .replaceAll("(?is)</p>", "\n\n")
                .replaceAll("(?is)<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .trim();
    }
}