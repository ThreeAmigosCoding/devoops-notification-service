package com.devoops.notification.integration;

import com.devoops.notification.dto.message.*;
import com.devoops.notification.dto.request.NotificationPreferencesUpdateRequest;
import com.devoops.notification.dto.response.NotificationPreferencesResponse;
import com.devoops.notification.entity.NotificationPreferences;
import com.devoops.notification.util.TestDataFactory;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("End-to-End Notification Flow Tests")
class EndToEndNotificationFlowTest extends BaseIntegrationTest {

    private static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    private static final String PREFERENCES_PATH = "/api/notification/preferences";

    @Nested
    @DisplayName("Complete User Flow")
    class CompleteUserFlow {

        @Test
        @DisplayName("User created -> Preferences initialized -> Reservation notification -> Email sent")
        void shouldCompleteUserCreationToNotificationFlow() {
            UUID userId = UUID.randomUUID();
            String userEmail = "newuser@example.com";

            UserCreatedMessage userCreatedMessage = UserCreatedMessage.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .build();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "user.created", userCreatedMessage);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(preferencesRepository.findByUserId(userId)).isPresent()
            );

            HttpHeaders headers = createHeaders(userId.toString(), "HOST");
            ResponseEntity<NotificationPreferencesResponse> response = get(
                    PREFERENCES_PATH,
                    headers,
                    NotificationPreferencesResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().userId()).isEqualTo(userId);
            assertThat(response.getBody().userEmail()).isEqualTo(userEmail);
            assertThat(response.getBody().reservationRequestCreated()).isTrue();

