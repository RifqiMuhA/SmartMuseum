package org.example.smartmuseum.model.entity;

/**
 * Flow node entity representing conversation nodes
 */
public class FlowNode {
    private int nodeId;
    private int flowId;
    private String nodeType;
    private String nodeContent;
    private String nodeKey;
    private int parentNodeId;
    private boolean isActive;

    public FlowNode() {
        this.isActive = true;
    }

    public FlowNode(int flowId, String nodeType, String nodeContent, String nodeKey) {
        this();
        this.flowId = flowId;
        this.nodeType = nodeType;
        this.nodeContent = nodeContent;
        this.nodeKey = nodeKey;
    }

    // Getters and Setters
    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeContent() {
        return nodeContent;
    }

    public void setNodeContent(String nodeContent) {
        this.nodeContent = nodeContent;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public int getParentNodeId() {
        return parentNodeId;
    }

    public void setParentNodeId(int parentNodeId) {
        this.parentNodeId = parentNodeId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
