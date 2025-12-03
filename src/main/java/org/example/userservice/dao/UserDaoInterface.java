package org.example.userservice.dao;

import org.example.userservice.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDaoInterface {
    Long save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    void update(User user);
    void delete(Long id);
    Optional<User> findByEmail(String email);
}
