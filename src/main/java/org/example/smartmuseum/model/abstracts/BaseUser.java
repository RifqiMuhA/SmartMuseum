package org.example.smartmuseum.model.abstracts;

import org.example.smartmuseum.model.enums.UserRole;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Abstract base class for all user types
 */
public abstract class BaseUser {
    protected int userId;
    protected String username;
    protected String email;
    protected UserRole role;
    protected LocalDateTime createdAt;
    protected LocalDateTime lastLogin;
    protected boolean isActive;

    public BaseUser() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public BaseUser(int userId, String username) {
        this();
        this.userId = userId;
        this.username = username;
    }

    public BaseUser(int userId, String username, UserRole role) {
        this(userId, username);
        this.role = role;
    }

    // Abstract methods that must be implemented by subclasses
    public abstract String getDisplayName();
    public abstract boolean hasPermission(String permission);

    /**
     * Perform login operation
     * @param username Username
     * @param password Password
     * @return true if successful
     */
    public boolean login(String username, String password) {
        // Default implementation - can be overridden by concrete classes
        this.updateLastLogin();
        return true;
    }

    /**
     * Perform logout operation
     */
    public void logout() {
        // Default implementation - can be overridden by concrete classes
        System.out.println("User logged out: " + username);
    }

    /**
     * Update user profile
     * @param profile Profile data
     */
    public void updateProfile(Object profile) {
        // Default implementation - can be overridden by concrete classes
        System.out.println("Profile updated for user: " + username);
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
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "BaseUser{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                '}';
    }
}
