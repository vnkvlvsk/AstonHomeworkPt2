package Homework5.service;

import Homework5.event.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    private static final String FROM = "noreply@user-service.local";

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, FROM);
    }

    @Test
    void sendUserEventNotification_created_sendsExpectedMessage() {
        emailService.sendUserEventNotification("ivan@example.com", OperationType.CREATED);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertEquals(FROM, sent.getFrom());
        assertEquals("ivan@example.com", sent.getTo()[0]);
        assertEquals("Аккаунт создан", sent.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.", sent.getText());
    }

    @Test
    void sendUserEventNotification_deleted_sendsExpectedMessage() {
        emailService.sendUserEventNotification("ivan@example.com", OperationType.DELETED);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertEquals("ivan@example.com", sent.getTo()[0]);
        assertEquals("Аккаунт удалён", sent.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", sent.getText());
    }
}
