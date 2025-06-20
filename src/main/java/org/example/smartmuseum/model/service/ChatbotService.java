package org.example.smartmuseum.model.service;

import org.example.smartmuseum.model.entity.UserChatSession;
import org.example.smartmuseum.model.entity.ConversationFlow;
import org.example.smartmuseum.model.service.ChatbotEngine;
import java.util.List;
import java.util.ArrayList;

public class ChatbotService {
    private ChatbotEngine chatbotEngine;

    public ChatbotService() {
        this.chatbotEngine = new ChatbotEngine();
        loadConversationFlows();
    }

    public String processUserInput(int userId, String input) {
        return chatbotEngine.processUserInput(userId, input);
    }

    public UserChatSession initializeChatSession(int userId) {
        return chatbotEngine.initializeSession(userId);
    }

    public void loadConversationFlows() {
        chatbotEngine.loadFlows();
        System.out.println("Conversation flows loaded");
    }

    public List<String> getSessionHistory(int sessionId) {
        // In real implementation, would load from database
        return new ArrayList<>();
    }

    public ChatbotEngine getChatbotEngine() {
        return chatbotEngine;
    }
}
