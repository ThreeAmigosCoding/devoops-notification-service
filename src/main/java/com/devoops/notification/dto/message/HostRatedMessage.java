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
public class HostRatedMessage extends NotificationMessage {

    private String guestName;
    private Integer rating;
    private String comment;

    @Override
    public NotificationType getType() {
        return NotificationType.HOST_RATED;
    }

    @Override
    public String getSubject() {
        return "You've Received a New Rating!";
    }
}
