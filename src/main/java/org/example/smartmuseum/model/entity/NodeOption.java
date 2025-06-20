package org.example.smartmuseum.model.entity;

public class NodeOption {
    private int optionId;
    private int nodeId;
    private int optionNumber;
    private String optionText;
    private int targetNodeId;
    private boolean isActive;

    // Constructors
    public NodeOption() {}

    public NodeOption(int optionId, int nodeId, int optionNumber, String optionText, int targetNodeId, boolean isActive) {
        this.optionId = optionId;
        this.nodeId = nodeId;
        this.optionNumber = optionNumber;
        this.optionText = optionText;
        this.targetNodeId = targetNodeId;
        this.isActive = isActive;
    }

    public int getOptionNumber() { return optionNumber; }
    public String getOptionText() { return optionText; }
    public FlowNode getTargetNode() {
        // This would typically load from database
        return null; // Placeholder
    }

    // Getters and Setters
    public int getOptionId() { return optionId; }
    public void setOptionId(int optionId) { this.optionId = optionId; }

    public int getNodeId() { return nodeId; }
    public void setNodeId(int nodeId) { this.nodeId = nodeId; }

    public void setOptionNumber(int optionNumber) { this.optionNumber = optionNumber; }

    public void setOptionText(String optionText) { this.optionText = optionText; }

    public int getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(int targetNodeId) { this.targetNodeId = targetNodeId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
