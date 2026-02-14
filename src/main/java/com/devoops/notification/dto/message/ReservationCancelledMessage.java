package com.devoops.notification.dto.message;

import com.devoops.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ReservationCancelledMessage extends NotificationMessage {

    private String guestName;
    private String accommodationName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String reason;

    @Override
    public NotificationType getType() {
        return NotificationType.RESERVATION_CANCELLED;
    }

    @Override
    public String getSubject() {
        return "Reservation Cancelled - " + accommodationName;
    }
}
