package org.example.smartmuseum.server;

import org.example.smartmuseum.controller.AuctionSocketController;
import org.example.smartmuseum.model.service.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced Smart Museum Server with auction socket integration
 * Handles both general museum operations and real-time auction functionality
 */
public class SmartMuseumServer {

    private static final int MAIN_PORT = 8080;
    private static final int AUCTION_PORT = 8082;

    private List<ClientHandler> clientHandlers;
    private ServerSocket mainServerSocket;
    private ExecutorService threadPool;
    private boolean isRunning;

    // Services
    private UserService userService;
    private EmployeeService employeeService;
    private AuctionService auctionService;
    private ChatbotService chatbotService;

    // Auction Components
    private AuctionSocketController auctionSocketController;
    private Thread auctionServerThread;

    public SmartMuseumServer() {
        this.clientHandlers = new ArrayList<>();
        this.threadPool = Executors.newFixedThreadPool(15); // Increased for auction support
        this.isRunning = false;

        // Initialize services
        this.userService = new UserService();
        this.employeeService = new EmployeeService();
        this.auctionService = new AuctionService();
        this.chatbotService = new ChatbotService();

        // Initialize auction components
        this.auctionSocketController = new AuctionSocketController();

        // Link auction service with socket controller
        this.auctionService.setSocketController(auctionSocketController);

        System.out.println("🏛️ Smart Museum Server initialized with auction support");
    }

    /**
     * Start both main server and auction server
     */
    public void startServer() {
        try {
            // Start main server
            startMainServer();

            // Start auction server in separate thread
            startAuctionServer();

            System.out.println("🚀 Smart Museum Server complex started successfully");
            System.out.println("📡 Main Server: localhost:" + MAIN_PORT);
            System.out.println("🔨 Auction Server: localhost:" + AUCTION_PORT);

        } catch (IOException e) {
            System.err.println("❌ Failed to start server complex: " + e.getMessage());
            stopServer();
        }
    }

    /**
     * Start main museum server
     */
    private void startMainServer() throws IOException {
        mainServerSocket = new ServerSocket(MAIN_PORT);
        isRunning = true;

        // Main server thread
        Thread mainServerThread = new Thread(() -> {
            System.out.println("🏛️ Main Museum Server started on port " + MAIN_PORT);

            try {
                while (isRunning) {
                    Socket clientSocket = mainServerSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(
                            clientSocket,
                            userService,
                            employeeService,
                            auctionService,
                            chatbotService
                    );

                    clientHandlers.add(clientHandler);
                    threadPool.execute(clientHandler);

                    System.out.println("📱 New main client connected. Total: " + clientHandlers.size());
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("❌ Main server error: " + e.getMessage());
                }
            }
        });

        mainServerThread.setDaemon(true);
        mainServerThread.start();
    }

    /**
     * Start auction server
     */
    private void startAuctionServer() {
        auctionServerThread = new Thread(() -> {
            try {
                System.out.println("🔨 Starting auction socket server...");
                auctionSocketController.start();
            } catch (Exception e) {
                System.err.println("❌ Auction server error: " + e.getMessage());
            }
        });

        auctionServerThread.setDaemon(true);
        auctionServerThread.start();

        System.out.println("🔨 Auction server thread started");
    }

    /**
     * Handle individual client connection for main server
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
     * Broadcast update to all connected main clients
     */
    public void broadcastUpdate(String event) {
        System.out.println("📢 Broadcasting to " + clientHandlers.size() + " main clients: " + event);

        for (ClientHandler handler : clientHandlers) {
            handler.sendBroadcast(event);
        }
    }

    /**
     * Broadcast auction update to auction clients
     */
    public void broadcastAuctionUpdate(int auctionId, String event) {
        if (auctionSocketController != null) {
            System.out.println("🔨 Broadcasting auction update for " + auctionId + ": " + event);
            // Auction updates are handled through BidMessage system in AuctionSocketController
        }
    }

    /**
     * Stop both servers
     */
    public void stopServer() {
        System.out.println("🛑 Shutting down Smart Museum Server complex...");

        isRunning = false;

        try {
            // Stop main server
            if (mainServerSocket != null && !mainServerSocket.isClosed()) {
                mainServerSocket.close();
                System.out.println("🏛️ Main server stopped");
            }

            // Stop auction server
            if (auctionSocketController != null) {
                auctionSocketController.stop();
                System.out.println("🔨 Auction server stopped");
            }

            // Close all client connections
            for (ClientHandler handler : clientHandlers) {
                handler.closeConnection();
            }

            // Shutdown thread pool
            threadPool.shutdown();

            // Cleanup services
            if (auctionService != null) {
                auctionService.cleanup();
            }

            System.out.println("✅ Smart Museum Server complex stopped successfully");

        } catch (IOException e) {
            System.err.println("❌ Error stopping server: " + e.getMessage());
        }
    }

    /**
     * Get server statistics
     */
    public String getServerStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== SMART MUSEUM SERVER STATISTICS ===\n");
        stats.append("Main Server Status: ").append(isRunning ? "RUNNING" : "STOPPED").append("\n");
        stats.append("Main Clients Connected: ").append(clientHandlers.size()).append("\n");
        stats.append("Auction Server Status: ").append(auctionSocketController.isRunning() ? "RUNNING" : "STOPPED").append("\n");

        // Auction statistics
        if (auctionSocketController != null) {
            stats.append("\n").append(auctionSocketController.getAuctionStats());
        }

        // Service statistics
        if (auctionService != null) {
            stats.append("\n").append(auctionService.getAuctionStats());
        }

        return stats.toString();
    }

    /**
     * Start a specific auction
     */
    public void startAuction(int auctionId) {
        if (auctionService != null) {
            auctionService.startAuction(auctionId);
            System.out.println("🚀 Auction " + auctionId + " started via server command");
        }
    }

    /**
     * End a specific auction
     */
    public void endAuction(int auctionId) {
        if (auctionService != null) {
            auctionService.endAuction(auctionId);
            System.out.println("🏁 Auction " + auctionId + " ended via server command");
        }
    }

    /**
     * Get auction service for external access
     */
    public AuctionService getAuctionService() {
        return auctionService;
    }

    /**
     * Get auction socket controller for external access
     */
    public AuctionSocketController getAuctionSocketController() {
        return auctionSocketController;
    }

    // Getters for existing services
    public List<ClientHandler> getClientHandlers() { return clientHandlers; }
    public UserService getUserService() { return userService; }
    public EmployeeService getEmployeeService() { return employeeService; }
    public ChatbotService getChatbotService() { return chatbotService; }
    public boolean isRunning() { return isRunning; }

    /**
     * Main method to start server complex
     */
    public static void main(String[] args) {
        SmartMuseumServer server = new SmartMuseumServer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Shutdown signal received...");
            server.stopServer();
        }));

        // Start server complex
        server.startServer();

        // Keep main thread alive
        try {
            while (server.isRunning()) {
                Thread.sleep(5000); // Check every 5 seconds

                // Print statistics every minute
                if (System.currentTimeMillis() % 60000 < 5000) {
                    System.out.println("\n📊 Server Statistics:");
                    System.out.println(server.getServerStats());
                }
            }
        } catch (InterruptedException e) {
            System.out.println("🛑 Server interrupted");
            server.stopServer();
        }
    }
}