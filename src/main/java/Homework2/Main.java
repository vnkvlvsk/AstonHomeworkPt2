package Homework2;

import Homework2.console.ConsoleInputReader;
import Homework2.console.UserConsoleApp;
import Homework2.dao.UserDao;
import Homework2.dao.UserDaoImpl;
import Homework2.service.UserService;
import Homework2.service.UserServiceImpl;
import Homework2.util.HibernateUtil;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        UserDao userDao = new UserDaoImpl(HibernateUtil.getSessionFactory());
        UserService userService = new UserServiceImpl(userDao);
        ConsoleInputReader input = new ConsoleInputReader(new Scanner(System.in));
        new UserConsoleApp(userService, input).run();
    }
}
