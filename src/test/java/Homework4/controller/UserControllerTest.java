package Homework4.controller;

import Homework4.dto.UserRequest;
import Homework4.entity.User;
import Homework4.exception.DuplicateEmailException;
import Homework4.exception.UserNotFoundException;
import Homework4.mapper.UserMapper;
import Homework4.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserMapper.class)
class UserControllerTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private User buildUser(Long id, String name, String email, Integer age) {
        User user = new User(name, email, age);
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "createdAt", CREATED_AT);
        return user;
    }

    @Test
    void createUser_validRequest_returnsCreatedUser() throws Exception {
        UserRequest request = new UserRequest("Ivan", "ivan@example.com", 30);
        User saved = buildUser(1L, "Ivan", "ivan@example.com", 30);
        when(userService.createUser(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(userService).createUser(any(User.class));
    }

    @Test
    void createUser_blankName_returnsBadRequest() throws Exception {
        UserRequest request = new UserRequest("", "ivan@example.com", 30);

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());

        verify(userService, never()).createUser(any());
    }

    @Test
    void createUser_invalidEmail_returnsBadRequest() throws Exception {
        UserRequest request = new UserRequest("Ivan", "not-an-email", 30);

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void createUser_duplicateEmail_returnsConflict() throws Exception {
        UserRequest request = new UserRequest("Ivan", "dup@example.com", 30);
        when(userService.createUser(any(User.class)))
                .thenThrow(new DuplicateEmailException("Пользователь с email 'dup@example.com' уже существует"));

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Пользователь с email 'dup@example.com' уже существует"));
    }

    @Test
    void getUserById_found_returnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(buildUser(1L, "Ivan", "ivan@example.com", 30));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T12:00:00"));
    }

    @Test
    void getUserById_missing_returnsNotFound() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new UserNotFoundException("Пользователь с id=99 не найден"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id=99 не найден"));
    }

    @Test
    void getAllUsers_returnsList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                buildUser(1L, "A", "a@example.com", 20),
                buildUser(2L, "B", "b@example.com", 25)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("B"));
    }

    @Test
    void getAllUsers_empty_returnsEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updateUser_valid_returnsUpdatedUser() throws Exception {
        UserRequest request = new UserRequest("NewName", "new@example.com", 40);
        when(userService.updateUser(eq(1L), any(User.class)))
                .thenReturn(buildUser(1L, "NewName", "new@example.com", 40));

        mockMvc.perform(put("/api/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.age").value(40));
    }

    @Test
    void updateUser_missing_returnsNotFound() throws Exception {
        UserRequest request = new UserRequest("NewName", "new@example.com", 40);
        when(userService.updateUser(eq(99L), any(User.class)))
                .thenThrow(new UserNotFoundException("Пользователь с id=99 не найден"));

        mockMvc.perform(put("/api/users/99")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_duplicateEmail_returnsConflict() throws Exception {
        UserRequest request = new UserRequest("NewName", "taken@example.com", 40);
        when(userService.updateUser(eq(1L), any(User.class)))
                .thenThrow(new DuplicateEmailException("Email 'taken@example.com' уже занят другим пользователем"));

        mockMvc.perform(put("/api/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteUser_found_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_missing_returnsNotFound() throws Exception {
        doThrow(new UserNotFoundException("Пользователь с id=99 не найден"))
                .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}
