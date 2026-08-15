package Homework2.dao;

import Homework2.entity.User;
import Homework2.exception.DaoException;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User create(User user) throws DaoException;
    Optional<User> findById(Long id) throws DaoException;
    List<User> findAll() throws DaoException;
    User update(User user) throws DaoException;
    boolean delete(Long id) throws DaoException;
}
