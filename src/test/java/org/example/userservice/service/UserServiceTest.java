package org.example.userservice.service;

import org.example.userservice.dao.UserDao;
import org.example.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    // ============ CREATE USER TESTS ============

    @Test
    void shouldCreateUserSuccessfully() {
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 25;

        User savedUser = new User(name, email, age);
        savedUser.setId(1L);

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(1L);

        Long userId = userService.createUser(name, email, age);

        assertNotNull(userId);
        assertEquals(1L, userId);
        verify(userDao).findByEmail(email);
        verify(userDao).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        String name = "John Doe";
        String email = "existing@example.com";
        Integer age = 25;

        User existingUser = new User("Existing User", email, 30);
        when(userDao.findByEmail(email)).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(name, email, age)
        );

        assertEquals("User with email " + email + " already exists", exception.getMessage());
        verify(userDao, never()).save(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    void shouldThrowExceptionWhenNameIsEmpty(String invalidName) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidName, "test@example.com", 25)
        );

        assertEquals("Name cannot be empty", exception.getMessage());
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(null, "test@example.com", 25)
        );

        assertEquals("Name cannot be empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNameIsTooShort() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("J", "test@example.com", 25)
        );

        assertTrue(exception.getMessage().contains("Name must contain between 2 and 100 characters"));
    }

    @Test
    void shouldThrowExceptionWhenNameIsTooLong() {
        String longName = "A".repeat(101);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(longName, "test@example.com", 25)
        );

        assertTrue(exception.getMessage().contains("Name must contain between 2 and 100 characters"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"John123", "John@Doe", "John_Doe", "John123 Doe"})
    void shouldThrowExceptionWhenNameContainsInvalidCharacters(String invalidName) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidName, "test@example.com", 25)
        );

        assertTrue(exception.getMessage().contains("Name can only contain letters, spaces, hyphens and apostrophes"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    void shouldThrowExceptionWhenEmailIsEmpty(String invalidEmail) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John Doe", invalidEmail, 25)
        );

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John Doe", null, 25)
        );

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "@domain.com", "user@", "user@.com",
            "user@domain."})
    void shouldThrowExceptionWhenEmailFormatIsInvalid(String invalidEmail) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John Doe", invalidEmail, 25)
        );

        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsTooLong() {
        String longEmail = "user@" + "a".repeat(250) + ".com";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John Doe", longEmail, 25)
        );

        assertEquals("Email is too long. Maximum length is 255 characters", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAgeIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "john@example.com", null)
        );

        assertEquals("Age cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAgeIsNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "john@example.com", -5)
        );

        assertEquals("Age cannot be negative. Received: -5", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAgeExceedsMaximum() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "john@example.com", 121)
        );

        assertEquals("Age cannot exceed 120 years. Received: 121", exception.getMessage());
    }

    @Test
    void shouldAcceptValidAgeBoundaries() {
        String name = "John Doe";

        when(userDao.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(1L);

        // Test minimum age
        assertDoesNotThrow(() -> userService.createUser(name, "test1@example.com", 0));

        // Test maximum age
        assertDoesNotThrow(() -> userService.createUser(name, "test2@example.com", 120));
    }

    // ============ GET USER BY ID TESTS ============

    @Test
    void shouldGetUserByIdSuccessfully() {
        Long userId = 1L;
        User user = new User("John Doe", "john@example.com", 25);
        user.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        assertEquals("john@example.com", result.get().getEmail());
        assertEquals(25, result.get().getAge());
        verify(userDao).findById(userId);
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(userId);

        assertFalse(result.isPresent());
        verify(userDao).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(null)
        );

        assertEquals("ID cannot be null", exception.getMessage());
        verify(userDao, never()).findById(any());
    }

    @Test
    void shouldThrowExceptionWhenIdIsZeroOrNegative() {
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(0L)
        );
        assertEquals("ID must be a positive number", exception1.getMessage());

        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(-1L)
        );
        assertEquals("ID must be a positive number", exception2.getMessage());
    }

    // ============ GET ALL USERS TESTS ============

    @Test
    void shouldGetAllUsersSuccessfully() {
        List<User> users = Arrays.asList(
                new User("User1", "user1@example.com", 25),
                new User("User2", "user2@example.com", 30)
        );

        when(userDao.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userDao).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        when(userDao.findAll()).thenReturn(List.of());

        List<User> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
        verify(userDao).findAll();
    }

    // ============ UPDATE USER TESTS ============

    @Test
    void shouldUpdateUserSuccessfully() {
        Long userId = 1L;
        User existingUser = new User("Old Name", "old@example.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.findByEmail("new@example.com")).thenReturn(Optional.empty());

        boolean result = userService.updateUser(userId, "New Name", "new@example.com", 30);

        assertTrue(result);
        verify(userDao).update(existingUser);
        assertEquals("New Name", existingUser.getName());
        assertEquals("new@example.com", existingUser.getEmail());
        assertEquals(30, existingUser.getAge());
    }

    @Test
    void shouldUpdateOnlyNameWhenEmailIsNull() {
        Long userId = 1L;
        User existingUser = new User("Old Name", "old@example.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));

        boolean result = userService.updateUser(userId, "New Name", null, null);

        assertTrue(result);
        assertEquals("New Name", existingUser.getName());
        assertEquals("old@example.com", existingUser.getEmail());
        assertEquals(25, existingUser.getAge());
    }

    @Test
    void shouldUpdateOnlyAgeWhenOtherFieldsAreNull() {
        Long userId = 1L;
        User existingUser = new User("John Doe", "john@example.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));

        boolean result = userService.updateUser(userId, null, null, 30);

        assertTrue(result);
        assertEquals("John Doe", existingUser.getName());
        assertEquals("john@example.com", existingUser.getEmail());
        assertEquals(30, existingUser.getAge());
    }

    @Test
    void shouldNotUpdateWhenEmailIsSame() {
        Long userId = 1L;
        User existingUser = new User("John Doe", "john@example.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));

        boolean result = userService.updateUser(userId, "New Name", "john@example.com", 30);

        assertTrue(result);
        verify(userDao, never()).findByEmail(anyString());
        assertEquals("New Name", existingUser.getName());
        assertEquals("john@example.com", existingUser.getEmail());
        assertEquals(30, existingUser.getAge());
    }

    @Test
    void shouldThrowExceptionWhenNewEmailIsTakenByAnotherUser() {
        Long userId = 1L;
        User existingUser = new User("User1", "old@example.com", 25);
        existingUser.setId(userId);

        User otherUser = new User("User2", "taken@example.com", 30);
        otherUser.setId(2L);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDao.findByEmail("taken@example.com")).thenReturn(Optional.of(otherUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(userId, "New Name", "taken@example.com", 30)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void shouldNotUpdateWhenUserNotFound() {
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        boolean result = userService.updateUser(userId, "New Name", "new@example.com", 30);

        assertFalse(result);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithInvalidName() {
        Long userId = 1L;
        User existingUser = new User("Valid Name", "valid@example.com", 25);
        existingUser.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(userId, "J", null, null) // too short name
        );

        assertTrue(exception.getMessage().contains("Name must contain between 2 and 100 characters"));
        verify(userDao, never()).update(any(User.class));
    }

    // ============ DELETE USER TESTS ============

    @Test
    void shouldDeleteUserSuccessfully() {
        Long userId = 1L;
        User user = new User("To Delete", "delete@example.com", 25);
        user.setId(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(user));

        boolean result = userService.deleteUser(userId);

        assertTrue(result);
        verify(userDao).delete(userId);
    }

    @Test
    void shouldNotDeleteWhenUserNotFound() {
        Long userId = 999L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        boolean result = userService.deleteUser(userId);

        assertFalse(result);
        verify(userDao, never()).delete(userId);
    }

    @Test
    void shouldThrowExceptionWhenDeleteWithInvalidId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUser(0L)
        );

        assertEquals("ID must be a positive number", exception.getMessage());
        verify(userDao, never()).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenDeleteWithNullId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUser(null)
        );

        assertEquals("ID cannot be null", exception.getMessage());
        verify(userDao, never()).delete(any());
    }
}