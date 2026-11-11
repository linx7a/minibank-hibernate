package sorokin.java.course.console;

import org.springframework.stereotype.Component;
import sorokin.java.course.operations.ConsoleOperationType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class ConsoleInput {

    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public ConsoleOperationType readOperationType() {
        while (true) {
            System.out.println("Enter command:");
            String value = scanner.nextLine().trim();
            if (value.isBlank()) {
                System.out.println("Error: command must not be blank");
                printAvailableCommands();
                continue;
            }
            try {
                return ConsoleOperationType.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                System.out.println("Error: unknown command");
                printAvailableCommands();
            }
        }
    }

    public String readRequiredString(String prompt, String fieldName) {
        while (true) {
            System.out.println(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isBlank()) {
                return value;
            }
            System.out.println("Error: " + fieldName + " must not be blank");
        }
    }

    public int readPositiveInt(String prompt, String fieldName) {
        while (true) {
            System.out.println(prompt);
            String value = scanner.nextLine().trim();
            if (value.isBlank()) {
                System.out.println("Error: " + fieldName + " must not be blank");
                continue;
            }
            try {
                int parsed = Integer.parseInt(value);
                if (parsed <= 0) {
                    System.out.println("Error: " + fieldName + " must be > 0");
                    continue;
                }
                return parsed;
            } catch (NumberFormatException e) {
                System.out.println("Error: " + fieldName + " must be a number");
            }
        }
    }

    public long readPositiveLong(String prompt, String fieldName) {
        while (true) {
            System.out.println(prompt);
            String value = scanner.nextLine().trim();
            if (value.isBlank()) {
                System.out.println("Error: " + fieldName + " must not be blank");
                continue;
            }
            try {
                long parsed = Long.parseLong(value);
                if (parsed <= 0) {
                    System.out.println("Error: " + fieldName + " must be > 0");
                    continue;
                }
                return parsed;
            } catch (NumberFormatException e) {
                System.out.println("Error: " + fieldName + " must be a number");
            }
        }
    }


    public void printAvailableCommands() {
        String availableCommands = Arrays.stream(ConsoleOperationType.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
        System.out.println("Available commands: " + availableCommands);
    }
}
