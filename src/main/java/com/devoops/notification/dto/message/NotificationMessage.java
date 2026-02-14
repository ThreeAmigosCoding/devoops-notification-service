package com.devoops.notification.dto.message;

import com.devoops.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class NotificationMessage implements Serializable {

    private UUID userId;
    private String userEmail;

    public abstract NotificationType getType();

    public abstract String getSubject();
}
