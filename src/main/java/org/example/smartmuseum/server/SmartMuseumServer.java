package org.example.smartmuseum.server;

import org.example.smartmuseum.model.service.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main server class for Smart Museum application
 */
public class SmartMuseumServer {

    private static final int PORT = 8080;
    private List<ClientHandler> clientHandlers;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean isRunning;

    // Services
    private UserService userService;
    private EmployeeService employeeService;
    private AuctionService auctionService;
    private ChatbotService chatbotService;

    public SmartMuseumServer() {
        this.clientHandlers = new ArrayList<>();
        this.threadPool = Executors.newFixedThreadPool(10);
        this.isRunning = false;

        // Initialize services
        this.userService = new UserService();
        this.employeeService = new EmployeeService();
        this.auctionService = new AuctionService();
        this.chatbotService = new ChatbotService();
    }

    /**
     * Start the server
     */
    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("Smart Museum Server started on port " + PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(
                        clientSocket,
                        userService,
                        employeeService,
                        auctionService,
                        chatbotService
                );

                clientHandlers.add(clientHandler);
                threadPool.execute(clientHandler);

                System.out.println("New client connected. Total clients: " + clientHandlers.size());
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Server error: " + e.getMessage());
            }
        } finally {
            stopServer();
        }
    }

    /**
     * Handle individual client connection
     * @param client Client socket
     */
    public void handleClient(Socket client) {
        ClientHandler handler = new ClientHandler(
                client,
                userService,
                employeeService,
                auctionService,
                chatbotService
        );
        threadPool.execute(handler);
    }

    /**
     * Broadcast update to all connected clients
     * @param event Event to broadcast
     */
    public void broadcastUpdate(String event) {
        System.out.println("Broadcasting to " + clientHandlers.size() + " clients: " + event);

        for (ClientHandler handler : clientHandlers) {
            handler.sendBroadcast(event);
        }
    }

    /**
     * Stop the server
     */
    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Close all client connections
            for (ClientHandler handler : clientHandlers) {
                handler.closeConnection();
            }

            threadPool.shutdown();
            System.out.println("Server stopped");
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    // Getters
    public List<ClientHandler> getClientHandlers() { return clientHandlers; }
    public UserService getUserService() { return userService; }
    public EmployeeService getEmployeeService() { return employeeService; }
    public AuctionService getAuctionService() { return auctionService; }
    public ChatbotService getChatbotService() { return chatbotService; }

    /**
     * Main method to start server
     */
    public static void main(String[] args) {
        SmartMuseumServer server = new SmartMuseumServer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stopServer));

        server.startServer();
    }
}
