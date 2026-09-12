package Homework4.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserRequest(

        @Schema(description = "Имя пользователя", example = "Иван Иванов")
        @NotBlank(message = "Имя не может быть пустым")
        String name,

        @Schema(description = "Email пользователя, должен быть уникальным", example = "ivan@example.com")
        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        String email,

        @Schema(description = "Возраст пользователя", example = "30")
        @NotNull(message = "Возраст обязателен")
        @Positive(message = "Возраст должен быть положительным числом")
        Integer age
) {
}
