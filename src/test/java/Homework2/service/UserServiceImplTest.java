package Homework2.service;

import Homework2.dao.UserDao;
import Homework2.entity.User;
import Homework2.exception.DaoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_passesNewUserToDaoAndReturnsResult() {
        User saved = new User("Ivan", "ivan@example.com", 30);
        when(userDao.create(any(User.class))).thenReturn(saved);

        User result = userService.createUser("Ivan", "ivan@example.com", 30);

        assertEquals(saved, result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).create(captor.capture());
        assertEquals("Ivan", captor.getValue().getName());
        assertEquals("ivan@example.com", captor.getValue().getEmail());
        assertEquals(30, captor.getValue().getAge());
    }

    @Test
    void getUserById_found_returnsUserFromDao() {
        User user = new User("Ivan", "ivan@example.com", 30);
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void getUserById_missing_returnsEmpty() {
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        assertTrue(userService.getUserById(99L).isEmpty());
    }

    @Test
    void getAllUsers_returnsListFromDao() {
        List<User> users = List.of(
                new User("A", "a@example.com", 20),
                new User("B", "b@example.com", 25));
        when(userDao.findAll()).thenReturn(users);

        assertEquals(users, userService.getAllUsers());
    }

    @Test
    void updateUser_blankFields_keepExistingValues() {
        User existing = new User("OldName", "old@example.com", 20);
        when(userDao.findById(1L)).thenReturn(Optional.of(existing));
        when(userDao.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, "NewName", "", null);

        assertEquals("NewName", result.getName());
        assertEquals("old@example.com", result.getEmail());
        assertEquals(20, result.getAge());
        verify(userDao).update(eq(existing));
    }

    @Test
    void updateUser_allFieldsProvided_overwritesAll() {
        User existing = new User("OldName", "old@example.com", 20);
        when(userDao.findById(1L)).thenReturn(Optional.of(existing));
        when(userDao.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, "NewName", "new@example.com", 40);

        assertEquals("NewName", result.getName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(40, result.getAge());
    }

    @Test
    void updateUser_missingUser_throwsDaoExceptionAndNeverCallsUpdate() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DaoException.class, () -> userService.updateUser(1L, "X", "x@example.com", 20));

        verify(userDao, never()).update(any());
    }

    @Test
    void deleteUser_delegatesToDaoAndReturnsResult() {
        when(userDao.delete(1L)).thenReturn(true);

        assertTrue(userService.deleteUser(1L));
    }

    @Test
    void deleteUser_missingUser_returnsFalse() {
        when(userDao.delete(99L)).thenReturn(false);

        assertFalse(userService.deleteUser(99L));
    }
}
