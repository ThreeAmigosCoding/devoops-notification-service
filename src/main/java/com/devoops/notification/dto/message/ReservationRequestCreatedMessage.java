package com.devoops.notification.dto.message;

import com.devoops.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ReservationRequestCreatedMessage extends NotificationMessage {

    private String guestName;
    private String accommodationName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal totalPrice;

    @Override
    public NotificationType getType() {
        return NotificationType.RESERVATION_REQUEST_CREATED;
    }

    @Override
    public String getSubject() {
        return "New Reservation Request for " + accommodationName;
    }
}
