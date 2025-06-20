package org.example.smartmuseum.server;

import org.example.smartmuseum.model.service.ChatbotService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dedicated server for chatbot interactions
 */
public class ChatbotServer {

    private static final int CHATBOT_PORT = 8081;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private ChatbotService chatbotService;
    private boolean isRunning;

    public ChatbotServer() {
        this.threadPool = Executors.newFixedThreadPool(5);
        this.chatbotService = new ChatbotService();
        this.isRunning = false;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(CHATBOT_PORT);
            isRunning = true;
            System.out.println("Chatbot Server started on port " + CHATBOT_PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleChatClient(clientSocket));
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Chatbot server error: " + e.getMessage());
            }
        }
    }

    private void handleChatClient(Socket clientSocket) {
        // Handle chatbot-specific client interactions
        System.out.println("New chatbot client connected");
        // Implementation would handle chat flow here
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            threadPool.shutdown();
        } catch (IOException e) {
            System.err.println("Error stopping chatbot server: " + e.getMessage());
        }
    }
}