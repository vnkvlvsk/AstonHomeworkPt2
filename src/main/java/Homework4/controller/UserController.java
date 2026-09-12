package Homework4.controller;

import Homework4.assembler.UserModelAssembler;
import Homework4.dto.UserRequest;
import Homework4.dto.UserResponse;
import Homework4.entity.User;
import Homework4.mapper.UserMapper;
import Homework4.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Пользователи", description = "CRUD-операции над сущностью User")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserModelAssembler assembler;

    public UserController(UserService userService, UserMapper userMapper, UserModelAssembler assembler) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.assembler = assembler;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь создан"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные запроса"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    public EntityModel<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User created = userService.createUser(userMapper.toEntity(request));
        return assembler.toModel(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Найти пользователя по id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public EntityModel<UserResponse> getUserById(
            @Parameter(description = "ID пользователя") @PathVariable Long id) {
        User user = userService.getUserById(id);
        return assembler.toModel(user);
    }

    @GetMapping
    @Operation(summary = "Получить список всех пользователей")
    public CollectionModel<EntityModel<UserResponse>> getAllUsers() {
        List<EntityModel<UserResponse>> users = userService.getAllUsers().stream()
                .map(assembler::toModel)
                .toList();
        return CollectionModel.of(users, linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь обновлён"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Email уже занят другим пользователем")
    })
    public EntityModel<UserResponse> updateUser(
            @Parameter(description = "ID пользователя") @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        User updated = userService.updateUser(id, userMapper.toEntity(request));
        return assembler.toModel(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь удалён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<Void> deleteUser(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
