package org.example.smartmuseum.model.entity;

/**
 * Conversation flow entity
 */
public class ConversationFlow {
    private int flowId;
    private String flowName;
    private String description;
    private boolean isActive;
    private int startNodeId;

    public ConversationFlow() {
        this.isActive = true;
    }

    public ConversationFlow(String flowName, String description) {
        this();
        this.flowName = flowName;
        this.description = description;
    }

    // Getters and Setters
    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(int startNodeId) {
        this.startNodeId = startNodeId;
    }
}
