package org.example.smartmuseum.server;

import org.example.smartmuseum.model.service.*;
import org.example.smartmuseum.model.abstracts.BaseUser;
import java.io.*;
import java.net.Socket;

/**
 * Handles individual client connections
 */
public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private BufferedReader inputReader;
    private PrintWriter outputWriter;
    private BaseUser currentUser;

    // Services
    private UserService userService;
    private EmployeeService employeeService;
    private AuctionService auctionService;
    private ChatbotService chatbotService;

    public ClientHandler(Socket clientSocket, UserService userService,
                         EmployeeService employeeService, AuctionService auctionService,
                         ChatbotService chatbotService) {
        this.clientSocket = clientSocket;
        this.userService = userService;
        this.employeeService = employeeService;
        this.auctionService = auctionService;
        this.chatbotService = chatbotService;

        try {
            this.inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up client handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String inputLine;
            sendResponse("Welcome to Smart Museum! Please login or type 'help' for commands.");

            while ((inputLine = inputReader.readLine()) != null) {
                handleRequest(inputLine);
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * Handle client request
     * @param request Client request
     */
    public void handleRequest(String request) {
        if (request == null || request.trim().isEmpty()) {
            sendResponse("Invalid request");
            return;
        }

        String[] parts = request.trim().split("\\s+");
        String command = parts[0].toLowerCase();

        switch (command) {
            case "login":
                handleLogin(parts);
                break;
            case "logout":
                handleLogout();
                break;
            case "chat":
                handleChat(parts);
                break;
            case "bid":
                handleBid(parts);
                break;
            case "attendance":
                handleAttendance(parts);
                break;
            case "help":
                sendResponse(getHelpMessage());
                break;
            default:
                sendResponse("Unknown command: " + command + ". Type 'help' for available commands.");
        }
    }

    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            sendResponse("Usage: login <username> <password>");
            return;
        }

        String username = parts[1];
        String password = parts[2];

        BaseUser user = userService.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            sendResponse("Login successful! Welcome " + username);
            sendResponse("Available actions: " + user.getAvailableActions());
        } else {
            sendResponse("Login failed. Please check your credentials.");
        }
    }

    private void handleLogout() {
        if (currentUser != null) {
            currentUser.logout();
            currentUser = null;
            sendResponse("Logged out successfully");
        } else {
            sendResponse("You are not logged in");
        }
    }

    private void handleChat(String[] parts) {
        if (currentUser == null) {
            sendResponse("Please login first");
            return;
        }

        if (parts.length < 2) {
            sendResponse("Usage: chat <input>");
            return;
        }

        String chatInput = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        String response = chatbotService.processUserInput(currentUser.getUserId(), chatInput);
        sendResponse("Bot: " + response);
    }

    private void handleBid(String[] parts) {
        if (currentUser == null) {
            sendResponse("Please login first");
            return;
        }

        if (parts.length < 3) {
            sendResponse("Usage: bid <auction_id> <amount>");
            return;
        }

        try {
            int auctionId = Integer.parseInt(parts[1]);
            double amount = Double.parseDouble(parts[2]);
            sendResponse("Bid processing for auction " + auctionId + " with amount $" + amount);
        } catch (NumberFormatException e) {
            sendResponse("Invalid auction ID or amount");
        }
    }

    private void handleAttendance(String[] parts) {
        if (currentUser == null) {
            sendResponse("Please login first");
            return;
        }

        if (parts.length < 2) {
            sendResponse("Usage: attendance <checkin|checkout>");
            return;
        }

        String action = parts[1].toLowerCase();
        sendResponse("Attendance " + action + " processed for " + currentUser.getUsername());
    }

    private String getHelpMessage() {
        return "Available commands:\n" +
                "login <username> <password> - Login to the system\n" +
                "logout - Logout from the system\n" +
                "chat <message> - Chat with the bot\n" +
                "bid <auction_id> <amount> - Place a bid\n" +
                "attendance <checkin|checkout> - Record attendance\n" +
                "help - Show this help message";
    }

    /**
     * Send response to client
     * @param response Response message
     */
    public void sendResponse(String response) {
        if (outputWriter != null) {
            outputWriter.println(response);
        }
    }

    /**
     * Send broadcast message to client
     * @param message Broadcast message
     */
    public void sendBroadcast(String message) {
        sendResponse("BROADCAST: " + message);
    }

    /**
     * Close client connection
     */
    public void closeConnection() {
        try {
            if (inputReader != null) inputReader.close();
            if (outputWriter != null) outputWriter.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}