package org.example.smartmuseum.controller;

import org.example.smartmuseum.model.entity.BidMessage;
import org.example.smartmuseum.model.entity.Auction;
import org.example.smartmuseum.model.entity.Bid;
import org.example.smartmuseum.model.service.AuctionService;
import org.example.smartmuseum.model.service.AuctionTimer;
import org.example.smartmuseum.util.SessionManager;

import java.io.*;
import java.net.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Socket controller for real-time auction communication
 * Manages bidder connections, bid processing, and timer synchronization
 */
public class AuctionSocketController {

    private static final int AUCTION_PORT = 8082;
    private static final BigDecimal MIN_BID_INCREMENT = new BigDecimal("10000"); // 10,000 minimum increment

    private ServerSocket serverSocket;
    private boolean isRunning;
    private ExecutorService threadPool;

    // Services
    private AuctionService auctionService;
    private AuctionTimer auctionTimer;

    // Connected clients per auction
    private ConcurrentMap<Integer, ConcurrentMap<String, AuctionClient>> auctionClients;

    // Current auction data
    private ConcurrentMap<Integer, AuctionData> activeAuctions;

    public static class AuctionClient {
        private final String sessionId;
        private final String username;
        private final int userId;
        private final Socket socket;
        private final PrintWriter out;
        private final BufferedReader in;

        public AuctionClient(String sessionId, String username, int userId, Socket socket) throws IOException {
            this.sessionId = sessionId;
            this.username = username;
            this.userId = userId;
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public void sendMessage(BidMessage message) {
            try {
                out.println(message.toSocketMessage());
            } catch (Exception e) {
                System.err.println("Error sending message to client " + username + ": " + e.getMessage());
            }
        }

        public void close() {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public String getUsername() { return username; }
        public int getUserId() { return userId; }
        public Socket getSocket() { return socket; }
        public BufferedReader getIn() { return in; }
    }

    public static class AuctionData {
        private int auctionId;
        private BigDecimal currentBid;
        private String currentWinner;
        private int currentWinnerUserId;
        private boolean isActive;
        private int participantCount;

        public AuctionData(int auctionId, BigDecimal startingBid) {
            this.auctionId = auctionId;
            this.currentBid = startingBid;
            this.isActive = false;
            this.participantCount = 0;
        }

        // Getters and setters
        public int getAuctionId() { return auctionId; }
        public BigDecimal getCurrentBid() { return currentBid; }
        public void setCurrentBid(BigDecimal currentBid) { this.currentBid = currentBid; }
        public String getCurrentWinner() { return currentWinner; }
        public void setCurrentWinner(String currentWinner) { this.currentWinner = currentWinner; }
        public int getCurrentWinnerUserId() { return currentWinnerUserId; }
        public void setCurrentWinnerUserId(int currentWinnerUserId) { this.currentWinnerUserId = currentWinnerUserId; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        public int getParticipantCount() { return participantCount; }
        public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }
    }

    public AuctionSocketController() {
        this.auctionTimer = new AuctionTimer();
        this.threadPool = Executors.newFixedThreadPool(20);
        this.auctionClients = new ConcurrentHashMap<>();
        this.activeAuctions = new ConcurrentHashMap<>();
        this.isRunning = false;
    }

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    /**
     * Start the auction socket server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(AUCTION_PORT);
            isRunning = true;
            System.out.println("🚀 Auction Socket Server started on port " + AUCTION_PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleNewClient(clientSocket));
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("❌ Auction server error: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    /**
     * Stop the auction socket server
     */
    public void stop() {
        isRunning = false;
        try {
            // Stop all auction timers
            auctionTimer.stopAllTimers();

            // Close all client connections
            auctionClients.values().forEach(auctionMap ->
                    auctionMap.values().forEach(AuctionClient::close));

            if (serverSocket != null) {
                serverSocket.close();
            }
            threadPool.shutdown();

            System.out.println("🛑 Auction Socket Server stopped");
        } catch (IOException e) {
            System.err.println("Error stopping auction server: " + e.getMessage());
        }
    }

    /**
     * Handle new client connection
     */
    private void handleNewClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Wait for initial connection message with session info
            String connectionMessage = in.readLine();
            if (connectionMessage == null) {
                clientSocket.close();
                return;
            }

            // Parse connection message: "CONNECT|auctionId|sessionId|username|userId"
            String[] parts = connectionMessage.split("\\|");
            if (parts.length != 5 || !parts[0].equals("CONNECT")) {
                clientSocket.close();
                return;
            }

            int auctionId = Integer.parseInt(parts[1]);
            String sessionId = parts[2];
            String username = parts[3];
            int userId = Integer.parseInt(parts[4]);

            // Create client wrapper
            AuctionClient client = new AuctionClient(sessionId, username, userId, clientSocket);

            // Add client to auction
            addClientToAuction(auctionId, client);

            // Start handling messages from this client
            handleClientMessages(auctionId, client);

        } catch (Exception e) {
            System.err.println("Error handling new client: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                System.err.println("Error closing client socket: " + ex.getMessage());
            }
        }
    }

