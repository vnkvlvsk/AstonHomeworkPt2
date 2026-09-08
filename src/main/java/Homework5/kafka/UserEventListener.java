package Homework5.kafka;

import Homework5.event.UserEventMessage;
import Homework5.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserEventListener.class);

    private final EmailService emailService;

    public UserEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "user-events", groupId = "notification-service")
    public void onUserEvent(UserEventMessage message) {
        log.info("Получено событие {} для {}", message.operation(), message.email());
        emailService.sendUserEventNotification(message.email(), message.operation());
    }
}
