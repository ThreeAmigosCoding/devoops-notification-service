package com.devoops.notification.service;

import com.devoops.notification.dto.message.ReservationRequestCreatedMessage;
import com.devoops.notification.entity.NotificationPreferences;
import com.devoops.notification.entity.NotificationType;
import com.devoops.notification.entity.Preferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationPreferencesService preferencesService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService service;

    private UUID testUserId;
    private String testUserEmail;
    private NotificationPreferences testPreferences;
    private ReservationRequestCreatedMessage testMessage;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserEmail = "test@example.com";

        Preferences prefs = new Preferences();
        prefs.setReservationRequestCreated(true);
        prefs.setReservationCancelled(true);
        prefs.setHostRated(true);
        prefs.setAccommodationRated(true);
        prefs.setReservationResponse(true);

        testPreferences = new NotificationPreferences(testUserId, testUserEmail);
        testPreferences.setPreferences(prefs);

        testMessage = ReservationRequestCreatedMessage.builder()
                .userId(testUserId)
                .userEmail(testUserEmail)
                .guestName("John Doe")
                .accommodationName("Test Accommodation")
                .checkIn(LocalDate.of(2024, 3, 15))
                .checkOut(LocalDate.of(2024, 3, 20))
                .totalPrice(new BigDecimal("450.00"))
                .build();
    }

    @Nested
    @DisplayName("processNotification")
    class ProcessNotificationTests {

        @Test
        @DisplayName("Should send email when notification type is enabled")
        void processNotification_WhenEnabled_SendsEmail() {
            // Given
            when(preferencesService.getOrCreatePreferences(testUserId, testUserEmail))
                    .thenReturn(testPreferences);

            // When
            service.processNotification(testMessage);

            // Then
            verify(preferencesService).getOrCreatePreferences(testUserId, testUserEmail);
            verify(emailService).sendNotificationEmail(testMessage);
        }

        @Test
        @DisplayName("Should not send email when notification type is disabled")
        void processNotification_WhenDisabled_DoesNotSendEmail() {
            // Given
            testPreferences.getPreferences().setReservationRequestCreated(false);
            when(preferencesService.getOrCreatePreferences(testUserId, testUserEmail))
                    .thenReturn(testPreferences);

            // When
            service.processNotification(testMessage);

            // Then
            verify(preferencesService).getOrCreatePreferences(testUserId, testUserEmail);
            verify(emailService, never()).sendNotificationEmail(any());
        }

        @Test
        @DisplayName("Should create preferences if they don't exist")
        void processNotification_WhenNoPreferences_CreatesPreferences() {
            // Given
            when(preferencesService.getOrCreatePreferences(testUserId, testUserEmail))
                    .thenReturn(testPreferences);

            // When
            service.processNotification(testMessage);

            // Then
            verify(preferencesService).getOrCreatePreferences(testUserId, testUserEmail);
        }
    }
}
