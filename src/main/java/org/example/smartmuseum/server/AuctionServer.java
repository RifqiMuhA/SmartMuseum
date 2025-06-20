package org.example.smartmuseum.server;

import org.example.smartmuseum.model.service.AuctionService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dedicated server for auction operations
 */
public class AuctionServer {

    private static final int AUCTION_PORT = 8082;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private AuctionService auctionService;
    private boolean isRunning;

    public AuctionServer() {
        this.threadPool = Executors.newFixedThreadPool(5);
        this.auctionService = new AuctionService();
        this.isRunning = false;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(AUCTION_PORT);
            isRunning = true;
            System.out.println("Auction Server started on port " + AUCTION_PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleAuctionClient(clientSocket));
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Auction server error: " + e.getMessage());
            }
        }
    }

    private void handleAuctionClient(Socket clientSocket) {
        // Handle auction-specific client interactions
        System.out.println("New auction client connected");
        // Implementation would handle auction operations here
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            threadPool.shutdown();
        } catch (IOException e) {
            System.err.println("Error stopping auction server: " + e.getMessage());
        }
    }
}