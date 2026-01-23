package com.hcproj.healthcareprojectbackend.global.mail.port;

public interface EmailSender {
    void send(String to, String subject, String textBody);
}
