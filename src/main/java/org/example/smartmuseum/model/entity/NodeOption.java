package org.example.smartmuseum.model.entity;

/**
 * Node option entity for conversation choices
 */
public class NodeOption {
    private int optionId;
    private int nodeId;
    private String optionText;
    private String optionValue;
    private int nextNodeId;
    private int orderIndex;
    private boolean isActive;

    public NodeOption() {
        this.isActive = true;
    }

    public NodeOption(int nodeId, String optionText, String optionValue, int nextNodeId) {
        this();
        this.nodeId = nodeId;
        this.optionText = optionText;
        this.optionValue = optionValue;
        this.nextNodeId = nextNodeId;
    }

    // Getters and Setters
    public int getOptionId() {
        return optionId;
    }

    public void setOptionId(int optionId) {
        this.optionId = optionId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public int getNextNodeId() {
        return nextNodeId;
    }

    public void setNextNodeId(int nextNodeId) {
        this.nextNodeId = nextNodeId;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
