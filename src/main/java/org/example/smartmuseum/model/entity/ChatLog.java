package org.example.smartmuseum.model.entity;

import java.time.LocalDateTime;

/**
 * Chat log entity for storing chat messages with node tracking
 */
public class ChatLog {
    private int logId;
    private int sessionId;
    private String messageText;
    private boolean isUserMessage;
    private LocalDateTime timestamp;
    private String messageType;
    private Integer nodeId; // Track which node generated this message

    public ChatLog() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatLog(int sessionId, String messageText, boolean isUserMessage) {
        this();
        this.sessionId = sessionId;
        this.messageText = messageText;
        this.isUserMessage = isUserMessage;
        this.messageType = isUserMessage ? "USER" : "BOT";
    }

    public ChatLog(int sessionId, String messageText, boolean isUserMessage, Integer nodeId) {
        this(sessionId, messageText, isUserMessage);
        this.nodeId = nodeId;
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public void setUserMessage(boolean userMessage) {
        isUserMessage = userMessage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }
}