    /**
     * Add client to auction and notify others
     */
    private void addClientToAuction(int auctionId, AuctionClient client) {
        auctionClients.computeIfAbsent(auctionId, k -> new ConcurrentHashMap<>())
                .put(client.getSessionId(), client);

        // Update participant count
        AuctionData auctionData = activeAuctions.get(auctionId);
        if (auctionData != null) {
            auctionData.setParticipantCount(auctionClients.get(auctionId).size());
        }

        System.out.println("✅ " + client.getUsername() + " joined auction " + auctionId);

        // Notify all clients about new participant
        BidMessage joinMessage = BidMessage.createUserJoined(auctionId, client.getUserId(),
                client.getUsername(), client.getSessionId());
        broadcastToAuction(auctionId, joinMessage);

        // Send current auction state to new client
        sendCurrentAuctionState(auctionId, client);
    }

    /**
     * Handle messages from a client
     */
    private void handleClientMessages(int auctionId, AuctionClient client) {
        try {
            String message;
            while ((message = client.getIn().readLine()) != null) {
                processClientMessage(auctionId, client, message);
            }
        } catch (IOException e) {
            System.out.println("Client " + client.getUsername() + " disconnected from auction " + auctionId);
        } finally {
            removeClientFromAuction(auctionId, client);
        }
    }

    /**
     * Process message from client
     */
    private void processClientMessage(int auctionId, AuctionClient client, String messageText) {
        try {
            BidMessage message = BidMessage.fromSocketMessage(messageText);

            switch (message.getType()) {
                case BID_PLACED:
                    processBid(auctionId, client, message);
                    break;
                case HEARTBEAT:
                    // Respond to heartbeat
                    client.sendMessage(BidMessage.createHeartbeat());
                    break;
                default:
                    System.out.println("Unknown message type: " + message.getType());
            }
        } catch (Exception e) {
            System.err.println("Error processing client message: " + e.getMessage());
            BidMessage errorMessage = BidMessage.createError(auctionId, "Error processing message");
            client.sendMessage(errorMessage);
        }
    }

    /**
     * Process bid and reset timer to 30 seconds
     */
    private void processBid(int auctionId, AuctionClient client, BidMessage bidMessage) {
        if (auctionService == null) {
            BidMessage errorMessage = BidMessage.createError(auctionId, "Service not available");
            client.sendMessage(errorMessage);
            return;
        }

        AuctionData auctionData = activeAuctions.get(auctionId);
        if (auctionData == null || !auctionData.isActive()) {
            BidMessage errorMessage = BidMessage.createError(auctionId, "Auction is not active");
            client.sendMessage(errorMessage);
            return;
        }

        BigDecimal bidAmount = bidMessage.getBidAmount();
        BigDecimal currentBid = auctionData.getCurrentBid();
        BigDecimal minimumBid = currentBid.add(MIN_BID_INCREMENT);

        // Validate bid amount
        if (bidAmount.compareTo(minimumBid) < 0) {
            String errorMsg = String.format("Bid must be at least Rp %s (minimum increment: Rp %s)",
                    formatCurrency(minimumBid), formatCurrency(MIN_BID_INCREMENT));
            BidMessage errorMessage = BidMessage.createError(auctionId, errorMsg);
            client.sendMessage(errorMessage);
            return;
        }

        // Process successful bid
        auctionData.setCurrentBid(bidAmount);
        auctionData.setCurrentWinner(client.getUsername());
        auctionData.setCurrentWinnerUserId(client.getUserId());

        // Create bid record
        Bid bid = new Bid();
        bid.setAuctionId(auctionId);
        bid.setUserId(client.getUserId());
        bid.setBidAmount(bidAmount);
        bid.setBidTime(new Timestamp(System.currentTimeMillis()));

        // Save bid to service
        if (auctionService != null) {
            auctionService.placeBid(auctionId, bid);
        }

        System.out.println("✅ Bid placed: " + client.getUsername() + " - Rp " + formatCurrency(bidAmount));

        // **RESET TIMER TO 30 SECONDS** - This is crucial!
        auctionTimer.resetTimer(auctionId);
        System.out.println("🔄 Timer reset to 30 seconds for auction " + auctionId);

        // Create success message
        BidMessage successMessage = BidMessage.createBidPlaced(auctionId, client.getUserId(),
                client.getUsername(), client.getSessionId(), bidAmount);
        successMessage.setCurrentHighestBid(bidAmount);
        successMessage.setCurrentWinner(client.getUsername());

        // Broadcast to all clients
        broadcastToAuction(auctionId, successMessage);
    }

