package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.smartmuseum.model.entity.BidMessage;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.util.SessionContext;

import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Perbaikan BidderController untuk visitor interface
 */
public class BidderController implements Initializable, SessionAwareController {

    // FXML Elements
    @FXML private Label lblAuctionTitle;
    @FXML private Label lblAuctionId;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblBidderName;
    @FXML private ImageView imgArtwork;
    @FXML private Label lblArtworkTitle;
    @FXML private Label lblArtworkArtist;
    @FXML private Label lblCurrentBid;
    @FXML private Label lblCurrentWinner;
    @FXML private Label lblTimeRemaining;
    @FXML private Label lblParticipantCount;
    @FXML private TextField txtBidAmount;
    @FXML private Button btnPlaceBid;
    @FXML private Label lblBidStatus;
    @FXML private ListView<String> listBidHistory;
    @FXML private ListView<String> listParticipants;
    @FXML private Label lblMinimumBid;
    @FXML private Label lblYourLastBid;
    @FXML private Button btnDisconnect;

    // Socket connection
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;

    // Session and auction data
    private SessionContext sessionContext;
    private int auctionId = 1; // Default auction ID
    private BigDecimal currentHighestBid = new BigDecimal("500000"); // Default starting bid
    private BigDecimal minimumIncrement = new BigDecimal("10000");
    private BigDecimal myLastBid = BigDecimal.ZERO;
    private String currentWinner = "";
    private boolean auctionActive = false;

    // Formatting
    private NumberFormat currencyFormat;

