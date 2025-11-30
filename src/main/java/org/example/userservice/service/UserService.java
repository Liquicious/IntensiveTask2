package org.example.userservice.service;

import org.example.userservice.dao.UserDao;
import org.example.userservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Long createUser(String name, String email, Integer age) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        Optional<User> existingUser = userDao.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        User user = new User(name, email, age);
        return userDao.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userDao.findById(id);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public boolean updateUser(Long id, String name, String email, Integer age) {
        Optional<User> userOpt = userDao.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (email != null && !email.equals(user.getEmail())) {
                Optional<User> existingUser = userDao.findByEmail(email);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Email already exists");
                }
                user.setEmail(email);
            }

            if (name != null) user.setName(name);
            if (age != null) user.setAge(age);

            userDao.update(user);
            return true;
        }
        return false;
    }

    public boolean deleteUser(Long id) {
        Optional<User> userOpt = userDao.findById(id);
        if (userOpt.isPresent()) {
            userDao.delete(id);
            return true;
        }
        return false;
    }
}