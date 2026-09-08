package Homework4.service;

import Homework4.entity.User;
import Homework4.event.OperationType;
import Homework4.event.UserEventPublisher;
import Homework4.exception.UserNotFoundException;
import Homework4.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Проверяет, что user-service публикует Kafka-событие ровно тогда, когда должен:
 * при успешном создании и при успешном удалении пользователя.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_success_publishesCreatedEvent() {
        User user = new User("Ivan", "ivan@example.com", 30);
        when(userRepository.save(user)).thenReturn(user);

        userService.createUser(user);

        verify(eventPublisher).publish(OperationType.CREATED, "ivan@example.com");
    }

    @Test
    void deleteUser_success_publishesDeletedEventWithEmailOfDeletedUser() {
        User existing = new User("Ivan", "ivan@example.com", 30);
        ReflectionTestUtils.setField(existing, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
        verify(eventPublisher).publish(OperationType.DELETED, "ivan@example.com");
    }

    @Test
    void deleteUser_missingUser_doesNotPublishEvent() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));

        verify(eventPublisher, never()).publish(any(), any());
    }
}
