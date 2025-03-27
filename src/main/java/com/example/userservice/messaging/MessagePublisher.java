package com.example.userservice.messaging;

import com.example.userservice.model.EmailNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(MessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private boolean rabbitMQAvailable = true;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;

    public MessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        // Verificar si RabbitMQ está disponible
        try {
            rabbitTemplate.execute(channel -> null);
            logger.info("RabbitMQ connection successful");
        } catch (AmqpConnectException e) {
            logger.warn("RabbitMQ connection failed: {}. Notifications will be logged only.", e.getMessage());
            rabbitMQAvailable = false;
        }
    }

    public void publishEmailNotification(EmailNotification notification) {
        try {
            if (rabbitMQAvailable) {
                logger.info("Sending email notification to queue: {}", notification.getTo());
                rabbitTemplate.convertAndSend(exchange, routingKey, notification);
                logger.info("Email notification sent successfully to queue");
            } else {
                // Si RabbitMQ no está disponible, simplemente registramos la notificación
                logger.info("RabbitMQ not available. Would have sent email to: {}", notification.getTo());
                logger.info("Email Subject: {}", notification.getSubject());
                logger.info("Email Body: {}", notification.getBody());
            }
        } catch (Exception e) {
            logger.error("Error sending email notification: {}", e.getMessage(), e);
            // No lanzamos la excepción para evitar que falle toda la operación
            logger.info("Email would have been sent to: {}", notification.getTo());
        }
    }
}