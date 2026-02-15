package com.devoops.notification.service;

import com.devoops.notification.dto.message.*;
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

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationConsumerService Tests")
class NotificationConsumerServiceTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationConsumerService consumerService;

    private UUID testUserId;
    private String testUserEmail;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserEmail = "test@example.com";
    }

    @Nested
    @DisplayName("handleReservationCreated")
    class HandleReservationCreatedTests {

        @Test
        @DisplayName("Should process reservation created message")
        void handleReservationCreated_ProcessesMessage() {
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

            // When
            consumerService.handleReservationCreated(message);

            // Then
            verify(notificationService).processNotification(message);
        }
    }

    @Nested
    @DisplayName("handleReservationCancelled")
    class HandleReservationCancelledTests {

        @Test
        @DisplayName("Should process reservation cancelled message")
        void handleReservationCancelled_ProcessesMessage() {
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

            // When
            consumerService.handleReservationCancelled(message);

            // Then
            verify(notificationService).processNotification(message);
        }
    }

    @Nested
    @DisplayName("handleHostRated")
    class HandleHostRatedTests {

        @Test
        @DisplayName("Should process host rated message")
        void handleHostRated_ProcessesMessage() {
            // Given
            HostRatedMessage message = HostRatedMessage.builder()
                    .userId(testUserId)
                    .userEmail(testUserEmail)
                    .guestName("Jane Smith")
                    .rating(5)
                    .comment("Amazing host!")
                    .build();

            // When
            consumerService.handleHostRated(message);

            // Then
            verify(notificationService).processNotification(message);
        }
    }

    @Nested
    @DisplayName("handleAccommodationRated")
    class HandleAccommodationRatedTests {

        @Test
        @DisplayName("Should process accommodation rated message")
        void handleAccommodationRated_ProcessesMessage() {
            // Given
            AccommodationRatedMessage message = AccommodationRatedMessage.builder()
                    .userId(testUserId)
                    .userEmail(testUserEmail)
                    .guestName("Jane Smith")
                    .accommodationName("Cozy Beach House")
                    .rating(4)
                    .comment("Great place!")
                    .build();

            // When
            consumerService.handleAccommodationRated(message);

            // Then
            verify(notificationService).processNotification(message);
        }
    }

    @Nested
    @DisplayName("handleReservationResponse")
    class HandleReservationResponseTests {

        @Test
        @DisplayName("Should process reservation response message")
        void handleReservationResponse_ProcessesMessage() {
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

            // When
            consumerService.handleReservationResponse(message);

            // Then
            verify(notificationService).processNotification(message);
        }
    }
}
