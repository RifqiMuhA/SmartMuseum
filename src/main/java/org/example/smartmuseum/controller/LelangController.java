package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.smartmuseum.model.entity.BidMessage;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.util.FXMLLoaderHelper;
import org.example.smartmuseum.util.SessionContext;

import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

/**
 * Updated LelangController with socket integration for real-time auction
 * Connects to auction socket server for live bidding experience
 */
public class LelangController implements SessionAwareController {

    @FXML private Label labelLogo;
    @FXML private Button backButton;
    @FXML private ImageView gambarUtamaDinamis;
    @FXML private Label namaBenda;
    @FXML private Label auctionInformation;
    @FXML private Label auctionIdDinamis;
    @FXML private Label dateDinamis;
    @FXML private Label waktuDinamis;
    @FXML private Label namaPemilikDinamis;
    @FXML private Label currentBidStatis;
    @FXML private Label hargaDinamis;
    @FXML private Label jumlahBidDinamis;
    @FXML private Label yourBid;
    @FXML private TextField inputBid;
    @FXML private Button buttonBid;
    @FXML private Label warning;
    @FXML private Label timeLeftStatis;
    @FXML private Label detikDinamis;
    @FXML private Label detikStatis;
    @FXML private Button btnOpenBidderWindow;
    @FXML private Label lblConnectedBidders;

    // Socket connection
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;

    // Auction logic variables
    private BigDecimal currentBid = new BigDecimal("500000");
    private BigDecimal minimumIncrement = new BigDecimal("10000");
    private int bidCount = 0;
    private int timeLeft = 30;
    private NumberFormat currencyFormat;
    private boolean auctionEnded = false;
    private String instanceName;
    private int auctionId = 1; // Default auction ID
    private SessionContext sessionContext;

