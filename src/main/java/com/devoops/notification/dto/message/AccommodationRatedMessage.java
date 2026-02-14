package com.devoops.notification.dto.message;

import com.devoops.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AccommodationRatedMessage extends NotificationMessage {

    private String guestName;
    private String accommodationName;
    private Integer rating;
    private String comment;

    @Override
    public NotificationType getType() {
        return NotificationType.ACCOMMODATION_RATED;
    }

    @Override
    public String getSubject() {
        return "New Review for " + accommodationName;
    }
}
