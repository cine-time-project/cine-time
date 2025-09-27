package com.cinetime.service.mail;

import com.cinetime.entity.business.Payment;
import com.cinetime.entity.business.Ticket;
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
     * HTML e-posta gönderimi (with plain-text fallback).
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

    /**
     * Sends a purchase confirmation email with payment details and ticket list.
     */
    public void sendPurchaseReceipt(String to, Payment payment, java.util.List<Ticket> tickets) {
        String subject = "CineTime - Payment Confirmation #" + payment.getId();

        StringBuilder html = new StringBuilder();
        html.append("<h2>Thanks for your purchase!</h2>");
        html.append("<p><b>Payment ID:</b> ").append(payment.getId()).append("<br>")
                .append("<b>Status:</b> ").append(payment.getPaymentStatus()).append("<br>")
                .append("<b>Amount:</b> ").append(String.format("%.2f", payment.getAmount())).append(" ")
                .append(payment.getCurrency() == null ? "" : payment.getCurrency()).append("<br>");
        if (payment.getProviderReference() != null) {
            html.append("<b>Reference:</b> ").append(payment.getProviderReference()).append("<br>");
        }
        if (payment.getPaymentDate() != null) {
            html.append("<b>Date:</b> ").append(payment.getPaymentDate()).append("<br>");
        }
        html.append("</p>");

        html.append("<h3>Tickets</h3><ol>");
        for (Ticket t : tickets) {
            String seat = t.getSeatLetter() + t.getSeatNumber();
            var st = t.getShowtime();
            String when = (st != null && st.getDate() != null && st.getStartTime() != null)
                    ? st.getDate() + " " + st.getStartTime()
                    : "";
            String movie = (st != null && st.getMovie() != null) ? st.getMovie().getTitle() : "Movie";
            String hall  = (st != null && st.getHall() != null) ? st.getHall().getName() : "Hall";
            String cinema= (st != null && st.getHall() != null && st.getHall().getCinema() != null)
                    ? st.getHall().getCinema().getName() : "Cinema";

            html.append("<li><b>").append(movie).append("</b> — ")
                    .append(cinema).append(" / ").append(hall).append(" — ")
                    .append(when).append(" — Seat ").append(seat)
                    .append("</li>");
        }
        html.append("</ol>");

        String plain = stripHtml(html.toString());
        sendHtml(to, subject, html.toString(), plain);
    }


}

