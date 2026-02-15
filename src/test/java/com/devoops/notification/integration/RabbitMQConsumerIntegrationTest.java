package com.devoops.notification.integration;

import com.devoops.notification.dto.message.*;
import com.devoops.notification.entity.NotificationPreferences;
import com.devoops.notification.util.TestDataFactory;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("RabbitMQ Consumer Integration Tests")
class RabbitMQConsumerIntegrationTest extends BaseIntegrationTest {

    private static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    @Nested
    @DisplayName("User Created Consumer")
    class UserCreatedConsumer {

        @Test
        @DisplayName("Should initialize preferences when user created message received")
        void shouldInitializePreferencesWhenUserCreated() {
            UserCreatedMessage message = TestDataFactory.createUserCreatedMessage();

            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "user.created", message);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(preferencesRepository.findByUserId(TestDataFactory.DEFAULT_USER_ID))
                        .isPresent()
                        .hasValueSatisfying(prefs -> {
                            assertThat(prefs.getUserEmail()).isEqualTo(TestDataFactory.DEFAULT_USER_EMAIL);
                            assertThat(prefs.getPreferences().isReservationRequestCreated()).isTrue();
                            assertThat(prefs.getPreferences().isReservationCancelled()).isTrue();
                            assertThat(prefs.getPreferences().isHostRated()).isTrue();
                            assertThat(prefs.getPreferences().isAccommodationRated()).isTrue();
                            assertThat(prefs.getPreferences().isReservationResponse()).isTrue();
                        });
            });
        }

        @Test
        @DisplayName("Should not duplicate preferences on re-send")
        void shouldNotDuplicatePreferencesOnResend() {
            UserCreatedMessage message = TestDataFactory.createUserCreatedMessage();

            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "user.created", message);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(preferencesRepository.findByUserId(TestDataFactory.DEFAULT_USER_ID)).isPresent()
            );

            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "user.created", message);

            await().pollDelay(1, TimeUnit.SECONDS).atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
                long count = preferencesRepository.findAll().stream()
                        .filter(p -> p.getUserId().equals(TestDataFactory.DEFAULT_USER_ID))
                        .count();
                assertThat(count).isEqualTo(1);
            });
        }
    }

    @Nested
    @DisplayName("Reservation Request Created Consumer")
    class ReservationRequestCreatedConsumer {

        @Test
        @DisplayName("Should send email when notification enabled")
        void shouldSendEmailWhenNotificationEnabled() {
            preferencesRepository.save(TestDataFactory.createDefaultPreferences());

            ReservationRequestCreatedMessage message = TestDataFactory.createReservationRequestCreatedMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.created", message);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(1);
                assertThat(receivedMessages[0].getSubject())
                        .contains("New Reservation Request");
                assertThat(receivedMessages[0].getAllRecipients()[0].toString())
                        .isEqualTo(TestDataFactory.DEFAULT_USER_EMAIL);
            });
        }

        @Test
        @DisplayName("Should not send email when notification disabled")
        void shouldNotSendEmailWhenNotificationDisabled() {
            NotificationPreferences prefs = TestDataFactory.createPreferencesWithAllDisabled(
                    TestDataFactory.DEFAULT_USER_ID,
                    TestDataFactory.DEFAULT_USER_EMAIL
            );
            preferencesRepository.save(prefs);

            ReservationRequestCreatedMessage message = TestDataFactory.createReservationRequestCreatedMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.created", message);

            await().pollDelay(1, TimeUnit.SECONDS).atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(getGreenMail().getReceivedMessages()).isEmpty()
            );
        }

        @Test
        @DisplayName("Should create preferences if missing and send email")
        void shouldCreatePreferencesIfMissingAndSendEmail() {
            ReservationRequestCreatedMessage message = TestDataFactory.createReservationRequestCreatedMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.created", message);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(preferencesRepository.findByUserId(TestDataFactory.DEFAULT_USER_ID)).isPresent();
                assertThat(getGreenMail().getReceivedMessages()).hasSize(1);
            });
        }
    }

    @Nested
    @DisplayName("Reservation Cancelled Consumer")
    class ReservationCancelledConsumer {

        @Test
        @DisplayName("Should send cancellation email when enabled")
        void shouldSendCancellationEmailWhenEnabled() {
            preferencesRepository.save(TestDataFactory.createDefaultPreferences());

            ReservationCancelledMessage message = TestDataFactory.createReservationCancelledMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.cancelled", message);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(1);
                assertThat(receivedMessages[0].getSubject())
                        .contains("Reservation Cancelled");
            });
        }

        @Test
        @DisplayName("Should not send email when disabled")
        void shouldNotSendEmailWhenDisabled() {
            NotificationPreferences prefs = TestDataFactory.createDefaultPreferences();
            prefs.getPreferences().setReservationCancelled(false);
            preferencesRepository.save(prefs);

            ReservationCancelledMessage message = TestDataFactory.createReservationCancelledMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.cancelled", message);

            await().pollDelay(1, TimeUnit.SECONDS).atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(getGreenMail().getReceivedMessages()).isEmpty()
            );
        }
    }

    @Nested
    @DisplayName("Host Rated Consumer")
    class HostRatedConsumer {

        @Test
        @DisplayName("Should send host rating notification email")
        void shouldSendHostRatingNotificationEmail() {
            preferencesRepository.save(TestDataFactory.createDefaultPreferences());

            HostRatedMessage message = TestDataFactory.createHostRatedMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.rating.host", message);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(1);
                assertThat(receivedMessages[0].getSubject())
                        .contains("Rating");
            });
        }

        @Test
        @DisplayName("Should not send email when host rated notification disabled")
        void shouldNotSendEmailWhenHostRatedDisabled() {
            NotificationPreferences prefs = TestDataFactory.createDefaultPreferences();
            prefs.getPreferences().setHostRated(false);
            preferencesRepository.save(prefs);

            HostRatedMessage message = TestDataFactory.createHostRatedMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.rating.host", message);

            await().pollDelay(1, TimeUnit.SECONDS).atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(getGreenMail().getReceivedMessages()).isEmpty()
            );
        }
    }

    @Nested
    @DisplayName("Accommodation Rated Consumer")
    class AccommodationRatedConsumer {

        @Test
        @DisplayName("Should send accommodation rating notification email")
        void shouldSendAccommodationRatingNotificationEmail() {
            preferencesRepository.save(TestDataFactory.createDefaultPreferences());

            AccommodationRatedMessage message = TestDataFactory.createAccommodationRatedMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.rating.accommodation", message);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(1);
                assertThat(receivedMessages[0].getSubject())
                        .contains("Review");
            });
        }

        @Test
        @DisplayName("Should not send email when accommodation rated notification disabled")
        void shouldNotSendEmailWhenAccommodationRatedDisabled() {
            NotificationPreferences prefs = TestDataFactory.createDefaultPreferences();
            prefs.getPreferences().setAccommodationRated(false);
            preferencesRepository.save(prefs);

            AccommodationRatedMessage message = TestDataFactory.createAccommodationRatedMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.rating.accommodation", message);

            await().pollDelay(1, TimeUnit.SECONDS).atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(getGreenMail().getReceivedMessages()).isEmpty()
            );
        }
    }

    @Nested
    @DisplayName("Reservation Response Consumer")
    class ReservationResponseConsumer {

        @Test
        @DisplayName("Should send approval email for approved reservation")
        void shouldSendApprovalEmailForApprovedReservation() {
            preferencesRepository.save(TestDataFactory.createDefaultPreferences());

            ReservationResponseMessage message = TestDataFactory.createApprovedReservationResponseMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.response", message);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(1);
                assertThat(receivedMessages[0].getSubject())
                        .contains("Confirmed");
            });
        }

        @Test
        @DisplayName("Should send decline email for declined reservation")
        void shouldSendDeclineEmailForDeclinedReservation() {
            preferencesRepository.save(TestDataFactory.createDefaultPreferences());

            ReservationResponseMessage message = TestDataFactory.createDeclinedReservationResponseMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.response", message);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(1);
                assertThat(receivedMessages[0].getSubject())
                        .contains("Update");
            });
        }

        @Test
        @DisplayName("Should not send email when reservation response notification disabled")
        void shouldNotSendEmailWhenReservationResponseDisabled() {
            NotificationPreferences prefs = TestDataFactory.createDefaultPreferences();
            prefs.getPreferences().setReservationResponse(false);
            preferencesRepository.save(prefs);

            ReservationResponseMessage message = TestDataFactory.createApprovedReservationResponseMessage();
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.response", message);

            await().pollDelay(1, TimeUnit.SECONDS).atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
                    assertThat(getGreenMail().getReceivedMessages()).isEmpty()
            );
        }
    }

    @Nested
    @DisplayName("Multiple Users")
    class MultipleUsers {

        @Test
        @DisplayName("Should handle notifications for different users independently")
        void shouldHandleNotificationsForDifferentUsersIndependently() {
            UUID user1Id = UUID.randomUUID();
            UUID user2Id = UUID.randomUUID();
            String user1Email = "user1@example.com";
            String user2Email = "user2@example.com";

            NotificationPreferences user1Prefs = TestDataFactory.createPreferences(user1Id, user1Email);
            user1Prefs.getPreferences().setReservationRequestCreated(true);

            NotificationPreferences user2Prefs = TestDataFactory.createPreferences(user2Id, user2Email);
            user2Prefs.getPreferences().setReservationRequestCreated(false);

            preferencesRepository.save(user1Prefs);
            preferencesRepository.save(user2Prefs);

            ReservationRequestCreatedMessage message1 = TestDataFactory.createReservationRequestCreatedMessage(
                    user1Id, user1Email
            );
            ReservationRequestCreatedMessage message2 = TestDataFactory.createReservationRequestCreatedMessage(
                    user2Id, user2Email
            );

            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.created", message1);
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, "notification.reservation.created", message2);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                MimeMessage[] receivedMessages = getGreenMail().getReceivedMessages();
                assertThat(receivedMessages).hasSize(1);
                assertThat(receivedMessages[0].getAllRecipients()[0].toString())
                        .isEqualTo(user1Email);
            });
        }
    }
}
