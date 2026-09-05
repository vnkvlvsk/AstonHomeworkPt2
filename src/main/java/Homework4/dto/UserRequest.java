package Homework4.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserRequest(

        @NotBlank(message = "Имя не может быть пустым")
        String name,

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        String email,

        @NotNull(message = "Возраст обязателен")
        @Positive(message = "Возраст должен быть положительным числом")
        Integer age
) {
}
