package org.example.smartmuseum.util;

import javafx.stage.Stage;

/**
 * SessionContext untuk menyimpan dan pass SessionManager instance ke controllers
 * Ini akan memungkinkan setiap window memiliki session sendiri
 */
public class SessionContext {
    private final SessionManager sessionManager;
    private Stage stage;
    private String windowType; // "MAIN", "AUCTION", "BIDDER", etc.

    public SessionContext(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public SessionContext(SessionManager sessionManager, String windowType) {
        this.sessionManager = sessionManager;
        this.windowType = windowType;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        if (stage != null && sessionManager != null) {
            // Set window title di session manager
            stage.titleProperty().addListener((obs, oldTitle, newTitle) -> {
                sessionManager.setWindowTitle(newTitle);
            });
        }
    }

    public String getWindowType() {
        return windowType;
    }

    public void setWindowType(String windowType) {
        this.windowType = windowType;
    }

    /**
     * Create new window context untuk window baru
     */
    public static SessionContext createNewWindowContext(String windowType) {
        SessionManager session = SessionManager.createNewSession(windowType);
        return new SessionContext(session, windowType);
    }

    /**
     * Create context dari existing session (untuk child windows)
     */
    public static SessionContext fromExistingSession(SessionManager sessionManager, String windowType) {
        return new SessionContext(sessionManager, windowType);
    }

    /**
     * Cleanup saat window ditutup
     */
    public void cleanup() {
        if (sessionManager != null) {
            sessionManager.destroySession();
        }
    }

    @Override
    public String toString() {
        return String.format("SessionContext[windowType=%s, session=%s]",
                windowType, sessionManager != null ? sessionManager.toString() : "null");
    }
}