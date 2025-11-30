package org.example.userservice.service;

import org.example.userservice.dao.UserDao;
import org.example.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao);
    }

    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        String name = "John Doe";
        String email = "john@test.com";
        Integer age = 25;

        User savedUser = new User(name, email, age);
        savedUser.setId(1L);

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(1L);

        // When
        Long userId = userService.createUser(name, email, age);

        // Then
        assertNotNull(userId);
        assertEquals(1L, userId);
        verify(userDao).findByEmail(email);
        verify(userDao).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        String name = "John Doe";
        String email = "existing@test.com";
        Integer age = 25;

        User existingUser = new User("Existing User", email, 30);
        when(userDao.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(name, email, age)
        );

        assertEquals("User with email " + email + " already exists", exception.getMessage());
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("", "test@test.com", 25)
        );

        assertEquals("Name cannot be empty", exception.getMessage());
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void shouldGetUserById() {
        // Given
        Long userId = 1L;
        User user = new User("John", "john@test.com", 25);
        user.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getName());
        verify(userDao).findById(userId);
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // Given
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertFalse(result.isPresent());
        verify(userDao).findById(userId);
    }

    @Test
    void shouldGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(
                new User("User1", "user1@test.com", 25),
                new User("User2", "user2@test.com", 30)
        );

        when(userDao.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        verify(userDao).findAll();
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        // Given
        Long userId = 1L;
        User existingUser = new User("Old Name", "old@test.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.findByEmail("new@test.com")).thenReturn(Optional.empty());

        // When
        boolean result = userService.updateUser(userId, "New Name", "new@test.com", 30);

        // Then
        assertTrue(result);
        verify(userDao).update(existingUser);
        assertEquals("New Name", existingUser.getName());
        assertEquals("new@test.com", existingUser.getEmail());
        assertEquals(30, existingUser.getAge());
    }

    @Test
    void shouldNotUpdateWhenUserNotFound() {
        // Given
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        // When
        boolean result = userService.updateUser(userId, "New Name", "new@test.com", 30);

        // Then
        assertFalse(result);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenNewEmailIsTaken() {
        // Given
        Long userId = 1L;
        User existingUser = new User("User1", "old@test.com", 25);
        existingUser.setId(userId);

        User otherUser = new User("User2", "taken@test.com", 30);
        otherUser.setId(2L);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.findByEmail("taken@test.com")).thenReturn(Optional.of(otherUser));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(userId, "New Name", "taken@test.com", 30)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // Given
        Long userId = 1L;
        User user = new User("To Delete", "delete@test.com", 25);
        user.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(user));

        // When
        boolean result = userService.deleteUser(userId);

        // Then
        assertTrue(result);
        verify(userDao).delete(userId);
    }

    @Test
    void shouldNotDeleteWhenUserNotFound() {
        // Given
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        // When
        boolean result = userService.deleteUser(userId);

        // Then
        assertFalse(result);
        verify(userDao, never()).delete(userId);
    }
}