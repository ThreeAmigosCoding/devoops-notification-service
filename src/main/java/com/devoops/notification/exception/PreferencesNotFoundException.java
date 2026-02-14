package com.devoops.notification.exception;

import java.util.UUID;

public class PreferencesNotFoundException extends RuntimeException {

    public PreferencesNotFoundException(UUID userId) {
        super("Notification preferences not found for user: " + userId);
    }
}
