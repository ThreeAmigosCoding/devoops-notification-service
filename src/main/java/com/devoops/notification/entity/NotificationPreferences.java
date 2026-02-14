package com.devoops.notification.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Document(collection = "notification_preferences")
public class NotificationPreferences extends BaseDocument {

    @Indexed(unique = true)
    private UUID userId;

    private String userEmail;

    @Builder.Default
    private Preferences preferences = new Preferences();

    public NotificationPreferences(UUID userId, String userEmail) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.preferences = new Preferences();
    }
}
