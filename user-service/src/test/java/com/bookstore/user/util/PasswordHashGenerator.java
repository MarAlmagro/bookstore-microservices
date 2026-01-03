package com.bookstore.user.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes for database initialization.
 * This is a test utility and should not be used in production code.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("=".repeat(70));
        System.out.println("BCrypt Password Hash Generator");
        System.out.println("=".repeat(70));
        System.out.println();

        // Generate hash for admin password
        String adminPassword = "admin123";
        String adminHash = encoder.encode(adminPassword);
        System.out.println("Password: " + adminPassword);
        System.out.println("BCrypt Hash: " + adminHash);
        System.out.println();

        // Generate hash for customer password
        String customerPassword = "customer123";
        String customerHash = encoder.encode(customerPassword);
        System.out.println("Password: " + customerPassword);
        System.out.println("BCrypt Hash: " + customerHash);
        System.out.println();

        // Verify the hashes work
        System.out.println("Verification:");
        System.out.println("admin123 matches: " + encoder.matches(adminPassword, adminHash));
        System.out.println("customer123 matches: " + encoder.matches(customerPassword, customerHash));
        System.out.println();
        System.out.println("=".repeat(70));
    }
}
