package Homework4.service;

import Homework4.entity.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, User changes);
    void deleteUser(Long id);
}
