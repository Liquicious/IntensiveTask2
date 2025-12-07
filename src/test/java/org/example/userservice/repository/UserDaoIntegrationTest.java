//package org.example.userservice.repository;
//
//import org.example.userservice.entity.User;
//import org.example.userservice.util.HibernateUtil;
//import org.hibernate.SessionFactory;
//import org.junit.jupiter.api.*;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.MountableFile;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Testcontainers
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class UserDaoIntegrationTest {
//
//    @Container
//    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
//            .withDatabaseName("testdb")
//            .withUsername("test")
//            .withPassword("test")
//            .withCopyFileToContainer(
//                    MountableFile.forClasspathResource("test-init.sql"),
//                    "/docker-entrypoint-initdb.d/test-init.sql"
//            );
//
//    private UserDao userDao;
//    private SessionFactory sessionFactory;
//
//    @BeforeAll
//    void setup() {
//        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
//        System.setProperty("hibernate.connection.username", postgres.getUsername());
//        System.setProperty("hibernate.connection.password", postgres.getPassword());
//
//        sessionFactory = HibernateUtil.getSessionFactory();
//        userDao = new UserDaoImpl();
//    }
//
//    @BeforeEach
//    void cleanDatabase() {
//        try (var session = sessionFactory.openSession()) {
//            var transaction = session.beginTransaction();
//            session.createMutationQuery("DELETE FROM User").executeUpdate();
//            transaction.commit();
//        }
//    }
//
//    @AfterAll
//    void tearDown() {
//        if (sessionFactory != null) {
//            sessionFactory.close();
//        }
//    }
//
//    @Test
//    void shouldSaveUser() {
//        User user = new User("John Doe", "john@test.com", 25);
//
//        Long userId = userDao.save(user);
//
//        assertNotNull(userId);
//        assertTrue(userId > 0);
//    }
//
//    @Test
//    void shouldFindUserById() {
//        User user = new User("Jane Doe", "jane@test.com", 30);
//        Long userId = userDao.save(user);
//
//        Optional<User> foundUser = userDao.findById(userId);
//
//        assertTrue(foundUser.isPresent());
//        assertEquals("Jane Doe", foundUser.get().getName());
//        assertEquals("jane@test.com", foundUser.get().getEmail());
//        assertEquals(30, foundUser.get().getAge());
//    }
//
//    @Test
//    void shouldReturnEmptyWhenUserNotFound() {
//        Optional<User> foundUser = userDao.findById(999L);
//
//        assertFalse(foundUser.isPresent());
//    }
//
//    @Test
//    void shouldFindAllUsers() {
//        userDao.save(new User("User1", "user1@test.com", 25));
//        userDao.save(new User("User2", "user2@test.com", 30));
//
//        List<User> users = userDao.findAll();
//
//        assertEquals(2, users.size());
//    }
//
//    @Test
//    void shouldUpdateUser() {
//        User user = new User("Old Name", "old@test.com", 25);
//        Long userId = userDao.save(user);
//
//        User userToUpdate = new User("New Name", "new@test.com", 30);
//        userToUpdate.setId(userId);
//
//        userDao.update(userToUpdate);
//
//        Optional<User> updatedUser = userDao.findById(userId);
//        assertTrue(updatedUser.isPresent());
//        assertEquals("New Name", updatedUser.get().getName());
//        assertEquals("new@test.com", updatedUser.get().getEmail());
//        assertEquals(30, updatedUser.get().getAge());
//    }
//
//    @Test
//    void shouldDeleteUser() {
//        User user = new User("To Delete", "delete@test.com", 25);
//        Long userId = userDao.save(user);
//
//        userDao.delete(userId);
//
//        Optional<User> deletedUser = userDao.findById(userId);
//        assertFalse(deletedUser.isPresent());
//    }
//
//    @Test
//    void shouldFindUserByEmail() {
//        User user = new User("Email User", "email@test.com", 25);
//        userDao.save(user);
//
//        Optional<User> foundUser = userDao.findByEmail("email@test.com");
//
//        assertTrue(foundUser.isPresent());
//        assertEquals("Email User", foundUser.get().getName());
//    }
//}