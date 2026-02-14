package com.devoops.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Preferences {

    private boolean reservationRequestCreated = true;
    private boolean reservationCancelled = true;
    private boolean hostRated = true;
    private boolean accommodationRated = true;
    private boolean reservationResponse = true;

    public boolean isEnabled(NotificationType type) {
        return switch (type) {
            case RESERVATION_REQUEST_CREATED -> reservationRequestCreated;
            case RESERVATION_CANCELLED -> reservationCancelled;
            case HOST_RATED -> hostRated;
            case ACCOMMODATION_RATED -> accommodationRated;
            case RESERVATION_RESPONSE -> reservationResponse;
        };
    }
}
