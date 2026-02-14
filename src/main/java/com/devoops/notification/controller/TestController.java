package com.devoops.notification.controller;

import com.devoops.notification.dto.message.*;
import com.devoops.notification.entity.NotificationType;
import com.devoops.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/notification/test")
@RequiredArgsConstructor
public class TestController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<String> sendTestNotification(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "RESERVATION_REQUEST_CREATED") String type
    ) {
        UUID testUserId = UUID.randomUUID();
        NotificationType notificationType = NotificationType.valueOf(type);

        NotificationMessage message = createTestMessage(testUserId, email, notificationType);

        log.info("Sending test notification: type={}, email={}, userId={}", type, email, testUserId);
        notificationService.processNotification(message);

        return ResponseEntity.ok("Test notification sent to " + email + " (type: " + type + ")");
    }

    @GetMapping("/types")
    public ResponseEntity<NotificationType[]> getNotificationTypes() {
        return ResponseEntity.ok(NotificationType.values());
    }

    private NotificationMessage createTestMessage(UUID userId, String email, NotificationType type) {
        return switch (type) {
            case RESERVATION_REQUEST_CREATED -> ReservationRequestCreatedMessage.builder()
                    .userId(userId)
                    .userEmail(email)
                    .guestName("John Doe")
                    .accommodationName("Cozy Beach House")
                    .checkIn(LocalDate.of(2024, 3, 15))
                    .checkOut(LocalDate.of(2024, 3, 20))
                    .totalPrice(new BigDecimal("450.00"))
                    .build();

            case RESERVATION_CANCELLED -> ReservationCancelledMessage.builder()
                    .userId(userId)
                    .userEmail(email)
                    .guestName("John Doe")
                    .accommodationName("Cozy Beach House")
                    .checkIn(LocalDate.of(2024, 3, 15))
                    .checkOut(LocalDate.of(2024, 3, 20))
                    .reason("Guest cancelled due to schedule conflict")
                    .build();

            case HOST_RATED -> HostRatedMessage.builder()
                    .userId(userId)
                    .userEmail(email)
                    .guestName("Jane Smith")
                    .rating(5)
                    .comment("Amazing host! Very helpful and responsive.")
                    .build();

            case ACCOMMODATION_RATED -> AccommodationRatedMessage.builder()
                    .userId(userId)
                    .userEmail(email)
                    .guestName("Jane Smith")
                    .accommodationName("Cozy Beach House")
                    .rating(4)
                    .comment("Great location, clean and comfortable.")
                    .build();

            case RESERVATION_RESPONSE -> ReservationResponseMessage.builder()
                    .userId(userId)
                    .userEmail(email)
                    .hostName("Mike Johnson")
                    .accommodationName("Cozy Beach House")
                    .status(ReservationResponseMessage.ReservationStatus.APPROVED)
                    .checkIn(LocalDate.of(2024, 3, 15))
                    .checkOut(LocalDate.of(2024, 3, 20))
                    .build();
        };
    }
}
