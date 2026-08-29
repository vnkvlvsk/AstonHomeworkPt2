package Homework2.service;

import Homework2.dao.UserDao;
import Homework2.entity.User;
import Homework2.exception.DaoException;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User createUser(String name, String email, Integer age) {
        return userDao.create(new User(name, email, age));
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public User updateUser(Long id, String name, String email, Integer age) {
        User existing = userDao.findById(id)
                .orElseThrow(() -> new DaoException("Пользователь с id=" + id + " не найден"));

        if (name != null && !name.isBlank()) {
            existing.setName(name);
        }
        if (email != null && !email.isBlank()) {
            existing.setEmail(email);
        }
        if (age != null) {
            existing.setAge(age);
        }
        return userDao.update(existing);
    }

    @Override
    public boolean deleteUser(Long id) {
        return userDao.delete(id);
    }
}