    /**
     * Remove client from auction
     */
    private void removeClientFromAuction(int auctionId, AuctionClient client) {
        ConcurrentMap<String, AuctionClient> clients = auctionClients.get(auctionId);
        if (clients != null) {
            clients.remove(client.getSessionId());

            // Update participant count
            AuctionData auctionData = activeAuctions.get(auctionId);
            if (auctionData != null) {
                auctionData.setParticipantCount(clients.size());
            }

            System.out.println("❌ " + client.getUsername() + " left auction " + auctionId);

            // Notify remaining clients
            BidMessage leaveMessage = BidMessage.createUserLeft(auctionId, client.getUserId(),
                    client.getUsername(), client.getSessionId());
            broadcastToAuction(auctionId, leaveMessage);
        }

        client.close();
    }

    /**
     * Start auction with 30-second timer
     */
    public void startAuction(int auctionId) {
        System.out.println("🔥 DEBUG: AuctionSocketController.startAuction(" + auctionId + ") called");

        if (auctionService == null) {
            System.err.println("❌ AuctionService not set in socket controller");
            return;
        }

        Auction auction = auctionService.getAuction(auctionId);
        if (auction == null) {
            System.err.println("❌ Auction not found: " + auctionId);
            return;
        }

        // Create auction data for socket management
        AuctionData auctionData = new AuctionData(auctionId, auction.getStartingBid());
        auctionData.setActive(true);
        activeAuctions.put(auctionId, auctionData);

        System.out.println("🚀 Starting auction " + auctionId + " with starting bid: " + auction.getStartingBid());
        System.out.println("🔥 DEBUG: Connected clients for auction " + auctionId + ": " +
                (auctionClients.get(auctionId) != null ? auctionClients.get(auctionId).size() : 0));

        // Start 30-second countdown timer
        auctionTimer.startTimer(auctionId,
                // onTimeUpdate - broadcast remaining time every second
                (remainingTime) -> {
                    BidMessage timerMessage = BidMessage.createTimerUpdate(auctionId, remainingTime);
                    if (remainingTime <= 10) {
                        timerMessage.setMessage("⚠️ " + remainingTime + " seconds remaining!");
                    }
                    System.out.println("⏰ Broadcasting timer update: " + remainingTime + "s");
                    broadcastToAuction(auctionId, timerMessage);
                },
                // onTimeEnd - auction ends when timer reaches 0
                (endedAuctionId) -> {
                    System.out.println("⏰ Timer expired for auction " + endedAuctionId + " - ending auction");
                    endAuction(endedAuctionId);
                },
                // onWarning (10 seconds)
                () -> {
                    BidMessage warningMessage = BidMessage.createTimerUpdate(auctionId, 10);
                    warningMessage.setMessage("⚠️ 10 seconds remaining!");
                    broadcastToAuction(auctionId, warningMessage);
                },
                // onCritical (5 seconds)
                () -> {
                    BidMessage criticalMessage = BidMessage.createTimerUpdate(auctionId, 5);
                    criticalMessage.setMessage("🚨 FINAL 5 SECONDS!");
                    broadcastToAuction(auctionId, criticalMessage);
                }
        );

        // Notify all connected clients
        BidMessage startMessage = BidMessage.createAuctionStarted(auctionId, auction.getStartingBid());
        System.out.println("📤 Broadcasting auction start message to " +
                (auctionClients.get(auctionId) != null ? auctionClients.get(auctionId).size() : 0) + " clients");
        broadcastToAuction(auctionId, startMessage);

        System.out.println("✅ Auction " + auctionId + " started successfully");
    }

