package org.example.userservice.service;

import org.example.userservice.dao.UserDaoImpl;
import org.example.userservice.dao.UserDao;
import org.example.userservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Zа-яА-ЯёЁ\\s\\-']{2,100}$");

    public UserService() {
        this.userDao = new UserDaoImpl();
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        String trimmedName = name.trim();
        // Проверка длины
        if (trimmedName.length() < 2 || trimmedName.length() > 100) {
            throw new IllegalArgumentException(
                    "Name must contain between 2 and 100 characters. Received: " + trimmedName.length()
            );
        }

        // Проверка на допустимые символы
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            throw new IllegalArgumentException(
                    "Name can only contain letters, spaces, hyphens and apostrophes. Invalid name: " + name
            );
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        String trimmedEmail = email.trim().toLowerCase();
        // Проверка длины
        if (trimmedEmail.length() > 255) {
            throw new IllegalArgumentException(
                    "Email is too long. Maximum length is 255 characters"
            );
        }

        // Проверка формата email
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException(
                    "Invalid email format. Example: user@example.com. Received: " + email
            );
        }
    }

    private void validateAge(Integer age) {
        if (age == null) {
            throw new IllegalArgumentException(
                    "Age cannot be null");
        }

        if (age < 0) {
            throw new IllegalArgumentException(
                    "Age cannot be negative. Received: " + age
            );
        }

        if (age > 120) {
            throw new IllegalArgumentException(
                    "Age cannot exceed 120 years. Received: " + age
            );
        }
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        if (id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number");
        }
    }

    public Long createUser(String name, String email, Integer age) {
        logger.info("Attempting to create user: {}, {}, {}", name, email, age);

        validateName(name);
        validateEmail(email);
        validateAge(age);

        Optional<User> existingUser = userDao.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        User user = new User(name, email, age);
        return userDao.save(user);
    }

    public Optional<User> getUserById(Long id) {
        logger.info("Attempting to get user with ID: {}", id);

        validateId(id);
        return userDao.findById(id);
    }

    public List<User> getAllUsers() {
        logger.info("Attempting to get all users");

        return userDao.findAll();
    }

    public boolean updateUser(Long id, String name, String email, Integer age) {
        logger.info("Attempting to update user with ID: {}", id);

        validateId(id);

        Optional<User> userOpt = userDao.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (email != null && !email.equals(user.getEmail())) {
                Optional<User> existingUser = userDao.findByEmail(email);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Email already exists");
                }
                validateEmail(email);
                user.setEmail(email);
            }

            if (name != null) {
                validateName(name);
                user.setName(name);
            }
            if (age != null) {
                validateAge(age);
                user.setAge(age);
            }

            userDao.update(user);
            return true;
        }
        return false;
    }

    public boolean deleteUser(Long id) {
        logger.info("Attempting to delete user with ID: {}", id);
        validateId(id);

        Optional<User> userOpt = userDao.findById(id);
        if (userOpt.isPresent()) {
            userDao.delete(id);
            return true;
        }
        return false;
    }
}