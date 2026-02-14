package com.devoops.notification.service;

import com.devoops.notification.dto.message.NotificationMessage;
import com.devoops.notification.entity.NotificationPreferences;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationPreferencesService preferencesService;
    private final EmailService emailService;

    public void processNotification(NotificationMessage message) {
        log.debug("Processing notification for user {}: type={}",
                message.getUserId(), message.getType());

        NotificationPreferences preferences = preferencesService.getOrCreatePreferences(
                message.getUserId(),
                message.getUserEmail()
        );

        if (!preferences.getPreferences().isEnabled(message.getType())) {
            log.info("Notification type {} is disabled for user {}, skipping email",
                    message.getType(), message.getUserId());
            return;
        }

        emailService.sendNotificationEmail(message);

        log.info("Notification processed successfully for user {}: type={}",
                message.getUserId(), message.getType());
    }
}
