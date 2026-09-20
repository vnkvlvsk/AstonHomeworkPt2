package Homework4.assembler;

import Homework4.controller.UserController;
import Homework4.dto.UserResponse;
import Homework4.entity.User;
import Homework4.mapper.UserMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<UserResponse>> {

    private final UserMapper userMapper;

    public UserModelAssembler(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public EntityModel<UserResponse> toModel(User user) {
        UserResponse response = userMapper.toResponse(user);
        return EntityModel.of(response,
                linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"),
                linkTo(methodOn(UserController.class).updateUser(user.getId(), null)).withRel("update"),
                linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete"));
    }
}
