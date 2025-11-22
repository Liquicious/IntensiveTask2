package org.example.userservice;

import org.example.userservice.service.UserService;
import org.example.userservice.model.User;
import org.example.userservice.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final UserService userService = new UserService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        logger.info("Starting User Service application");

        try {
            showMenu();
        } catch (Exception e) {
            logger.error("Application error: {}", e.getMessage());
            System.err.println("Critical error: " + e.getMessage());
        } finally {
            HibernateUtil.shutdown();
            scanner.close();
            logger.info("User Service application stopped");
        }
    }

    private static void showMenu() {
        while (true) {
            System.out.println("\n=== User Service ===");
            System.out.println("1. Create User");
            System.out.println("2. Get User by ID");
            System.out.println("3. Get All Users");
            System.out.println("4. Update User");
            System.out.println("5. Delete User");
            System.out.println("0. Exit");
            System.out.print("Choose option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1": createUser(); break;
                case "2": getUserById(); break;
                case "3": getAllUsers(); break;
                case "4": updateUser(); break;
                case "5": deleteUser(); break;
                case "0":
                    System.out.println("Shutting down...");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void createUser() {
        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();
            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            System.out.print("Enter age: ");
            String ageInput = scanner.nextLine();
            Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);

            Long id = userService.createUser(name, email, age);
            System.out.println("User created successfully with ID: " + id);
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
        }
    }

    private static void getUserById() {
        try {
            System.out.print("Enter user ID: ");
            Long id = Long.parseLong(scanner.nextLine());
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                System.out.println("User found: " + user.get());
            } else {
                System.out.println("User not found with ID: " + id);
            }
        } catch (Exception e) {
            System.out.println("Error finding user: " + e.getMessage());
        }
    }

    private static void getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("No users found");
            } else {
                System.out.println("Users:");
                users.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.out.println("Error getting users: " + e.getMessage());
        }
    }

    private static void updateUser() {
        try {
            System.out.print("Enter user ID to update: ");
            Long id = Long.parseLong(scanner.nextLine());
            System.out.print("Enter new name (leave empty to keep current): ");
            String name = scanner.nextLine();
            System.out.print("Enter new email (leave empty to keep current): ");
            String email = scanner.nextLine();
            System.out.print("Enter new age (leave empty to keep current): ");
            String ageInput = scanner.nextLine();
            Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);

            boolean updated = userService.updateUser(id,
                    name.isEmpty() ? null : name,
                    email.isEmpty() ? null : email,
                    age);

            if (updated) {
                System.out.println("User updated successfully");
            } else {
                System.out.println("User not found with ID: " + id);
            }
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        try {
            System.out.print("Enter user ID to delete: ");
            Long id = Long.parseLong(scanner.nextLine());
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                System.out.println("User deleted successfully");
            } else {
                System.out.println("User not found with ID: " + id);
            }
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
    }
}