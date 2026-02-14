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
public class ReservationResponseMessage extends NotificationMessage {

    private String hostName;
    private String accommodationName;
    private ReservationStatus status;
    private LocalDate checkIn;
    private LocalDate checkOut;

    public enum ReservationStatus {
        APPROVED,
        DECLINED
    }

    @Override
    public NotificationType getType() {
        return NotificationType.RESERVATION_RESPONSE;
    }

    @Override
    public String getSubject() {
        return status == ReservationStatus.APPROVED
                ? "Your Reservation is Confirmed!"
                : "Reservation Request Update";
    }
}
