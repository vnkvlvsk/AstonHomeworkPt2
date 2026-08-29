package Homework2.service;

import Homework2.entity.User;
import Homework2.exception.DaoException;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(String name, String email, Integer age) throws DaoException;
    Optional<User> getUserById(Long id) throws DaoException;
    List<User> getAllUsers() throws DaoException;
    User updateUser(Long id, String name, String email, Integer age) throws DaoException;
    boolean deleteUser(Long id) throws DaoException;
}
