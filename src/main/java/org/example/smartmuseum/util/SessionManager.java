package org.example.smartmuseum.util;

import org.example.smartmuseum.model.entity.User;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * Enhanced SessionManager that supports multiple concurrent user sessions
 * Each window/stage can have its own session instance
 */
public class SessionManager {
    // Static registry untuk track semua active sessions
    private static final Map<String, SessionManager> activeSessions = new ConcurrentHashMap<>();

    // Instance variables untuk setiap session
    private final String sessionId;
    private User currentUser;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivity;
    private String windowTitle;

    // Private constructor untuk kontrol instansiasi
    private SessionManager(String sessionId) {
        this.sessionId = sessionId;
        this.lastActivity = LocalDateTime.now();
    }

    /**
     * Create new session instance untuk window baru
     */
    public static SessionManager createNewSession() {
        String sessionId = UUID.randomUUID().toString();
        SessionManager session = new SessionManager(sessionId);
        activeSessions.put(sessionId, session);
        System.out.println("New session created: " + sessionId);
        return session;
    }

    /**
     * Create new session dengan custom identifier
     */
    public static SessionManager createNewSession(String customId) {
        String sessionId = customId + "_" + UUID.randomUUID().toString().substring(0, 8);
        SessionManager session = new SessionManager(sessionId);
        activeSessions.put(sessionId, session);
        System.out.println("New session created with custom ID: " + sessionId);
        return session;
    }

    /**
     * Get session by ID (untuk debugging/monitoring)
     */
    public static SessionManager getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * Get all active sessions (untuk monitoring)
     */
    public static Map<String, SessionManager> getAllActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }

    /**
     * Login user ke session ini
     */
    public void login(User user) {
        this.currentUser = user;
        this.loginTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        System.out.println("User logged in - Session: " + sessionId + ", User: " +
                (user != null ? user.getUsername() : "null"));
    }

    /**
     * Set current user (untuk update profile dll)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.lastActivity = LocalDateTime.now();
        System.out.println("Session updated - Session: " + sessionId + ", User: " +
                (user != null ? user.getUsername() : "null"));
    }

    /**
     * Get current user dari session ini
     */
    public User getCurrentUser() {
        updateLastActivity();
        return currentUser;
    }

    /**
     * Check apakah user sudah login di session ini
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Logout user dari session ini
     */
    public void logout() {
        System.out.println("User logged out - Session: " + sessionId + ", User: " +
                (currentUser != null ? currentUser.getUsername() : "null"));
        currentUser = null;
        loginTime = null;
    }

    /**
     * Destroy session dan remove dari registry
     */
    public void destroySession() {
        if (currentUser != null) {
            logout();
        }
        activeSessions.remove(sessionId);
        System.out.println("Session destroyed: " + sessionId);
    }

    /**
     * Get current user role
     */
    public String getCurrentUserRole() {
        updateLastActivity();
        return currentUser != null ? currentUser.getRole().getValue() : "guest";
    }

    /**
     * Get current username
     */
    public String getCurrentUsername() {
        updateLastActivity();
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }

    /**
     * Get current user ID
     */
    public int getCurrentUserId() {
        updateLastActivity();
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    /**
     * Get session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Get login time
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * Get last activity time
     */
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    /**
     * Update last activity timestamp
     */
    private void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    /**
     * Set window title untuk identifying session
     */
    public void setWindowTitle(String title) {
        this.windowTitle = title;
    }

    /**
     * Get window title
     */
    public String getWindowTitle() {
        return windowTitle != null ? windowTitle : "Unknown Window";
    }

    /**
     * Check if user dengan username tertentu sudah login di session manapun
     */
    public static boolean isUserLoggedInAnywhere(String username) {
        return activeSessions.values().stream()
                .anyMatch(session -> session.isLoggedIn() &&
                        session.getCurrentUsername().equals(username));
    }

    /**
     * Get session where specific user is logged in
     */
    public static SessionManager getSessionByUsername(String username) {
        return activeSessions.values().stream()
                .filter(session -> session.isLoggedIn() &&
                        session.getCurrentUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get count of active sessions
     */
    public static int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Get count of logged in users
     */
    public static int getLoggedInUserCount() {
        return (int) activeSessions.values().stream()
                .filter(SessionManager::isLoggedIn)
                .count();
    }

    /**
     * Cleanup inactive sessions (bisa dipanggil periodically)
     */
    public static void cleanupInactiveSessions(int maxInactiveMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(maxInactiveMinutes);

        activeSessions.entrySet().removeIf(entry -> {
            SessionManager session = entry.getValue();
            if (session.lastActivity != null && session.lastActivity.isBefore(cutoff)) {
                System.out.println("Removing inactive session: " + entry.getKey());
                return true;
            }
            return false;
        });
    }

    @Override
    public String toString() {
        return String.format("Session[id=%s, user=%s, loginTime=%s, windowTitle=%s]",
                sessionId.substring(0, 8) + "...",
                getCurrentUsername(),
                loginTime,
                windowTitle);
    }
}