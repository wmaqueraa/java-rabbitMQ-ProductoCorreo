package com.example.userservice.messaging;

import com.example.userservice.model.EmailNotification;
import com.example.userservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    private final EmailService emailService;

    public MessageConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receiveEmailNotification(EmailNotification notification) {
        logger.info("Received email notification: {}", notification);
        try {
            emailService.sendEmail(notification);
            logger.info("Email sent successfully to: {}", notification.getTo());
        } catch (Exception e) {
            logger.error("Failed to process email notification: {}", e.getMessage(), e);
        }
    }
}