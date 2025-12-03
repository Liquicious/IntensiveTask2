package org.example.userservice.util;

import org.example.userservice.dao.UserDaoImpl;
import org.example.userservice.model.User;

public class DebugSessionFactory {
    public static void main(String[] args) {
        System.out.println("=== Test 1: Normal flow ===");
        testNormalFlow();

        System.out.println("\n=== Test 2: Closed factory ===");
        testClosedFactory();

        System.out.println("\n=== Test 3: Multiple restarts ===");
        testMultipleRestarts();
    }

    static void testNormalFlow() {
        try {
            UserDaoImpl dao = new UserDaoImpl();
            dao.save(new User("Normal", "normal@test.com", 25));
            System.out.println("✓ Normal flow works");
        } catch (Exception e) {
            System.out.println("✗ Normal flow failed: " + e.getMessage());
        }
    }

    static void testClosedFactory() {
        HibernateUtil.shutdown();

        try {
            UserDaoImpl dao = new UserDaoImpl();
            dao.save(new User("Closed", "closed@test.com", 30));
            System.out.println("✗ Should have failed but didn't!");
        } catch (Exception e) {
            System.out.println("✓ Got expected error: " + e.getMessage());
        }

        HibernateUtil.resetSessionFactory();
    }

    static void testMultipleRestarts() {
        for (int i = 1; i <= 3; i++) {
            System.out.println("Attempt " + i + ":");
            testNormalFlow();
            HibernateUtil.shutdown();
        }
    }
}