    /**
     * End auction and update database with end time
     */
    public void endAuction(int auctionId) {
        AuctionData auctionData = activeAuctions.get(auctionId);
        if (auctionData == null) {
            return;
        }

        auctionData.setActive(false);
        auctionTimer.stopTimer(auctionId);

        // Update auction service with end time
        if (auctionService != null) {
            auctionService.endAuction(auctionId);
        }

        String winner = auctionData.getCurrentWinner();
        BigDecimal winningBid = auctionData.getCurrentBid();

        System.out.println("🏁 Auction " + auctionId + " ended. Winner: " + winner + " - Rp " + formatCurrency(winningBid));

        // Notify all clients
        BidMessage endMessage = BidMessage.createAuctionEnded(auctionId, winner, winningBid);
        broadcastToAuction(auctionId, endMessage);

        // Cleanup
        activeAuctions.remove(auctionId);
    }


    /**
     * Broadcast message to all clients in an auction
     */
    private void broadcastToAuction(int auctionId, BidMessage message) {
        ConcurrentMap<String, AuctionClient> clients = auctionClients.get(auctionId);
        if (clients != null && !clients.isEmpty()) {
            String messageText = message.toSocketMessage();
            System.out.println("📡 Broadcasting to " + clients.size() + " clients: " + message.getType());

            clients.values().forEach(client -> {
                try {
                    client.sendMessage(message);
                    System.out.println("📤 Sent to " + client.getUsername());
                } catch (Exception e) {
                    System.err.println("❌ Error sending message to " + client.getUsername() + ": " + e.getMessage());
                }
            });
        } else {
            System.out.println("⚠️ No clients connected to auction " + auctionId);
        }
    }

    /**
     * Send current auction state to a specific client
     */
    private void sendCurrentAuctionState(int auctionId, AuctionClient client) {
        AuctionData auctionData = activeAuctions.get(auctionId);
        if (auctionData != null) {
            BidMessage stateMessage = new BidMessage(BidMessage.MessageType.TIMER_UPDATE, auctionId);
            stateMessage.setCurrentHighestBid(auctionData.getCurrentBid());
            stateMessage.setCurrentWinner(auctionData.getCurrentWinner());
            stateMessage.setRemainingTime(auctionTimer.getRemainingTime(auctionId));
            stateMessage.setMessage("Current auction state");
            client.sendMessage(stateMessage);
        }
    }

    /**
     * Cleanup auction after it ends
     */
    private void cleanupAuction(int auctionId) {
        activeAuctions.remove(auctionId);
        ConcurrentMap<String, AuctionClient> clients = auctionClients.remove(auctionId);
        if (clients != null) {
            clients.values().forEach(AuctionClient::close);
        }
        System.out.println("🧹 Cleaned up auction " + auctionId);
    }

    /**
     * Get auction statistics
     */
    public String getAuctionStats() {
        StringBuilder stats = new StringBuilder("Auction Socket Statistics:\n");
        stats.append("Active Auctions: ").append(activeAuctions.size()).append("\n");
        stats.append("Connected Clients: ").append(
                auctionClients.values().stream().mapToInt(Map::size).sum()
        ).append("\n");

        activeAuctions.forEach((auctionId, data) -> {
            stats.append("Auction ").append(auctionId).append(": ")
                    .append(data.getParticipantCount()).append(" participants, ")
                    .append("Current bid: Rp ").append(formatCurrency(data.getCurrentBid()))
                    .append(" by ").append(data.getCurrentWinner()).append("\n");
        });

        return stats.toString();
    }

    /**
     * Format currency for display
     */
    private String formatCurrency(BigDecimal amount) {
        return String.format("%,d", amount.longValue());
    }

    // Getters
    public boolean isRunning() { return isRunning; }
    public AuctionTimer getAuctionTimer() { return auctionTimer; }
    public AuctionService getAuctionService() { return auctionService; }
}