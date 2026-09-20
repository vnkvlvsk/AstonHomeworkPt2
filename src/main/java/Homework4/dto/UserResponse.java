package Homework4.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record UserResponse(
        @Schema(description = "ID пользователя", example = "1")
        Long id,

        @Schema(description = "Имя пользователя", example = "Иван Иванов")
        String name,

        @Schema(description = "Email пользователя", example = "ivan@example.com")
        String email,

        @Schema(description = "Возраст пользователя", example = "30")
        Integer age,

        @Schema(description = "Дата и время создания пользователя")
        LocalDateTime createdAt
) {
}
