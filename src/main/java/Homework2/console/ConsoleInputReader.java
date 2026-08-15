package Homework2.console;

import java.util.Scanner;

public class ConsoleInputReader {

    private final Scanner scanner;

    public ConsoleInputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readNonBlank(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Значение не может быть пустым.");
        }
    }

    public Integer readPositiveInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                int parsed = Integer.parseInt(value);
                if (parsed <= 0) {
                    System.out.println("Значение должно быть положительным числом.");
                    continue;
                }
                return parsed;
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число.");
            }
        }
    }

    public Long readLong(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число.");
            }
        }
    }
}
