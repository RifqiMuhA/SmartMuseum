package org.example.smartmuseum.model.interfaces;

/**
 * Interface defining core user actions available in the system
 */
public interface UserActions {
    /**
     * Authenticate user with username and password
     * @param username User's username
     * @param password User's password
     * @return true if authentication successful, false otherwise
     */
    boolean login(String username, String password);

    /**
     * Log out the current user
     */
    void logout();

    /**
     * Update user profile information
     * @param profile Updated profile data
     */
    void updateProfile(Object profile);
}