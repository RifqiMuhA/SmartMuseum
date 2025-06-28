package org.example.smartmuseum.util;

import org.example.smartmuseum.model.entity.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("Session updated for user: " + (user != null ? user.getUsername() : "null"));
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        System.out.println("User logged out: " + (currentUser != null ? currentUser.getUsername() : "null"));
        currentUser = null;
    }

    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole().getValue() : "guest";
    }

    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }

    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
}
