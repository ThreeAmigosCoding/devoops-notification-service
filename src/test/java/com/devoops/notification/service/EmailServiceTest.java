package com.devoops.notification.service;

import com.devoops.notification.dto.message.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private UUID testUserId;
    private String testUserEmail;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserEmail = "test@example.com";

        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@devoops.com");
        ReflectionTestUtils.setField(emailService, "fromName", "DevOops");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");
    }

    @Nested
    @DisplayName("sendNotificationEmail")
    class SendNotificationEmailTests {

        @Test
        @DisplayName("Should send reservation request created email")
        void sendNotificationEmail_ReservationRequestCreated_SendsEmail() {
            // Given
            ReservationRequestCreatedMessage message = ReservationRequestCreatedMessage.builder()
                    .userId(testUserId)
                    .userEmail(testUserEmail)
                    .guestName("John Doe")
                    .accommodationName("Cozy Beach House")
                    .checkIn(LocalDate.of(2024, 3, 15))
                    .checkOut(LocalDate.of(2024, 3, 20))
                    .totalPrice(new BigDecimal("450.00"))
                    .build();

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email/reservation-request-created"), any(Context.class)))
                    .thenReturn("<html>Test Email</html>");

            // When
            emailService.sendNotificationEmail(message);

            // Then
            verify(mailSender).createMimeMessage();
            verify(templateEngine).process(eq("email/reservation-request-created"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send reservation cancelled email")
        void sendNotificationEmail_ReservationCancelled_SendsEmail() {
            // Given
            ReservationCancelledMessage message = ReservationCancelledMessage.builder()
                    .userId(testUserId)
                    .userEmail(testUserEmail)
                    .guestName("John Doe")
                    .accommodationName("Cozy Beach House")
                    .checkIn(LocalDate.of(2024, 3, 15))
                    .checkOut(LocalDate.of(2024, 3, 20))
                    .reason("Guest cancelled")
                    .build();

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email/reservation-cancelled"), any(Context.class)))
                    .thenReturn("<html>Test Email</html>");

            // When
            emailService.sendNotificationEmail(message);

            // Then
            verify(templateEngine).process(eq("email/reservation-cancelled"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send host rated email")
        void sendNotificationEmail_HostRated_SendsEmail() {
            // Given
            HostRatedMessage message = HostRatedMessage.builder()
                    .userId(testUserId)
                    .userEmail(testUserEmail)
                    .guestName("Jane Smith")
                    .rating(5)
                    .comment("Amazing host!")
                    .build();

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email/host-rated"), any(Context.class)))
                    .thenReturn("<html>Test Email</html>");

            // When
            emailService.sendNotificationEmail(message);

            // Then
            verify(templateEngine).process(eq("email/host-rated"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send accommodation rated email")
        void sendNotificationEmail_AccommodationRated_SendsEmail() {
            // Given
            AccommodationRatedMessage message = AccommodationRatedMessage.builder()
                    .userId(testUserId)
                    .userEmail(testUserEmail)
                    .guestName("Jane Smith")
                    .accommodationName("Cozy Beach House")
                    .rating(4)
                    .comment("Great place!")
                    .build();

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email/accommodation-rated"), any(Context.class)))
                    .thenReturn("<html>Test Email</html>");

            // When
            emailService.sendNotificationEmail(message);

            // Then
            verify(templateEngine).process(eq("email/accommodation-rated"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send reservation response email")
        void sendNotificationEmail_ReservationResponse_SendsEmail() {
            // Given
            ReservationResponseMessage message = ReservationResponseMessage.builder()
                    .userId(testUserId)
                    .userEmail(testUserEmail)
                    .hostName("Mike Johnson")
                    .accommodationName("Cozy Beach House")
                    .status(ReservationResponseMessage.ReservationStatus.APPROVED)
                    .checkIn(LocalDate.of(2024, 3, 15))
                    .checkOut(LocalDate.of(2024, 3, 20))
                    .build();

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email/reservation-response"), any(Context.class)))
                    .thenReturn("<html>Test Email</html>");

            // When
            emailService.sendNotificationEmail(message);

            // Then
            verify(templateEngine).process(eq("email/reservation-response"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should include correct template variables in context")
        void sendNotificationEmail_SetsCorrectTemplateVariables() {
            // Given
            ReservationRequestCreatedMessage message = ReservationRequestCreatedMessage.builder()
                    .userId(testUserId)
                    .userEmail(testUserEmail)
                    .guestName("John Doe")
                    .accommodationName("Cozy Beach House")
                    .checkIn(LocalDate.of(2024, 3, 15))
                    .checkOut(LocalDate.of(2024, 3, 20))
                    .totalPrice(new BigDecimal("450.00"))
                    .build();

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
            when(templateEngine.process(eq("email/reservation-request-created"), contextCaptor.capture()))
                    .thenReturn("<html>Test Email</html>");

            // When
            emailService.sendNotificationEmail(message);

            // Then
            Context capturedContext = contextCaptor.getValue();
            assertThat(capturedContext.getVariable("guestName")).isEqualTo("John Doe");
            assertThat(capturedContext.getVariable("accommodationName")).isEqualTo("Cozy Beach House");
            assertThat(capturedContext.getVariable("frontendUrl")).isEqualTo("http://localhost:4200");
        }

        @Test
        @DisplayName("Should throw exception when mail sending fails")
        void sendNotificationEmail_WhenMailFails_ThrowsException() {
            // Given
            ReservationRequestCreatedMessage message = ReservationRequestCreatedMessage.builder()
                    .userId(testUserId)
                    .userEmail(testUserEmail)
                    .guestName("John Doe")
                    .accommodationName("Cozy Beach House")
                    .checkIn(LocalDate.of(2024, 3, 15))
                    .checkOut(LocalDate.of(2024, 3, 20))
                    .totalPrice(new BigDecimal("450.00"))
                    .build();

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(anyString(), any(Context.class)))
                    .thenReturn("<html>Test Email</html>");
            doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

            // When/Then
            assertThatThrownBy(() -> emailService.sendNotificationEmail(message))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
