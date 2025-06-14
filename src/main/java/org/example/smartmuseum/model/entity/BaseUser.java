package org.example.smartmuseum.model.entity;

import org.example.smartmuseum.model.interfaces.UserActions;
import org.example.smartmuseum.model.interfaces.SystemObserver;
import org.example.smartmuseum.model.enums.UserRole;
import java.time.LocalDateTime;
import java.util.List;

public abstract class BaseUser implements UserActions, SystemObserver {
    protected int userId;
    protected String username;
    protected String email;
    protected String phone;
    protected UserRole role;
    protected boolean isActive;
    protected LocalDateTime createdAt;

    // Constructors
    public BaseUser() {}

    public BaseUser(int userId, String username, String email, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Template method for login process
    public final boolean performLogin(String username, String password) {
        if (validateCredentials(username, password)) {
            initializeSession();
            System.out.println("Login successful for: " + username);
            return true;
        }
        System.out.println("Login failed for: " + username);
        return false;
    }

    // Abstract methods to be implemented by subclasses
    public abstract void displayDashboard();
    public abstract List<String> getAvailableActions();
    protected abstract void initializeSession();

    // Common validation logic
    protected boolean validateCredentials(String username, String password) {
        return username != null && !username.trim().isEmpty() &&
                password != null && password.length() >= 4;
    }

    // Default implementation of UserActions
    @Override
    public boolean login(String username, String password) {
        return performLogin(username, password);
    }

    @Override
    public void logout() {
        System.out.println("User " + username + " logged out");
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword) {
        // Implementation would validate old password and update new one
        return newPassword != null && newPassword.length() >= 4;
    }

    @Override
    public void updateProfile(Object profile) {
        System.out.println("Profile updated for user: " + username);
    }

    // Default implementation of SystemObserver
    @Override
    public void onBidPlaced(Object event) {
        // Default implementation - can be overridden
    }

    @Override
    public void onMessageReceived(Object event) {
        System.out.println("Message received by: " + username);
    }

    @Override
    public void onAttendanceChanged(Object event) {
        System.out.println("Attendance change notification for: " + username);
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role=%s}",
                userId, username, role);
    }
}