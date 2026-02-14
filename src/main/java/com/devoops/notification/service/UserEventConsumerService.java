package com.devoops.notification.service;

import com.devoops.notification.dto.message.UserCreatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventConsumerService {

    private final NotificationPreferencesService preferencesService;

    @RabbitListener(queues = "${rabbitmq.queue.user-created}")
    public void handleUserCreated(UserCreatedMessage message) {
        log.info("Received user created event for user {}", message.getUserId());
        preferencesService.initializePreferences(message.getUserId(), message.getUserEmail());
        log.info("Notification preferences initialized for user {}", message.getUserId());
    }
}
