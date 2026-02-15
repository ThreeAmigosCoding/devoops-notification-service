package com.devoops.notification.util;

import com.devoops.notification.dto.message.*;
import com.devoops.notification.entity.NotificationPreferences;
import com.devoops.notification.entity.Preferences;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class TestDataFactory {

    public static final UUID DEFAULT_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final String DEFAULT_USER_EMAIL = "test@example.com";
    public static final String DEFAULT_GUEST_NAME = "John Doe";
    public static final String DEFAULT_HOST_NAME = "Jane Host";
    public static final String DEFAULT_ACCOMMODATION_NAME = "Cozy Beach House";

    private TestDataFactory() {
    }

    public static NotificationPreferences createDefaultPreferences() {
        return createPreferences(DEFAULT_USER_ID, DEFAULT_USER_EMAIL);
    }

    public static NotificationPreferences createPreferences(UUID userId, String userEmail) {
        NotificationPreferences prefs = new NotificationPreferences(userId, userEmail);
        return prefs;
    }

    public static NotificationPreferences createPreferencesWithAllDisabled(UUID userId, String userEmail) {
        NotificationPreferences prefs = new NotificationPreferences(userId, userEmail);
        Preferences preferences = prefs.getPreferences();
        preferences.setReservationRequestCreated(false);
        preferences.setReservationCancelled(false);
        preferences.setHostRated(false);
        preferences.setAccommodationRated(false);
        preferences.setReservationResponse(false);
        return prefs;
    }

    public static UserCreatedMessage createUserCreatedMessage() {
        return createUserCreatedMessage(DEFAULT_USER_ID, DEFAULT_USER_EMAIL);
    }

    public static UserCreatedMessage createUserCreatedMessage(UUID userId, String userEmail) {
        return UserCreatedMessage.builder()
                .userId(userId)
                .userEmail(userEmail)
                .build();
    }

    public static ReservationRequestCreatedMessage createReservationRequestCreatedMessage() {
        return createReservationRequestCreatedMessage(DEFAULT_USER_ID, DEFAULT_USER_EMAIL);
    }

    public static ReservationRequestCreatedMessage createReservationRequestCreatedMessage(UUID userId, String userEmail) {
        return ReservationRequestCreatedMessage.builder()
                .userId(userId)
                .userEmail(userEmail)
                .guestName(DEFAULT_GUEST_NAME)
                .accommodationName(DEFAULT_ACCOMMODATION_NAME)
                .checkIn(LocalDate.now().plusDays(7))
                .checkOut(LocalDate.now().plusDays(14))
                .totalPrice(new BigDecimal("750.00"))
                .build();
    }

    public static ReservationCancelledMessage createReservationCancelledMessage() {
        return createReservationCancelledMessage(DEFAULT_USER_ID, DEFAULT_USER_EMAIL);
    }

    public static ReservationCancelledMessage createReservationCancelledMessage(UUID userId, String userEmail) {
        return ReservationCancelledMessage.builder()
                .userId(userId)
                .userEmail(userEmail)
                .guestName(DEFAULT_GUEST_NAME)
                .accommodationName(DEFAULT_ACCOMMODATION_NAME)
                .checkIn(LocalDate.now().plusDays(7))
                .checkOut(LocalDate.now().plusDays(14))
                .reason("Change of plans")
                .build();
    }

    public static HostRatedMessage createHostRatedMessage() {
        return createHostRatedMessage(DEFAULT_USER_ID, DEFAULT_USER_EMAIL);
    }

    public static HostRatedMessage createHostRatedMessage(UUID userId, String userEmail) {
        return HostRatedMessage.builder()
                .userId(userId)
                .userEmail(userEmail)
                .guestName(DEFAULT_GUEST_NAME)
                .rating(5)
                .comment("Excellent host! Very friendly and helpful.")
                .build();
    }

    public static AccommodationRatedMessage createAccommodationRatedMessage() {
        return createAccommodationRatedMessage(DEFAULT_USER_ID, DEFAULT_USER_EMAIL);
    }

    public static AccommodationRatedMessage createAccommodationRatedMessage(UUID userId, String userEmail) {
        return AccommodationRatedMessage.builder()
                .userId(userId)
                .userEmail(userEmail)
                .guestName(DEFAULT_GUEST_NAME)
                .accommodationName(DEFAULT_ACCOMMODATION_NAME)
                .rating(4)
                .comment("Great place, clean and comfortable!")
                .build();
    }

    public static ReservationResponseMessage createReservationResponseMessage(ReservationResponseMessage.ReservationStatus status) {
        return createReservationResponseMessage(DEFAULT_USER_ID, DEFAULT_USER_EMAIL, status);
    }

    public static ReservationResponseMessage createReservationResponseMessage(
            UUID userId, String userEmail, ReservationResponseMessage.ReservationStatus status) {
        return ReservationResponseMessage.builder()
                .userId(userId)
                .userEmail(userEmail)
                .hostName(DEFAULT_HOST_NAME)
                .accommodationName(DEFAULT_ACCOMMODATION_NAME)
                .status(status)
                .checkIn(LocalDate.now().plusDays(7))
                .checkOut(LocalDate.now().plusDays(14))
                .build();
    }

    public static ReservationResponseMessage createApprovedReservationResponseMessage() {
        return createReservationResponseMessage(ReservationResponseMessage.ReservationStatus.APPROVED);
    }

    public static ReservationResponseMessage createDeclinedReservationResponseMessage() {
        return createReservationResponseMessage(ReservationResponseMessage.ReservationStatus.DECLINED);
    }
}
