package Homework5.service;

import Homework5.event.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailServiceImpl(JavaMailSender mailSender,
                             @Value("${notification.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendUserEventNotification(String email, OperationType operation) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject(subjectFor(operation));
        message.setText(textFor(operation));

        mailSender.send(message);
        log.info("Отправлено уведомление '{}' на адрес {}", operation, email);
    }

    private String subjectFor(OperationType operation) {
        return switch (operation) {
            case CREATED -> "Аккаунт создан";
            case DELETED -> "Аккаунт удалён";
        };
    }

    private String textFor(OperationType operation) {
        return switch (operation) {
            case CREATED -> "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.";
            case DELETED -> "Здравствуйте! Ваш аккаунт был удалён.";
        };
    }
}
