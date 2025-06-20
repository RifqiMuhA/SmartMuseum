package org.example.smartmuseum.model.entity;

import java.sql.Timestamp;

public class UserChatSession {
    private int sessionId;
    private int userId;
    private int currentNodeId;
    private String sessionData;
    private Timestamp lastActivity;
    private boolean isActive;

    // Constructors
    public UserChatSession() {}

    public UserChatSession(int sessionId, int userId, int currentNodeId, String sessionData, Timestamp lastActivity, boolean isActive) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.currentNodeId = currentNodeId;
        this.sessionData = sessionData;
        this.lastActivity = lastActivity != null ? lastActivity : new Timestamp(System.currentTimeMillis());
        this.isActive = isActive;
    }

    public FlowNode getCurrentNode() {
        // This would typically load from database
        return null; // Placeholder
    }

    public void navigateToNode(int nodeId) {
        this.currentNodeId = nodeId;
        this.lastActivity = new Timestamp(System.currentTimeMillis());
    }

    public void updateSessionData(String data) {
        this.sessionData = data;
        this.lastActivity = new Timestamp(System.currentTimeMillis());
    }

    public boolean isSessionActive() { return isActive; }

    // Getters and Setters
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCurrentNodeId() { return currentNodeId; }
    public void setCurrentNodeId(int currentNodeId) { this.currentNodeId = currentNodeId; }

    public String getSessionData() { return sessionData; }
    public void setSessionData(String sessionData) { this.sessionData = sessionData; }

    public Timestamp getLastActivity() { return lastActivity; }
    public void setLastActivity(Timestamp lastActivity) { this.lastActivity = lastActivity; }

    public void setActive(boolean active) { isActive = active; }
}