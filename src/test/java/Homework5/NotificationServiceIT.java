package Homework5;

import Homework5.dto.NotificationRequest;
import Homework5.event.OperationType;
import Homework5.event.UserEventMessage;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Интеграционные тесты notification-service: реальный Kafka-брокер (Testcontainers)
 * и реальный (но фейковый) SMTP-сервер (GreenMail) вместо моков.
 * Проверяют оба входа - Kafka-листенер и прямой REST API - до фактически отправленного письма.
 */
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=notification-service",
                "spring.mail.port=3025"
        }
)
class NotificationServiceIT {

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

    // Фиксированный тестовый порт GreenMail (3025) вместо динамического: значение из
    // dynamicPort() было бы неизвестно на момент вызова @DynamicPropertySource, так как
    // GreenMail ещё не гарантированно запущен к этому моменту.
    @RegisterExtension
    static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.SMTP);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private KafkaTemplate<String, UserEventMessage> producer;

    @BeforeEach
    void setUp() {
        GREEN_MAIL.reset();

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        ProducerFactory<String, UserEventMessage> producerFactory = new DefaultKafkaProducerFactory<>(props);
        producer = new KafkaTemplate<>(producerFactory);
    }

    @Test
    void directApi_created_sendsCreationEmail() throws Exception {
        NotificationRequest request = new NotificationRequest("ivan@example.com", OperationType.CREATED);

        ResponseEntity<Void> response = restTemplate.postForEntity("/api/notifications", request, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertTrue(GREEN_MAIL.waitForIncomingEmail(5000, 1));
        MimeMessage received = GREEN_MAIL.getReceivedMessages()[0];
        assertEquals("ivan@example.com", received.getAllRecipients()[0].toString());
        assertEquals("Аккаунт создан", received.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.",
                ((String) received.getContent()).trim());
    }

    @Test
    void directApi_deleted_sendsDeletionEmail() throws Exception {
        NotificationRequest request = new NotificationRequest("ivan@example.com", OperationType.DELETED);

        ResponseEntity<Void> response = restTemplate.postForEntity("/api/notifications", request, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertTrue(GREEN_MAIL.waitForIncomingEmail(5000, 1));
        MimeMessage received = GREEN_MAIL.getReceivedMessages()[0];
        assertEquals("ivan@example.com", received.getAllRecipients()[0].toString());
        assertEquals("Аккаунт удалён", received.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", ((String) received.getContent()).trim());
    }

    @Test
    void directApi_blankEmail_returnsBadRequest() {
        NotificationRequest request = new NotificationRequest("", OperationType.CREATED);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/notifications", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void kafkaEvent_created_sendsCreationEmail() throws Exception {
        producer.send("user-events", "petr@example.com",
                new UserEventMessage(OperationType.CREATED, "petr@example.com"));

        assertTrue(GREEN_MAIL.waitForIncomingEmail(10000, 1));
        MimeMessage received = GREEN_MAIL.getReceivedMessages()[0];
        assertEquals("petr@example.com", received.getAllRecipients()[0].toString());
        assertEquals("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.",
                ((String) received.getContent()).trim());
    }

    @Test
    void kafkaEvent_deleted_sendsDeletionEmail() throws Exception {
        producer.send("user-events", "petr@example.com",
                new UserEventMessage(OperationType.DELETED, "petr@example.com"));

        assertTrue(GREEN_MAIL.waitForIncomingEmail(10000, 1));
        MimeMessage received = GREEN_MAIL.getReceivedMessages()[0];
        assertEquals("petr@example.com", received.getAllRecipients()[0].toString());
        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", ((String) received.getContent()).trim());
    }
}
