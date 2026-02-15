package com.devoops.notification.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.notification}")
    private String notificationExchange;

    @Value("${rabbitmq.exchange.dlx}")
    private String deadLetterExchange;

    @Value("${rabbitmq.queue.dlq}")
    private String deadLetterQueue;

    @Value("${rabbitmq.queue.reservation-created}")
    private String reservationCreatedQueue;

    @Value("${rabbitmq.queue.reservation-cancelled}")
    private String reservationCancelledQueue;

    @Value("${rabbitmq.queue.host-rated}")
    private String hostRatedQueue;

    @Value("${rabbitmq.queue.accommodation-rated}")
    private String accommodationRatedQueue;

    @Value("${rabbitmq.queue.reservation-response}")
    private String reservationResponseQueue;

    @Value("${rabbitmq.queue.user-created}")
    private String userCreatedQueue;

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(notificationExchange);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchange);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueue).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("notification.dlq");
    }

    @Bean
    public Queue reservationCreatedQueue() {
        return QueueBuilder.durable(reservationCreatedQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", "notification.dlq")
                .build();
    }

    @Bean
    public Queue reservationCancelledQueue() {
        return QueueBuilder.durable(reservationCancelledQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", "notification.dlq")
                .build();
    }

    @Bean
    public Queue hostRatedQueue() {
        return QueueBuilder.durable(hostRatedQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", "notification.dlq")
                .build();
    }

    @Bean
    public Queue accommodationRatedQueue() {
        return QueueBuilder.durable(accommodationRatedQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", "notification.dlq")
                .build();
    }

    @Bean
    public Queue reservationResponseQueue() {
        return QueueBuilder.durable(reservationResponseQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", "notification.dlq")
                .build();
    }

    @Bean
    public Binding reservationCreatedBinding() {
        return BindingBuilder
                .bind(reservationCreatedQueue())
                .to(notificationExchange())
                .with("notification.reservation.created");
    }

    @Bean
    public Binding reservationCancelledBinding() {
        return BindingBuilder
                .bind(reservationCancelledQueue())
                .to(notificationExchange())
                .with("notification.reservation.cancelled");
    }

    @Bean
    public Binding hostRatedBinding() {
        return BindingBuilder
                .bind(hostRatedQueue())
                .to(notificationExchange())
                .with("notification.rating.host");
    }

    @Bean
    public Binding accommodationRatedBinding() {
        return BindingBuilder
                .bind(accommodationRatedQueue())
                .to(notificationExchange())
                .with("notification.rating.accommodation");
    }

    @Bean
    public Binding reservationResponseBinding() {
        return BindingBuilder
                .bind(reservationResponseQueue())
                .to(notificationExchange())
                .with("notification.reservation.response");
    }

    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(userCreatedQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", "notification.dlq")
                .build();
    }

    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder
                .bind(userCreatedQueue())
                .to(notificationExchange())
                .with("user.created");
    }
}
