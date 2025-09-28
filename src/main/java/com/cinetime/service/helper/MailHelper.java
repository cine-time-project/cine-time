package com.cinetime.service.helper;

import com.cinetime.entity.business.Payment;
import com.cinetime.entity.business.Showtime;
import com.cinetime.entity.business.Ticket;
import com.cinetime.payload.messages.ErrorMessages;
import com.cinetime.service.mail.MailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailHelper {
private final MailService mailService;
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
            Showtime st = t.getShowtime();
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

        // sendHtml() already falls back to stripHtml(html) when plainTextBody is null
        mailService.sendHtml(to, subject, html.toString(), null);
    }
}