    @Override
    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
        System.out.println("✅ BidderController received session context: " + sessionContext);
    }

    @Override
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup currency formatter untuk Indonesia
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        // Setup UI
        setupUI();

        // Platform.runLater untuk memastikan sessionContext sudah di-set
        Platform.runLater(() -> {
            if (sessionContext != null) {
                loadUserInfo();
                connectToAuctionServer();
            } else {
                showError("No session context available");
            }
        });
    }

    private void setupUI() {
        // Disable bid button initially
        btnPlaceBid.setDisable(true);

        // Setup button actions
        btnPlaceBid.setOnAction(e -> placeBid());
        btnDisconnect.setOnAction(e -> disconnect());

        // Setup text field validation
        txtBidAmount.textProperty().addListener((obs, oldText, newText) -> {
            validateBidInput(newText);
        });

        // Setup lists
        listBidHistory.getItems().clear();
        listParticipants.getItems().clear();

        // Set initial values
        lblTimeRemaining.setText("--");
        lblCurrentBid.setText(formatCurrency(currentHighestBid));
        lblCurrentWinner.setText("--");
        lblParticipantCount.setText("0");
        lblMinimumBid.setText("Min: " + formatCurrency(currentHighestBid.add(minimumIncrement)));
        lblYourLastBid.setText("Your last: " + formatCurrency(BigDecimal.ZERO));

        // Setup artwork info
        lblArtworkTitle.setText("Ming Dynasty Vase");
        lblArtworkArtist.setText("Unknown Artist, 15th Century");

        // Update current time
        updateCurrentTime();

        // Show initial status
        showInfo("Connecting to auction server...");
    }

    private void loadUserInfo() {
        if (sessionContext != null && sessionContext.getSessionManager() != null) {
            User currentUser = sessionContext.getSessionManager().getCurrentUser();
            if (currentUser != null) {
                lblBidderName.setText("Bidder: " + currentUser.getUsername());

                // Set window title
                Platform.runLater(() -> {
                    Stage stage = (Stage) btnPlaceBid.getScene().getWindow();
                    if (stage != null) {
                        stage.setTitle("Live Auction - " + currentUser.getUsername());
                    }
                });

                System.out.println("✅ User info loaded: " + currentUser.getUsername());
            }
        }
    }

    private void connectToAuctionServer() {
        System.out.println("🔗 Attempting to connect to auction server...");

        try {
            // Connect to auction socket server
            socket = new Socket("localhost", 8082);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("✅ Socket connection established");

            // Get user info
            User currentUser = sessionContext.getSessionManager().getCurrentUser();
            String sessionId = sessionContext.getSessionManager().getSessionId();

            // Send connection message dengan format yang benar
            String connectMessage = String.format("CONNECT|%d|%s|%s|%d",
                    auctionId, sessionId, currentUser.getUsername(), currentUser.getUserId());

            System.out.println("📤 Sending connect message: " + connectMessage);
            out.println(connectMessage);

            isConnected = true;
            showSuccess("Connected to auction server");

            // Update UI
            lblAuctionId.setText("Auction #" + auctionId);
            lblAuctionTitle.setText("Live Auction - Antique Collection");

            // Start listening for messages
            startMessageListener();

        } catch (IOException e) {
            showError("Failed to connect to auction server: " + e.getMessage());
            System.err.println("❌ Socket connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startMessageListener() {
        Thread messageThread = new Thread(() -> {
            try {
                String message;
                System.out.println("👂 Message listener started");

                while (isConnected && (message = in.readLine()) != null) {
                    final String finalMessage = message;
                    System.out.println("📨 Received message: " + finalMessage);

                    Platform.runLater(() -> processServerMessage(finalMessage));
                }
            } catch (IOException e) {
                if (isConnected) {
                    System.err.println("❌ Connection lost to auction server: " + e.getMessage());
                    Platform.runLater(() -> showError("Connection lost to auction server"));
                }
            }
        });
        messageThread.setDaemon(true);
        messageThread.start();
    }

    private void processServerMessage(String messageText) {
        try {
            System.out.println("🔄 Processing message: " + messageText);
            BidMessage message = BidMessage.fromSocketMessage(messageText);

            switch (message.getType()) {
                case BID_PLACED:
                    handleBidPlaced(message);
                    break;
                case TIMER_UPDATE:
                    handleTimerUpdate(message);
                    break;
                case AUCTION_STARTED:
                    handleAuctionStarted(message);
                    break;
                case AUCTION_ENDED:
                    handleAuctionEnded(message);
                    break;
                case USER_JOINED:
                    handleUserJoined(message);
                    break;
                case USER_LEFT:
                    handleUserLeft(message);
                    break;
                case ERROR:
                    showError(message.getMessage());
                    break;
                default:
                    System.out.println("⚠️ Unknown message type: " + message.getType());
            }
        } catch (Exception e) {
            System.err.println("❌ Error processing server message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleBidPlaced(BidMessage message) {
        System.out.println("💰 Bid placed: " + message.getUsername() + " - " + formatCurrency(message.getBidAmount()));

        currentHighestBid = message.getCurrentHighestBid();
        currentWinner = message.getCurrentWinner();

        // Update UI
        lblCurrentBid.setText(formatCurrency(currentHighestBid));
        lblCurrentWinner.setText(currentWinner != null ? currentWinner : "--");

        // Update minimum bid
        BigDecimal minimumBid = currentHighestBid.add(minimumIncrement);
        lblMinimumBid.setText("Min: " + formatCurrency(minimumBid));

        // Add to bid history
        String bidEntry = String.format("%s - %s bid %s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                message.getUsername(),
                formatCurrency(message.getBidAmount()));

        listBidHistory.getItems().add(0, bidEntry);

        // Keep only last 15 entries
        if (listBidHistory.getItems().size() > 15) {
            listBidHistory.getItems().remove(15, listBidHistory.getItems().size());
        }

        // Check if this is my bid
        User currentUser = sessionContext.getSessionManager().getCurrentUser();
        if (currentUser != null && message.getUsername().equals(currentUser.getUsername())) {
            myLastBid = message.getBidAmount();
            lblYourLastBid.setText("Your last: " + formatCurrency(myLastBid));
            showSuccess("✅ Your bid placed successfully!");
            txtBidAmount.clear();
        } else {
            showInfo("💰 New bid by " + message.getUsername());
        }
    }

    private void handleTimerUpdate(BidMessage message) {
        int remainingTime = message.getRemainingTime();
        lblTimeRemaining.setText(remainingTime + "s");

        System.out.println("⏰ Timer update: " + remainingTime + " seconds remaining");

        // Color coding for time remaining
        if (remainingTime <= 5) {
            lblTimeRemaining.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-font-size: 28px;");
        } else if (remainingTime <= 10) {
            lblTimeRemaining.setStyle("-fx-text-fill: #fd7e14; -fx-font-weight: bold; -fx-font-size: 24px;");
        } else {
            lblTimeRemaining.setStyle("-fx-text-fill: #198754; -fx-font-size: 20px;");
        }

        // Show warning message if provided
        if (message.getMessage() != null && !message.getMessage().isEmpty()) {
            showWarning(message.getMessage());
        }
    }

    private void handleAuctionStarted(BidMessage message) {
        System.out.println("🚀 Auction started!");

        auctionActive = true;
        btnPlaceBid.setDisable(false);

        currentHighestBid = message.getCurrentHighestBid();
        lblCurrentBid.setText(formatCurrency(currentHighestBid));

        BigDecimal minimumBid = currentHighestBid.add(minimumIncrement);
        lblMinimumBid.setText("Min: " + formatCurrency(minimumBid));

        // Clear bid amount input
        txtBidAmount.clear();

        showSuccess("🚀 Auction started! You can now place bids. Timer: 30 seconds");

        listBidHistory.getItems().add(0, "🚀 Auction started with starting bid: " + formatCurrency(currentHighestBid));
    }

    private void handleAuctionEnded(BidMessage message) {
        System.out.println("🏁 Auction ended!");

        auctionActive = false;
        btnPlaceBid.setDisable(true);
        txtBidAmount.setDisable(true);

        lblTimeRemaining.setText("ENDED");
        lblTimeRemaining.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-font-size: 24px;");

        String winner = message.getCurrentWinner();
        BigDecimal winningBid = message.getCurrentHighestBid();

        if (winner != null && winningBid != null) {
            lblCurrentWinner.setText("🏆 " + winner);

            String endMessage = String.format("🏁 Auction ended! Winner: %s with bid: %s",
                    winner, formatCurrency(winningBid));

            listBidHistory.getItems().add(0, endMessage);

            // Check if current user won
            User currentUser = sessionContext.getSessionManager().getCurrentUser();
            if (currentUser != null && winner.equals(currentUser.getUsername())) {
                showSuccess("🎉 Congratulations! You won the auction!");
            } else {
                showInfo("Auction ended. Winner: " + winner);
            }
        }
    }

    private void handleUserJoined(BidMessage message) {
        String participantEntry = "👤 " + message.getUsername();
        if (!listParticipants.getItems().contains(participantEntry)) {
            listParticipants.getItems().add(participantEntry);
        }
        updateParticipantCount();

        System.out.println("👋 User joined: " + message.getUsername());
    }

    private void handleUserLeft(BidMessage message) {
        String participantEntry = "👤 " + message.getUsername();
        listParticipants.getItems().remove(participantEntry);
        updateParticipantCount();

        System.out.println("👋 User left: " + message.getUsername());
    }

    private void updateParticipantCount() {
        lblParticipantCount.setText(String.valueOf(listParticipants.getItems().size()));
    }

    private void placeBid() {
        if (!isConnected || !auctionActive) {
            showError("Cannot place bid: auction not active or not connected");
            return;
        }

        String bidText = txtBidAmount.getText().trim();
        if (bidText.isEmpty()) {
            showError("Please enter a bid amount");
            return;
        }

        try {
            // Parse bid amount (hapus semua non-digit)
            BigDecimal bidAmount = new BigDecimal(bidText.replaceAll("[^\\d]", ""));

            // Validate bid
            BigDecimal minimumBid = currentHighestBid.add(minimumIncrement);
            if (bidAmount.compareTo(minimumBid) < 0) {
                showError(String.format("Bid must be at least %s", formatCurrency(minimumBid)));
                return;
            }

            // Create bid message
            User currentUser = sessionContext.getSessionManager().getCurrentUser();
            String sessionId = sessionContext.getSessionManager().getSessionId();

            BidMessage bidMessage = BidMessage.createBidPlaced(auctionId,
                    currentUser.getUserId(), currentUser.getUsername(), sessionId, bidAmount);

            // Send to server
            String messageText = bidMessage.toSocketMessage();
            System.out.println("📤 Sending bid: " + messageText);
            out.println(messageText);

            // Disable button temporarily
            btnPlaceBid.setDisable(true);

            // Re-enable after 2 seconds
            Platform.runLater(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(() -> btnPlaceBid.setDisable(!auctionActive));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });

            showInfo("📤 Bid sent: " + formatCurrency(bidAmount));

        } catch (NumberFormatException e) {
            showError("Invalid bid amount format");
        } catch (Exception e) {
            showError("Error placing bid: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateBidInput(String input) {
        try {
            if (!input.isEmpty()) {
                BigDecimal amount = new BigDecimal(input.replaceAll("[^\\d]", ""));
                BigDecimal minimumBid = currentHighestBid.add(minimumIncrement);

                if (amount.compareTo(minimumBid) >= 0) {
                    btnPlaceBid.setDisable(!auctionActive);
                    txtBidAmount.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
                } else {
                    btnPlaceBid.setDisable(true);
                    txtBidAmount.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
                }
            } else {
                btnPlaceBid.setDisable(true);
                txtBidAmount.setStyle("");
            }
        } catch (NumberFormatException e) {
            btnPlaceBid.setDisable(true);
            txtBidAmount.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
        }
    }

    private void disconnect() {
        if (isConnected) {
            isConnected = false;
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
            showInfo("Disconnected from auction server");
        }

        // Close window
        Stage stage = (Stage) btnDisconnect.getScene().getWindow();
        stage.close();
    }

    private void updateCurrentTime() {
        Thread timeThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Platform.runLater(() -> {
                    if (lblCurrentTime != null) {
                        String currentTime = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        lblCurrentTime.setText(currentTime);
                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();
    }

    private String formatCurrency(BigDecimal amount) {
        return currencyFormat.format(amount).replace("Rp", "Rp ");
    }

    private void showSuccess(String message) {
        lblBidStatus.setText("✅ " + message);
        lblBidStatus.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
    }

    private void showError(String message) {
        lblBidStatus.setText("❌ " + message);
        lblBidStatus.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
    }

    private void showInfo(String message) {
        lblBidStatus.setText("ℹ️ " + message);
        lblBidStatus.setStyle("-fx-text-fill: #0d6efd; -fx-font-weight: bold;");
    }

    private void showWarning(String message) {
        lblBidStatus.setText("⚠️ " + message);
        lblBidStatus.setStyle("-fx-text-fill: #fd7e14; -fx-font-weight: bold;");
    }

    @Override
    public void cleanup() {
        disconnect();
        SessionAwareController.super.cleanup();
    }
}