package com.devoops.notification.dto.request;

public record NotificationPreferencesUpdateRequest(
        Boolean reservationRequestCreated,
        Boolean reservationCancelled,
        Boolean hostRated,
        Boolean accommodationRated,
        Boolean reservationResponse
) {
}
