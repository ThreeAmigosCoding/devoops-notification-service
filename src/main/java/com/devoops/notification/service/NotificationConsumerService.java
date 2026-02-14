package com.devoops.notification.service;

import com.devoops.notification.dto.message.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumerService {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queue.reservation-created}")
    public void handleReservationCreated(ReservationRequestCreatedMessage message) {
        log.info("Received reservation created notification for user {}", message.getUserId());
        notificationService.processNotification(message);
    }

    @RabbitListener(queues = "${rabbitmq.queue.reservation-cancelled}")
    public void handleReservationCancelled(ReservationCancelledMessage message) {
        log.info("Received reservation cancelled notification for user {}", message.getUserId());
        notificationService.processNotification(message);
    }

    @RabbitListener(queues = "${rabbitmq.queue.host-rated}")
    public void handleHostRated(HostRatedMessage message) {
        log.info("Received host rated notification for user {}", message.getUserId());
        notificationService.processNotification(message);
    }

    @RabbitListener(queues = "${rabbitmq.queue.accommodation-rated}")
    public void handleAccommodationRated(AccommodationRatedMessage message) {
        log.info("Received accommodation rated notification for user {}", message.getUserId());
        notificationService.processNotification(message);
    }

    @RabbitListener(queues = "${rabbitmq.queue.reservation-response}")
    public void handleReservationResponse(ReservationResponseMessage message) {
        log.info("Received reservation response notification for user {}", message.getUserId());
        notificationService.processNotification(message);
    }
}
