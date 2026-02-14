package com.devoops.notification.dto.response;

import java.util.UUID;

public record NotificationPreferencesResponse(
        UUID userId,
        String userEmail,
        boolean reservationRequestCreated,
        boolean reservationCancelled,
        boolean hostRated,
        boolean accommodationRated,
        boolean reservationResponse
) {
}
