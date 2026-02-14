package com.devoops.notification.service;

import com.devoops.notification.dto.message.*;
import com.devoops.notification.entity.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.from-name}")
    private String fromName;

    @Value("${notification.frontend.url}")
    private String frontendUrl;

    public void sendNotificationEmail(NotificationMessage message) {
        try {
            String subject = message.getSubject();
            String templateName = "email/" + message.getType().getTemplateName();

            Context context = createContext(message);
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(message.getUserEmail());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to {} for notification type {}",
                    message.getUserEmail(), message.getType());

        } catch (MessagingException e) {
            log.error("Failed to send email to {} for notification type {}: {}",
                    message.getUserEmail(), message.getType(), e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", message.getUserEmail(), e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private Context createContext(NotificationMessage message) {
        Context context = new Context();
        context.setVariable("frontendUrl", frontendUrl);
        context.setVariable("subject", message.getSubject());

        switch (message) {
            case ReservationRequestCreatedMessage msg -> {
                context.setVariable("guestName", msg.getGuestName());
                context.setVariable("accommodationName", msg.getAccommodationName());
                context.setVariable("checkIn", msg.getCheckIn());
                context.setVariable("checkOut", msg.getCheckOut());
                context.setVariable("totalPrice", msg.getTotalPrice());
            }
            case ReservationCancelledMessage msg -> {
                context.setVariable("guestName", msg.getGuestName());
                context.setVariable("accommodationName", msg.getAccommodationName());
                context.setVariable("checkIn", msg.getCheckIn());
                context.setVariable("checkOut", msg.getCheckOut());
                context.setVariable("reason", msg.getReason());
            }
            case HostRatedMessage msg -> {
                context.setVariable("guestName", msg.getGuestName());
                context.setVariable("rating", msg.getRating());
                context.setVariable("comment", msg.getComment());
            }
            case AccommodationRatedMessage msg -> {
                context.setVariable("guestName", msg.getGuestName());
                context.setVariable("accommodationName", msg.getAccommodationName());
                context.setVariable("rating", msg.getRating());
                context.setVariable("comment", msg.getComment());
            }
            case ReservationResponseMessage msg -> {
                context.setVariable("hostName", msg.getHostName());
                context.setVariable("accommodationName", msg.getAccommodationName());
                context.setVariable("status", msg.getStatus().name());
                context.setVariable("checkIn", msg.getCheckIn());
                context.setVariable("checkOut", msg.getCheckOut());
            }
            default -> throw new IllegalArgumentException("Unknown message type: " + message.getClass());
        }

        return context;
    }
}
