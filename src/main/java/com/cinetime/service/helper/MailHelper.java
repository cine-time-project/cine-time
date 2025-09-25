package com.cinetime.service.helper;

import com.cinetime.payload.messages.ErrorMessages;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailHelper {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")           private String mailFrom;
    @Value("${app.mail.reset.subject}")  private String resetSubject;
    @Value("${app.mail.reset.template}") private String resetTemplateHtml;

    public void sendResetCodeEmail(String to, String code) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(resetSubject);
            helper.setText(String.format(resetTemplateHtml, code), true); // HTML body
            mailSender.send(mime);
        } catch (Exception e) {
            throw new IllegalStateException(ErrorMessages.EMAIL_SENDING_FAILED);
        }
    }
}
