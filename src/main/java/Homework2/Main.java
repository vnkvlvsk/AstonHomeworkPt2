package Homework2;

import Homework2.console.ConsoleInputReader;
import Homework2.console.UserConsoleApp;
import Homework2.dao.UserDao;
import Homework2.dao.UserDaoImpl;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        UserDao userDao = new UserDaoImpl();
        ConsoleInputReader input = new ConsoleInputReader(new Scanner(System.in));
        new UserConsoleApp(userDao, input).run();
    }
}
