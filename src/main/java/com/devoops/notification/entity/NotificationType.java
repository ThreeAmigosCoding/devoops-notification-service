package com.devoops.notification.entity;

public enum NotificationType {
    RESERVATION_REQUEST_CREATED("reservationRequestCreated", "New Reservation Request"),
    RESERVATION_CANCELLED("reservationCancelled", "Reservation Cancelled"),
    HOST_RATED("hostRated", "You've Been Rated"),
    ACCOMMODATION_RATED("accommodationRated", "Accommodation Review Received"),
    RESERVATION_RESPONSE("reservationResponse", "Reservation Update");

    private final String preferenceKey;
    private final String emailSubject;

    NotificationType(String preferenceKey, String emailSubject) {
        this.preferenceKey = preferenceKey;
        this.emailSubject = emailSubject;
    }

    public String getPreferenceKey() {
        return preferenceKey;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public String getTemplateName() {
        return switch (this) {
            case RESERVATION_REQUEST_CREATED -> "reservation-request-created";
            case RESERVATION_CANCELLED -> "reservation-cancelled";
            case HOST_RATED -> "host-rated";
            case ACCOMMODATION_RATED -> "accommodation-rated";
            case RESERVATION_RESPONSE -> "reservation-response";
        };
    }
}
