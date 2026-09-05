package Homework4.event;

/**
 * Сообщение о событии пользователя, публикуемое в Kafka для notification-service.
 */
public record UserEventMessage(OperationType operation, String email) {
}
