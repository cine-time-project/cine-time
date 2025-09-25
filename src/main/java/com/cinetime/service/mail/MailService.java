package com.cinetime.service.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    // application.properties
    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.reset.subject:CineTime - Password Reset Code}")
    private String resetSubject;

    // %s -> code
    @Value("${app.mail.reset.template:<p>Hello,</p><p>Your code is: <b>%s</b></p><p>Please use it within 10 minutes.</p>}")
    private String resetTemplate;

    /**
     * Password reset code send.
     */
    // MailService.java (güncelle)
    public void sendResetCode(String to, String code) {
        String subject = (resetSubject == null || resetSubject.isBlank())
                ? "CineTime - Password Reset Code"
                : resetSubject;

        String template = (resetTemplate == null || resetTemplate.isBlank())
                ? "<p>Hello,</p><p>Your code is: <b>%s</b></p><p>Please use it within 10 minutes.</p>"
                : resetTemplate;

        String html = String.format(template, code);
        String text = buildPlainText(code);
        sendHtml(to, subject, html, text);
    }


    /**
     *  HTML e-posta gönderimi (with plain-text fallback).
     */
    public void sendHtml(String to, String subject, String htmlBody, String plainTextBody) {
        try {
            String sender = (from == null || from.isBlank())
                    ? "no-reply@example.com"
                    : from;

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setFrom(sender);
            helper.setSubject(subject);
            helper.setText(
                    plainTextBody != null ? plainTextBody : stripHtml(htmlBody),
                    htmlBody
            );

            mailSender.send(mime);
            log.info("Mail sent to {} with subject '{}'", to, subject);
        } catch (Exception ex) {
            log.error("Failed to send mail to {}: {}", to, ex.getMessage(), ex);
            throw new RuntimeException("Error: Failed to send email.");
        }
    }


    private String buildPlainText(String code) {
        return "Hello,\n"
                + "Your code is: " + code + "\n"
                + "Please use it within 10 minutes.\n"
                + "— CineTime";
    }

    // Single HTML -> text fall-back
    private String stripHtml(String html) {
        return html == null ? "" : html.replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ")
                .trim();
    }
}
