package org.example.smartmuseum.model.entity;

import org.example.smartmuseum.model.enums.NodeType;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

public class FlowNode {
    private int nodeId;
    private int flowId;
    private Integer parentNodeId;
    private NodeType nodeType;
    private String nodeText;
    private boolean isRoot;
    private int orderPosition;
    private List<NodeOption> options;
    private Timestamp createdAt;

    // Constructors
    public FlowNode() {
        this.options = new ArrayList<>();
    }

    public FlowNode(int nodeId, int flowId, Integer parentNodeId, NodeType nodeType, String nodeText, boolean isRoot, int orderPosition, Timestamp createdAt) {
        this.nodeId = nodeId;
        this.flowId = flowId;
        this.parentNodeId = parentNodeId;
        this.nodeType = nodeType;
        this.nodeText = nodeText;
        this.isRoot = isRoot;
        this.orderPosition = orderPosition;
        this.createdAt = createdAt;
        this.options = new ArrayList<>();
    }

    public String getNodeText() { return nodeText; }
    public List<NodeOption> getOptions() { return options; }
    public NodeType getNodeType() { return nodeType; }

    public void addOption(NodeOption option) {
        if (!options.contains(option)) {
            options.add(option);
        }
    }

    // Getters and Setters
    public int getNodeId() { return nodeId; }
    public void setNodeId(int nodeId) { this.nodeId = nodeId; }

    public int getFlowId() { return flowId; }
    public void setFlowId(int flowId) { this.flowId = flowId; }

    public Integer getParentNodeId() { return parentNodeId; }
    public void setParentNodeId(Integer parentNodeId) { this.parentNodeId = parentNodeId; }

    public void setNodeType(NodeType nodeType) { this.nodeType = nodeType; }

    public void setNodeText(String nodeText) { this.nodeText = nodeText; }

    public boolean isRoot() { return isRoot; }
    public void setRoot(boolean root) { isRoot = root; }

    public int getOrderPosition() { return orderPosition; }
    public void setOrderPosition(int orderPosition) { this.orderPosition = orderPosition; }

    public void setOptions(List<NodeOption> options) { this.options = options; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
