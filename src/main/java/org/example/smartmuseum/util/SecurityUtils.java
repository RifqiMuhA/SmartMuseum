package org.example.smartmuseum.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security utility functions for password hashing and validation
 */
public class SecurityUtils {

    private static final String ALGORITHM = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hash password with salt
     * @param password Plain text password
     * @param salt Salt for hashing
     * @return Hashed password
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Generate random salt
     * @return Random salt string
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Verify password against hash
     * @param password Plain text password
     * @param salt Salt used for original hashing
     * @param hash Stored hash
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String salt, String hash) {
        String computedHash = hashPassword(password, salt);
        return computedHash.equals(hash);
    }

    /**
     * Generate simple hash for demo purposes
     * @param password Password to hash
     * @return Simple hash
     */
    public static String simpleHash(String password) {
        return "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSekGkcSs6WDOQZW.p6.9Hm6G"; // Mock BCrypt hash
    }
}
