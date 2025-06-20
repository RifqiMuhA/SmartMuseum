package org.example.smartmuseum.model.service;

import org.example.smartmuseum.model.entity.ConversationFlow;
import org.example.smartmuseum.model.entity.FlowNode;
import org.example.smartmuseum.model.entity.NodeOption;
import org.example.smartmuseum.model.entity.UserChatSession;
import org.example.smartmuseum.model.enums.NodeType;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatbotEngine {
    private ConcurrentMap<Integer, ConversationFlow> activeFlows;
    private ConcurrentMap<Integer, UserChatSession> activeSessions;

    public ChatbotEngine() {
        this.activeFlows = new ConcurrentHashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
    }

    public String processUserInput(int userId, String input) {
        UserChatSession session = getOrCreateSession(userId);

        try {
            int option = Integer.parseInt(input.trim());
            FlowNode targetNode = navigateToOption(session, option);

            if (targetNode != null) {
                session.navigateToNode(targetNode.getNodeId());
                return generateMenuResponse(targetNode);
            } else {
                return "Pilihan tidak valid. Silakan pilih nomor yang tersedia.";
            }
        } catch (NumberFormatException e) {
            return "Mohon masukkan nomor pilihan yang valid.";
        }
    }

    public String generateMenuResponse(FlowNode node) {
        StringBuilder response = new StringBuilder();
        response.append(node.getNodeText()).append("\n\n");

        if (node.getNodeType() == NodeType.MENU) {
            response.append("Pilihan tersedia:\n");
            for (NodeOption option : node.getOptions()) {
                if (option.isActive()) {
                    response.append(option.getOptionNumber())
                            .append(". ")
                            .append(option.getOptionText())
                            .append("\n");
                }
            }
            response.append("0. Kembali\n99. Menu Utama\n");
            response.append("\nKetik nomor pilihan Anda:");
        }

        return response.toString();
    }

    public FlowNode navigateToOption(UserChatSession session, int optionNumber) {
        // In real implementation, would load current node from database
        // For now, return a mock node
        FlowNode mockNode = new FlowNode();
        mockNode.setNodeId(optionNumber);
        mockNode.setNodeType(NodeType.MENU);
        mockNode.setNodeText("Mock response for option " + optionNumber);
        return mockNode;
    }

    public UserChatSession initializeSession(int userId) {
        UserChatSession session = new UserChatSession();
        session.setUserId(userId);
        session.setCurrentNodeId(1); // Root node
        session.setActive(true);

        activeSessions.put(userId, session);
        return session;
    }

    public UserChatSession getOrCreateSession(int userId) {
        return activeSessions.computeIfAbsent(userId, k -> initializeSession(userId));
    }

    public void loadFlows() {
        // In real implementation, would load from database
        ConversationFlow mainFlow = new ConversationFlow();
        mainFlow.setFlowId(1);
        mainFlow.setFlowName("Main Flow");
        mainFlow.setActive(true);

        activeFlows.put(1, mainFlow);
        System.out.println("Loaded conversation flows");
    }

    public ConcurrentMap<Integer, ConversationFlow> getActiveFlows() {
        return activeFlows;
    }

    public ConcurrentMap<Integer, UserChatSession> getActiveSessions() {
        return activeSessions;
    }
}