package org.example.smartmuseum.model.entity;

import java.sql.Timestamp;

public class ConversationFlow {
    private int flowId;
    private String flowName;
    private String description;
    private boolean isActive;
    private FlowNode rootNode;
    private Timestamp createdAt;

    // Constructors
    public ConversationFlow() {}

    public ConversationFlow(int flowId, String flowName, String description, boolean isActive, Timestamp createdAt) {
        this.flowId = flowId;
        this.flowName = flowName;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public String getName() { return flowName; }
    public FlowNode getRootNode() { return rootNode; }
    public boolean isActive() { return isActive; }

    // Getters and Setters
    public int getFlowId() { return flowId; }
    public void setFlowId(int flowId) { this.flowId = flowId; }

    public String getFlowName() { return flowName; }
    public void setFlowName(String flowName) { this.flowName = flowName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public void setActive(boolean active) { isActive = active; }

    public void setRootNode(FlowNode rootNode) { this.rootNode = rootNode; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
