package org.example.userservice.dao;

import org.example.userservice.model.User;
import org.example.userservice.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    public Long save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.persist(user);

            transaction.commit();
            logger.info("User saved successfully: Name-{}, Email-{}", user.getName(), user.getEmail());
            return user.getId();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Error saving user: {}", e.getMessage());
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public Optional<User> findById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User user = session.find(User.class, id);

            transaction.commit();
            return Optional.ofNullable(user);
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Error finding user by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to find user", e);
        }
    }

    public List<User> findAll() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            List<User> users = session.createQuery("FROM User", User.class).list();

            transaction.commit();
            return users;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Error finding all users: {}", e.getMessage());
            throw new RuntimeException("Failed to find users", e);
        }
    }

    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.merge(user);

            transaction.commit();
            logger.info("User updated successfully: {}", user.getEmail());
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Error updating user: {}", e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        }
    }

    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User user = session.find(User.class, id);
            if (user != null) {
                session.remove(user);
                logger.info("User deleted successfully: {}", id);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Error deleting user {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            Optional<User> user = query.uniqueResultOptional();

            transaction.commit();
            return user;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Error finding user by email: {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to find user by email", e);
        }
    }
}