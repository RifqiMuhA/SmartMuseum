package org.example.smartmuseum.util;

import java.util.regex.Pattern;

/**
 * Validation utility functions
 */
public class ValidationHelper {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9]{10,15}$");

    /**
     * Validate email format
     * @param email Email to validate
     * @return true if valid email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format
     * @param phone Phone number to validate
     * @return true if valid phone format
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate username format
     * @param username Username to validate
     * @return true if valid username
     */
    public static boolean isValidUsername(String username) {
        return username != null &&
                username.length() >= 3 &&
                username.length() <= 50 &&
                username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Validate password strength
     * @param password Password to validate
     * @return true if password meets requirements
     */
    public static boolean isValidPassword(String password) {
        return password != null &&
                password.length() >= 6 &&
                password.length() <= 255;
    }

    /**
     * Validate required string field
     * @param value String value to validate
     * @param fieldName Field name for error messages
     * @return true if valid
     */
    public static boolean isValidRequiredString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            System.out.println(fieldName + " is required");
            return false;
        }
        return true;
    }
}