package Homework4.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    private static final String TOPIC = "user-events";

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);

    private final KafkaTemplate<String, UserEventMessage> kafkaTemplate;

    public UserEventPublisher(KafkaTemplate<String, UserEventMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(OperationType operation, String email) {
        kafkaTemplate.send(TOPIC, email, new UserEventMessage(operation, email))
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Не удалось отправить событие {} для {} в Kafka", operation, email, ex);
                    } else {
                        log.info("Событие {} для {} отправлено в Kafka", operation, email);
                    }
                });
    }
}
