package org.example.smartmuseum.model.entity;

import java.time.LocalDateTime;

/**
 * User chat session entity with node tracking
 */
public class UserChatSession {
    private int sessionId;
    private int userId;
    private LocalDateTime startTime;
    private LocalDateTime lastActivity;
    private boolean isActive;
    private String currentFlow;
    private int currentNodeId;
    private String sessionData;

    public UserChatSession() {
        this.startTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.isActive = true;
        this.currentFlow = "welcome";
        this.currentNodeId = 1; // Start at welcome node
    }

    public UserChatSession(int userId) {
        this();
        this.userId = userId;
    }

    public UserChatSession(int sessionId, int userId, int status, String currentFlow, String sessionData, boolean isActive) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.currentFlow = currentFlow;
        this.sessionData = sessionData;
        this.isActive = isActive;
        this.startTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.currentNodeId = 1;
    }

    public UserChatSession(int sessionId, int userId, String currentFlow) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.currentFlow = currentFlow;
        this.isActive = true;
        this.startTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.currentNodeId = 1;
    }

    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public boolean isSessionActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCurrentFlow() {
        return currentFlow;
    }

    public void setCurrentFlow(String currentFlow) {
        this.currentFlow = currentFlow;
    }

    public int getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(int currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public String getSessionData() {
        return sessionData;
    }

    public void setSessionData(String sessionData) {
        this.sessionData = sessionData;
    }

    /**
     * Update last activity timestamp
     */
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    /**
     * End the session
     */
    public void endSession() {
        this.isActive = false;
    }
}