            ReservationRequestCreatedMessage reservationMessage = ReservationRequestCreatedMessage.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .guestName("Test Guest")
                    .accommodationName("Test Accommodation")
                    .checkIn(LocalDate.now().plusDays(7))
                    .checkOut(LocalDate.now().plusDays(14))
                    .totalPrice(new BigDecimal("500.00"))
                    .build();

            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.created", reservationMessage);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(1);
                assertThat(receivedMessages[0].getAllRecipients()[0].toString()).isEqualTo(userEmail);
            });
        }

        @Test
        @DisplayName("Update preferences via API -> Verify notifications respect new settings")
        void shouldRespectUpdatedPreferences() {
            UUID userId = UUID.randomUUID();
            String userEmail = "updatetest@example.com";

            preferencesRepository.save(TestDataFactory.createPreferences(userId, userEmail));

            NotificationPreferencesUpdateRequest updateRequest = new NotificationPreferencesUpdateRequest(
                    false, true, true, true, true
            );

            HttpHeaders headers = createHeaders(userId.toString(), "HOST");
            ResponseEntity<NotificationPreferencesResponse> updateResponse = put(
                    PREFERENCES_PATH,
                    updateRequest,
                    headers,
                    NotificationPreferencesResponse.class
            );

            assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updateResponse.getBody()).isNotNull();
            assertThat(updateResponse.getBody().reservationRequestCreated()).isFalse();

            ReservationRequestCreatedMessage message = TestDataFactory.createReservationRequestCreatedMessage(
                    userId, userEmail
            );

            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.created", message);

            await().pollDelay(1, TimeUnit.SECONDS).atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(getGreenMail().getReceivedMessages()).isEmpty()
            );

            ReservationCancelledMessage cancelMessage = TestDataFactory.createReservationCancelledMessage(
                    userId, userEmail
            );

            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.cancelled", cancelMessage);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(1);
                assertThat(receivedMessages[0].getSubject()).contains("Cancelled");
            });
        }
    }

    @Nested
    @DisplayName("Multiple Notifications for Same User")
    class MultipleNotificationsForSameUser {

        @Test
        @DisplayName("Should handle multiple notification types for same user")
        void shouldHandleMultipleNotificationTypesForSameUser() {
            UUID userId = UUID.randomUUID();
            String userEmail = "multinotif@example.com";

            preferencesRepository.save(TestDataFactory.createPreferences(userId, userEmail));

            ReservationRequestCreatedMessage reservationMsg = TestDataFactory.createReservationRequestCreatedMessage(
                    userId, userEmail
            );
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.created", reservationMsg);

            HostRatedMessage ratingMsg = TestDataFactory.createHostRatedMessage(userId, userEmail);
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.rating.host", ratingMsg);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(2);
            });
        }

        @Test
        @DisplayName("Should send multiple emails sequentially")
        void shouldSendMultipleEmailsSequentially() {
            UUID userId = UUID.randomUUID();
            String userEmail = "sequential@example.com";

            preferencesRepository.save(TestDataFactory.createPreferences(userId, userEmail));

            for (int i = 0; i < 3; i++) {
                ReservationRequestCreatedMessage message = ReservationRequestCreatedMessage.builder()
                        .userId(userId)
                        .userEmail(userEmail)
                        .guestName("Guest " + i)
                        .accommodationName("Accommodation " + i)
                        .checkIn(LocalDate.now().plusDays(7 + i))
                        .checkOut(LocalDate.now().plusDays(14 + i))
                        .totalPrice(new BigDecimal((100 * (i + 1)) + ".00"))
                        .build();

                rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.created", message);
            }

            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(3);
            });
        }
    }

    @Nested
    @DisplayName("Concurrent Users")
    class ConcurrentUsers {

        @Test
        @DisplayName("Should handle notifications for multiple concurrent users")
        void shouldHandleNotificationsForMultipleConcurrentUsers() {
            int numberOfUsers = 5;
            UUID[] userIds = new UUID[numberOfUsers];
            String[] userEmails = new String[numberOfUsers];

            for (int i = 0; i < numberOfUsers; i++) {
                userIds[i] = UUID.randomUUID();
                userEmails[i] = "user" + i + "@example.com";
                preferencesRepository.save(TestDataFactory.createPreferences(userIds[i], userEmails[i]));
            }

            for (int i = 0; i < numberOfUsers; i++) {
                ReservationRequestCreatedMessage message = TestDataFactory.createReservationRequestCreatedMessage(
                        userIds[i], userEmails[i]
                );
                rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.created", message);
            }

            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(numberOfUsers);
            });

            MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
            for (int i = 0; i < numberOfUsers; i++) {
                final String expectedEmail = userEmails[i];
                boolean found = false;
                for (MimeMessage msg : receivedMessages) {
                    try {
                        if (msg.getAllRecipients()[0].toString().equals(expectedEmail)) {
                            found = true;
                            break;
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
                assertThat(found).as("Email should be sent to " + expectedEmail).isTrue();
            }
        }

        @Test
        @DisplayName("Should handle mixed enabled/disabled preferences across users")
        void shouldHandleMixedPreferencesAcrossUsers() {
            UUID enabledUserId = UUID.randomUUID();
            UUID disabledUserId = UUID.randomUUID();
            String enabledUserEmail = "enabled@example.com";
            String disabledUserEmail = "disabled@example.com";

            preferencesRepository.save(TestDataFactory.createPreferences(enabledUserId, enabledUserEmail));

            NotificationPreferences disabledPrefs = TestDataFactory.createPreferencesWithAllDisabled(
                    disabledUserId, disabledUserEmail
            );
            preferencesRepository.save(disabledPrefs);

            rabbitTemplate.convertAndSend(
                    NOTIFICATION_EXCHANGE,
                    "notification.reservation.created",
                    TestDataFactory.createReservationRequestCreatedMessage(enabledUserId, enabledUserEmail)
            );

            rabbitTemplate.convertAndSend(
                    NOTIFICATION_EXCHANGE,
                    "notification.reservation.created",
                    TestDataFactory.createReservationRequestCreatedMessage(disabledUserId, disabledUserEmail)
            );

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(1);
                assertThat(receivedMessages[0].getAllRecipients()[0].toString()).isEqualTo(enabledUserEmail);
            });
        }
    }

    @Nested
    @DisplayName("Notification Types Coverage")
    class NotificationTypesCoverage {

        @Test
        @DisplayName("Should process all notification types in sequence")
        void shouldProcessAllNotificationTypesInSequence() {
            UUID userId = UUID.randomUUID();
            String userEmail = "alltypes@example.com";

            preferencesRepository.save(TestDataFactory.createPreferences(userId, userEmail));

            rabbitTemplate.convertAndSend(
                    NOTIFICATION_EXCHANGE,
                    "notification.reservation.created",
                    TestDataFactory.createReservationRequestCreatedMessage(userId, userEmail)
            );

            rabbitTemplate.convertAndSend(
                    NOTIFICATION_EXCHANGE,
                    "notification.reservation.cancelled",
                    TestDataFactory.createReservationCancelledMessage(userId, userEmail)
            );

            rabbitTemplate.convertAndSend(
                    NOTIFICATION_EXCHANGE,
                    "notification.rating.host",
                    TestDataFactory.createHostRatedMessage(userId, userEmail)
            );

            rabbitTemplate.convertAndSend(
                    NOTIFICATION_EXCHANGE,
                    "notification.rating.accommodation",
                    TestDataFactory.createAccommodationRatedMessage(userId, userEmail)
            );

            rabbitTemplate.convertAndSend(
                    NOTIFICATION_EXCHANGE,
                    "notification.reservation.response",
                    TestDataFactory.createReservationResponseMessage(
                            userId, userEmail, ReservationResponseMessage.ReservationStatus.APPROVED
                    )
            );

            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(5);
            });
        }
    }

    @Nested
    @DisplayName("Preferences Initialization and Update Flow")
    class PreferencesInitializationAndUpdateFlow {

        @Test
        @DisplayName("Should initialize preferences and allow immediate update")
        void shouldInitializePreferencesAndAllowImmediateUpdate() {
            UUID userId = UUID.randomUUID();
            String userEmail = "initupdate@example.com";

            UserCreatedMessage userCreatedMessage = UserCreatedMessage.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .build();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "user.created", userCreatedMessage);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(preferencesRepository.findByUserId(userId)).isPresent()
            );

            NotificationPreferencesUpdateRequest updateRequest = new NotificationPreferencesUpdateRequest(
                    false, false, false, false, false
            );

            HttpHeaders headers = createHeaders(userId.toString(), "GUEST");
            ResponseEntity<NotificationPreferencesResponse> response = put(
                    PREFERENCES_PATH,
                    updateRequest,
                    headers,
                    NotificationPreferencesResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().reservationRequestCreated()).isFalse();
            assertThat(response.getBody().reservationResponse()).isFalse();

            NotificationPreferences updated = preferencesRepository.findByUserId(userId).orElseThrow();
            assertThat(updated.getPreferences().isReservationRequestCreated()).isFalse();
            assertThat(updated.getPreferences().isReservationCancelled()).isFalse();
            assertThat(updated.getPreferences().isHostRated()).isFalse();
            assertThat(updated.getPreferences().isAccommodationRated()).isFalse();
            assertThat(updated.getPreferences().isReservationResponse()).isFalse();
        }
    }
}
