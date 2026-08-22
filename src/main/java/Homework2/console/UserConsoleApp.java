package Homework2.console;

import Homework2.entity.User;
import Homework2.exception.DaoException;
import Homework2.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserConsoleApp {

    private static final Logger log = LoggerFactory.getLogger(UserConsoleApp.class);

    private final UserService userService;
    private final ConsoleInputReader input;

    public UserConsoleApp(UserService userService, ConsoleInputReader input) {
        this.userService = userService;
        this.input = input;
    }

    public void run() {
        System.out.println("=== user-service: управление пользователями ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = input.readLine("Выберите действие: ");
            try {
                switch (choice) {
                    case "1" -> createUser();
                    case "2" -> findUserById();
                    case "3" -> printAllUsers();
                    case "4" -> updateUser();
                    case "5" -> deleteUser();
                    case "0" -> running = false;
                    default -> System.out.println("Неизвестная команда: " + choice);
                }
            } catch (DaoException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                log.error("Неожиданная ошибка при обработке команды '{}'", choice, e);
                System.out.println("Непредвиденная ошибка, подробности см. в логе.");
            }
        }
        System.out.println("До встречи!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по id");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("0. Выход");
    }

    private void createUser() {
        String name = input.readNonBlank("Имя: ");
        String email = input.readNonBlank("Email: ");
        Integer age = input.readPositiveInt("Возраст: ");

        User created = userService.createUser(name, email, age);
        System.out.println("Создан: " + created);
    }

    private void findUserById() {
        Long id = input.readLong("ID: ");
        Optional<User> user = userService.getUserById(id);
        System.out.println(user.isPresent() ? user.get() : "Пользователь с id=" + id + " не найден.");
    }

    private void printAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Пользователей пока нет.");
            return;
        }
        users.forEach(System.out::println);
    }

    private void updateUser() {
        Long id = input.readLong("ID пользователя для обновления: ");
        Optional<User> found = userService.getUserById(id);
        if (found.isEmpty()) {
            System.out.println("Пользователь с id=" + id + " не найден.");
            return;
        }

        User user = found.get();
        System.out.println("Текущие данные: " + user);

        String name = input.readLine("Новое имя [" + user.getName() + "]: ");
        String email = input.readLine("Новый email [" + user.getEmail() + "]: ");
        String ageInput = input.readLine("Новый возраст [" + user.getAge() + "]: ");

        Integer age = null;
        if (!ageInput.isEmpty()) {
            try {
                age = Integer.parseInt(ageInput);
            } catch (NumberFormatException e) {
                System.out.println("Некорректный возраст, значение оставлено без изменений.");
            }
        }

        User updated = userService.updateUser(id, name, email, age);
        System.out.println("Обновлён: " + updated);
    }

    private void deleteUser() {
        Long id = input.readLong("ID пользователя для удаления: ");
        boolean deleted = userService.deleteUser(id);
        System.out.println(deleted ? "Пользователь удалён." : "Пользователь с id=" + id + " не найден.");
    }
}