    @Override
    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
        System.out.println("LelangController received session context: " + sessionContext);
    }

    @Override
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    @FXML
    public void initialize() {
        // Generate unique instance name
        instanceName = "Guest" + (new Random().nextInt(1000) + 1);

        // Setup currency formatter for Rupiah
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        // Set initial values
        setupInitialUI();

        // Platform.runLater to ensure sessionContext is set
        Platform.runLater(() -> {
            if (sessionContext != null) {
                loadUserInfo();
                connectToAuctionServer();
            } else {
                System.out.println("No session context available, using guest mode");
            }
        });

        // Setup button actions
        buttonBid.setOnAction(event -> placeBid());
        backButton.setOnAction(event -> handleBack());
        if (btnOpenBidderWindow != null) {
            btnOpenBidderWindow.setOnAction(event -> openBidderWindow());
        }

        // Set window title
        Platform.runLater(() -> {
            Stage stage = (Stage) buttonBid.getScene().getWindow();
            if (stage != null) {
                stage.setTitle("SeniMatic Live Auction - " + instanceName);
            }
        });
    }

    private void setupInitialUI() {
        namaBenda.setText("Ming Dynasty Antique Vase");
        auctionIdDinamis.setText("AUC-001");
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        dateDinamis.setText(currentDate.format(dateFormatter));
        updateTimeDisplay();
        namaPemilikDinamis.setText("Museum Collection");

        // Set sample image
        try {
            String imagePath = "/org/example/smartmuseum/images/vase.jpg";
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image != null && !image.isError()) {
                gambarUtamaDinamis.setImage(image);
            }
        } catch (Exception e) {
            System.out.println("Image not found: " + e.getMessage());
        }

        // Initialize auction data
        updateBidDisplay();
        updateTimeDisplay();

        // Initially disable bid button
        buttonBid.setDisable(true);

        if (lblConnectedBidders != null) {
            lblConnectedBidders.setText("Bidders: 0");
        }
    }

    private void loadUserInfo() {
        if (sessionContext != null && sessionContext.getSessionManager() != null) {
            User currentUser = sessionContext.getSessionManager().getCurrentUser();
            if (currentUser != null) {
                instanceName = currentUser.getUsername();
                yourBid.setText("Your Bid (" + instanceName + ")");
                System.out.println("✅ User loaded: " + instanceName);
            }
        }
    }

    private void connectToAuctionServer() {
        try {
            // Connect to auction socket server
            socket = new Socket("localhost", 8082);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send connection message
            User currentUser = sessionContext != null && sessionContext.getSessionManager() != null ?
                    sessionContext.getSessionManager().getCurrentUser() : null;
            String sessionId = sessionContext != null && sessionContext.getSessionManager() != null ?
                    sessionContext.getSessionManager().getSessionId() : "guest";
            int userId = currentUser != null ? currentUser.getUserId() : 0;

            String connectMessage = String.format("CONNECT|%d|%s|%s|%d",
                    auctionId, sessionId, instanceName, userId);

            out.println(connectMessage);

            isConnected = true;
            warning.setText("✅ Connected to live auction");
            warning.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px;");

            // Start listening for messages
            startMessageListener();

        } catch (IOException e) {
            warning.setText("❌ Failed to connect to auction server");
            warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
            System.err.println("Socket connection error: " + e.getMessage());
        }
    }

    private void startMessageListener() {
        Thread messageThread = new Thread(() -> {
            try {
                String message;
                while (isConnected && (message = in.readLine()) != null) {
                    final String finalMessage = message;
                    Platform.runLater(() -> processServerMessage(finalMessage));
                }
            } catch (IOException e) {
                if (isConnected) {
                    Platform.runLater(() -> {
                        warning.setText("❌ Connection lost to auction server");
                        warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
                    });
                }
            }
        });
        messageThread.setDaemon(true);
        messageThread.start();
    }

    private void processServerMessage(String messageText) {
        try {
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
                    warning.setText("❌ " + message.getMessage());
                    warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
                    break;
                default:
                    System.out.println("Unknown message type: " + message.getType());
            }
        } catch (Exception e) {
            System.err.println("Error processing server message: " + e.getMessage());
        }
    }

    private void handleBidPlaced(BidMessage message) {
        currentBid = message.getCurrentHighestBid();
        bidCount++;

        // Update UI
        updateBidDisplay();

        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        waktuDinamis.setText(currentTime.format(timeFormatter));

        namaPemilikDinamis.setText("Leading: " + message.getCurrentWinner());

        // Check if this is my bid
        if (message.getUsername().equals(instanceName)) {
            warning.setText("✅ Your bid placed successfully!");
            warning.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px;");
            inputBid.clear();
        } else {
            warning.setText("🔔 New bid by " + message.getUsername());
            warning.setStyle("-fx-text-fill: #0D6EFD; -fx-font-size: 12px;");
        }
    }

    private void handleTimerUpdate(BidMessage message) {
        timeLeft = message.getRemainingTime();
        updateTimeDisplay();
    }

    private void handleAuctionStarted(BidMessage message) {
        auctionEnded = false;
        buttonBid.setDisable(false);
        inputBid.setDisable(false);

        currentBid = message.getCurrentHighestBid();
        updateBidDisplay();

        warning.setText("🚀 Auction started! You can now place bids.");
        warning.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px;");
    }

    private void handleAuctionEnded(BidMessage message) {
        auctionEnded = true;
        buttonBid.setDisable(true);
        inputBid.setDisable(true);

        String winner = message.getCurrentWinner();
        BigDecimal winningBid = message.getCurrentHighestBid();

        if (winner != null && winningBid != null) {
            currentBid = winningBid;
            updateBidDisplay();

            namaPemilikDinamis.setText("🏆 Winner: " + winner);

            if (winner.equals(instanceName)) {
                warning.setText("🎉 Congratulations! You won the auction!");
                warning.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else {
                warning.setText("🏁 Auction ended. Winner: " + winner);
                warning.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 14px;");
            }
        }

        detikDinamis.setText("0");
        detikStatis.setText("ENDED");
        detikStatis.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
    }

    private void handleUserJoined(BidMessage message) {
        // Update connected bidders count (simplified)
        if (lblConnectedBidders != null) {
            try {
                String currentText = lblConnectedBidders.getText();
                int currentCount = Integer.parseInt(currentText.replaceAll("\\D", ""));
                lblConnectedBidders.setText("Bidders: " + (currentCount + 1));
            } catch (NumberFormatException e) {
                lblConnectedBidders.setText("Bidders: 1");
            }
        }
    }

    private void handleUserLeft(BidMessage message) {
        // Update connected bidders count (simplified)
        if (lblConnectedBidders != null) {
            try {
                String currentText = lblConnectedBidders.getText();
                int currentCount = Integer.parseInt(currentText.replaceAll("\\D", ""));
                lblConnectedBidders.setText("Bidders: " + Math.max(0, currentCount - 1));
            } catch (NumberFormatException e) {
                lblConnectedBidders.setText("Bidders: 0");
            }
        }
    }

    private void placeBid() {
        if (auctionEnded || !isConnected) {
            warning.setText("❌ Cannot place bid: auction not active or not connected");
            warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
            return;
        }

        try {
            String bidText = inputBid.getText().trim();
            if (bidText.isEmpty()) {
                warning.setText("❌ Please enter a bid amount!");
                warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
                return;
            }

            // Clean input and parse
            bidText = bidText.replaceAll("[Rp\\s,.]", "");
            BigDecimal newBid = new BigDecimal(bidText);

            // Validate bid
            BigDecimal minimumBid = currentBid.add(minimumIncrement);
            if (newBid.compareTo(minimumBid) < 0) {
                warning.setText("❌ Bid must be at least " + formatCurrency(minimumBid) + "!");
                warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
                return;
            }

            // Create bid message
            User currentUser = sessionContext != null && sessionContext.getSessionManager() != null ?
                    sessionContext.getSessionManager().getCurrentUser() : null;
            String sessionId = sessionContext != null && sessionContext.getSessionManager() != null ?
                    sessionContext.getSessionManager().getSessionId() : "guest";
            int userId = currentUser != null ? currentUser.getUserId() : 0;

            BidMessage bidMessage = BidMessage.createBidPlaced(auctionId,
                    userId, instanceName, sessionId, newBid);

            // Send to server
            out.println(bidMessage.toSocketMessage());

            warning.setText("⏳ Bid sent: " + formatCurrency(newBid));
            warning.setStyle("-fx-text-fill: #0D6EFD; -fx-font-size: 12px;");

        } catch (NumberFormatException e) {
            warning.setText("❌ Invalid bid format! Use numbers only (e.g., 1000000)");
            warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        }
    }

    private void openBidderWindow() {
        try {
            if (sessionContext != null && sessionContext.getSessionManager() != null) {
                User currentUser = sessionContext.getSessionManager().getCurrentUser();
                if (currentUser != null) {
                    Stage bidderStage = FXMLLoaderHelper.createBidderWindow(currentUser, "AUC-001");
                    bidderStage.show();
                } else {
                    showAlert("Please login first to open dedicated bidder window");
                }
            } else {
                showAlert("Please login first to open dedicated bidder window");
            }
        } catch (IOException e) {
            showAlert("Error opening bidder window: " + e.getMessage());
        }
    }

    private void handleBack() {
        try {
            cleanup();

            // Get current stage
            Stage currentStage = (Stage) backButton.getScene().getWindow();

            // Load welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/welcome.fxml"));
            Parent root = loader.load();

            Stage welcomeStage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            welcomeStage.setScene(scene);
            welcomeStage.setTitle("Smart Museum - Welcome");
            welcomeStage.setMaximized(true);

            // Close current stage and show welcome
            currentStage.close();
            welcomeStage.show();

            System.out.println("Returned to welcome from auction");
        } catch (Exception e) {
            System.out.println("Error returning to welcome: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateBidDisplay() {
        hargaDinamis.setText(formatCurrency(currentBid));
        jumlahBidDinamis.setText("(" + bidCount + " bids)");
    }

    private void updateTimeDisplay() {
        detikDinamis.setText(String.valueOf(timeLeft));
        if (timeLeft <= 5) {
            detikDinamis.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 48px;");
            detikStatis.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 24px;");
        } else if (timeLeft <= 10) {
            detikDinamis.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-font-size: 48px;");
            detikStatis.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-font-size: 24px;");
        } else {
            detikDinamis.setStyle("-fx-text-fill: #3B82F6; -fx-font-weight: bold; -fx-font-size: 48px;");
            detikStatis.setStyle("-fx-text-fill: #3B82F6; -fx-font-size: 24px;");
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return currencyFormat.format(amount).replace("Rp", "Rp ").replace(",00", "");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        if (isConnected) {
            isConnected = false;
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}