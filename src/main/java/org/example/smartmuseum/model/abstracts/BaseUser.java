package org.example.smartmuseum.model.abstracts;

import org.example.smartmuseum.model.interfaces.UserActions;
import org.example.smartmuseum.model.enums.UserRole;
import java.util.List;

/**
 * Abstract base class for all user types
 */
public abstract class BaseUser implements UserActions {
    protected int userId;
    protected String username;
    protected UserRole role;

    public BaseUser(int userId, String username, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    /**
     * Perform login operation
     * @param username Username
     * @param password Password
     * @return true if successful
     */
    public boolean performLogin(String username, String password) {
        // Implementation will be handled by concrete classes
        return login(username, password);
    }

    /**
     * Display dashboard specific to user role
     */
    public abstract void displayDashboard();

    /**
     * Get available actions for this user type
     * @return List of available actions
     */
    public abstract List<String> getAvailableActions();

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}