package Homework4.service;

import Homework4.entity.User;
import Homework4.exception.DuplicateEmailException;
import Homework4.exception.UserNotFoundException;
import Homework4.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("Пользователь с email '" + user.getEmail() + "' уже существует");
        }
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id=" + id + " не найден"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User changes) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id=" + id + " не найден"));

        existing.setName(changes.getName());
        existing.setEmail(changes.getEmail());
        existing.setAge(changes.getAge());

        try {
            return userRepository.save(existing);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("Email '" + changes.getEmail() + "' уже занят другим пользователем");
        }
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Пользователь с id=" + id + " не найден");
        }
        userRepository.deleteById(id);
    }
}
