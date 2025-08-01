package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.smartmuseum.model.service.ChatbotService;
import org.example.smartmuseum.model.entity.UserChatSession;
import org.example.smartmuseum.model.entity.ChatLog;
import org.example.smartmuseum.model.entity.ChatbotResponse;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for Chatbot UI interactions with database integration and sound support
 */
public class ChatbotController implements Initializable {

    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatContainer;
    @FXML private TextField userInputField;
    @FXML private Button sendButton;
    @FXML private Button clearChatButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;
    @FXML private Label sessionLabel;

    private ChatbotService chatbotService;
    private UserChatSession currentSession;
    private int currentUserId = 1;
    private boolean isProcessing = false;

    // Sound management
    private MediaPlayer currentMediaPlayer;
    private static final String SOUND_DIRECTORY = "src/main/resources/sounds/"; // Adjust path as needed

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeChatbot();
        setupUI();
        startChatSession();
    }

    /**
     * Initialize chatbot service and load conversation flows
     */
    private void initializeChatbot() {
        try {
            chatbotService = new ChatbotService();
            chatbotService.loadConversationFlows();
            updateStatus("Chatbot initialized successfully");
        } catch (Exception e) {
            updateStatus("Error initializing chatbot: " + e.getMessage());
            showErrorAlert("Chatbot Initialization Error",
                    "Failed to initialize chatbot service: " + e.getMessage());
        }
    }

    /**
     * Setup UI components and event handlers
     */
    private void setupUI() {
        // Auto-scroll to bottom when new messages are added
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
        });

        // Enable/disable send button based on input
        userInputField.textProperty().addListener((obs, oldText, newText) -> {
            sendButton.setDisable(newText.trim().isEmpty() || isProcessing);
        });

        // Initial state
        sendButton.setDisable(true);

        // Focus on input field
        Platform.runLater(() -> userInputField.requestFocus());
    }

    /**
     * Start new chat session and load chat history
     */
    private void startChatSession() {
        try {
            currentSession = chatbotService.initializeChatSession(currentUserId);
            updateSessionInfo();

            // Load existing chat history from database
            loadChatHistory();

            // If no history, send welcome message
            if (chatContainer.getChildren().isEmpty()) {
                ChatbotResponse welcomeResponse = chatbotService.getWelcomeResponse();
                addBotMessageToUI(welcomeResponse.getMessage());
                // Save welcome message to database
                saveBotMessageToDatabase(welcomeResponse.getMessage());
                // Play welcome sound
                if (welcomeResponse.hasSound()) {
                    playSound(welcomeResponse.getSoundFile());
                }
            }

            updateStatus("Chat session started");
        } catch (Exception e) {
            updateStatus("Error starting chat session: " + e.getMessage());
            addBotMessageToUI("Maaf, terjadi kesalahan saat memulai sesi chat. Silakan coba lagi.");
        }
    }

    /**
     * Play sound file and stop any currently playing sound
     */
    private void playSound(String soundFileName) {
        try {
            // Stop current sound if playing
            stopCurrentSound();

            // Build sound file path
            String soundPath = SOUND_DIRECTORY + soundFileName;
            File soundFile = new File(soundPath);

            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + soundPath);
                return;
            }
    
            // Create and play new sound
            Media media = new Media(soundFile.toURI().toString());
            currentMediaPlayer = new MediaPlayer(media);

            // Auto-cleanup when sound finishes
            currentMediaPlayer.setOnEndOfMedia(() -> {
                if (currentMediaPlayer != null) {
                    currentMediaPlayer.dispose();
                    currentMediaPlayer = null;
                }
            });

            // Handle errors
            currentMediaPlayer.setOnError(() -> {
                System.err.println("Error playing sound: " + currentMediaPlayer.getError());
                if (currentMediaPlayer != null) {
                    currentMediaPlayer.dispose();
                    currentMediaPlayer = null;
                }
            });

            currentMediaPlayer.play();
            System.out.println("Playing sound: " + soundFileName);

        } catch (Exception e) {
            System.err.println("Error playing sound " + soundFileName + ": " + e.getMessage());
        }
    }

    /**
     * Stop currently playing sound
     */
    private void stopCurrentSound() {
        if (currentMediaPlayer != null) {
            try {
                currentMediaPlayer.stop();
                currentMediaPlayer.dispose();
                currentMediaPlayer = null;
                System.out.println("Stopped current sound");
            } catch (Exception e) {
                System.err.println("Error stopping current sound: " + e.getMessage());
            }
        }
    }

    /**
     * Load chat history from database and display in UI
     */
    private void loadChatHistory() {
        try {
            if (currentSession != null) {
                List<ChatLog> chatHistory = chatbotService.getChatHistory(currentSession.getSessionId());

                for (ChatLog log : chatHistory) {
                    if (log.isUserMessage()) {
                        addUserMessageToUI(log.getMessageText(), log.getTimestamp());
                    } else {
                        addBotMessageToUI(log.getMessageText(), log.getTimestamp());
                    }
                }

                System.out.println("Loaded " + chatHistory.size() + " messages from database");
            }
        } catch (Exception e) {
            System.err.println("Error loading chat history: " + e.getMessage());
        }
    }

    /**
     * Handle send message action
     */
    @FXML
    private void handleSendMessage() {
        String userInput = userInputField.getText().trim();
        if (userInput.isEmpty() || isProcessing) {
            return;
        }

        // Add user message to UI immediately
        addUserMessageToUI(userInput);

        // SAVE USER MESSAGE TO DATABASE IMMEDIATELY
        saveUserMessageToDatabase(userInput);

        userInputField.clear();

        // Process input asynchronously
        processUserInputAsync(userInput);
    }

    /**
     * Process user input asynchronously to prevent UI blocking
     */
    private void processUserInputAsync(String input) {
        isProcessing = true;
        updateStatus("Processing...");
        sendButton.setDisable(true);

        CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate processing delay for better UX
                Thread.sleep(500);
                // Process through chatbot service (this will generate response with sound info)
                return chatbotService.generateResponseWithSound(currentUserId, input);
            } catch (Exception e) {
                return new ChatbotResponse("Maaf, terjadi kesalahan saat memproses input Anda: " + e.getMessage());
            }
        }).thenAcceptAsync(response -> {
            Platform.runLater(() -> {
                // Add bot response to UI
                addBotMessageToUI(response.getMessage());

                // SAVE BOT RESPONSE TO DATABASE IMMEDIATELY
                saveBotResponseToDatabase(response.getMessage());

                // PLAY SOUND IF AVAILABLE
                if (response.hasSound()) {
                    playSound(response.getSoundFile());
                }

                isProcessing = false;
                updateStatus("Ready");
                sendButton.setDisable(userInputField.getText().trim().isEmpty());
                userInputField.requestFocus();
            });
        });
    }

    /**
     * Add user message to UI only
     */
    private void addUserMessageToUI(String message) {
        addUserMessageToUI(message, LocalDateTime.now());
    }

    /**
     * Add user message to UI with specific timestamp
     */
    private void addUserMessageToUI(String message, LocalDateTime timestamp) {
        VBox messageBox = createMessageBox(message, true, timestamp);
        chatContainer.getChildren().add(messageBox);
    }

    /**
     * Add bot message to UI only
     */
    private void addBotMessageToUI(String message) {
        addBotMessageToUI(message, LocalDateTime.now());
    }

    /**
     * Add bot message to UI with specific timestamp
     */
    private void addBotMessageToUI(String message, LocalDateTime timestamp) {
        VBox messageBox = createMessageBox(message, false, timestamp);
        chatContainer.getChildren().add(messageBox);

        // Check if we need to add navigation buttons
        addNavigationButtonsIfNeeded(message);
    }

    /**
     * Add navigation buttons based on bot response content
     */
    private void addNavigationButtonsIfNeeded(String message) {
        // Check if this is artwork info response (option 1)
        if (message.contains("Cari berdasarkan seniman") ||
                message.contains("Cari berdasarkan kategori") ||
                message.contains("Artwork terpopuler")) {

            Button goToArtworkButton = new Button("🎨 Go to Artwork Gallery");
            goToArtworkButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
            goToArtworkButton.setOnAction(e -> openArtworkList());

            HBox buttonContainer = new HBox();
            buttonContainer.setAlignment(Pos.CENTER_LEFT);
            buttonContainer.setPadding(new Insets(10, 15, 5, 15));
            buttonContainer.getChildren().add(goToArtworkButton);

            chatContainer.getChildren().add(buttonContainer);
        }

        // Check if this is auction guide response (option 2)
        else if (message.contains("Registrasi akun") ||
                message.contains("Verifikasi identitas") ||
                message.contains("Deposit jaminan") ||
                message.contains("Mulai bidding")) {

            Button goToAuctionButton = new Button("🔨 Go to Auction");
            goToAuctionButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 5;");
            goToAuctionButton.setOnAction(e -> openAuction());

            HBox buttonContainer = new HBox();
            buttonContainer.setAlignment(Pos.CENTER_LEFT);
            buttonContainer.setPadding(new Insets(10, 15, 5, 15));
            buttonContainer.getChildren().add(goToAuctionButton);

            chatContainer.getChildren().add(buttonContainer);
        }
    }

    /**
     * Open artwork list window and close chatbot
     */
    private void openArtworkList() {
        try {
            // Stop any playing sound
            stopCurrentSound();

            // End chat session before closing
            if (currentSession != null) {
                chatbotService.endChatSession(currentUserId);
            }

            // Get current chatbot stage
            Stage chatbotStage = (Stage) backButton.getScene().getWindow();

            // Load artwork list
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/view/artwork-list.fxml"));
            Parent root = loader.load();

            Stage artworkStage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/artwork-style.css").toExternalForm());

            artworkStage.setScene(scene);
            artworkStage.setTitle("Smart Museum - Artwork Gallery");
            artworkStage.setMaximized(true);

            // Close chatbot and show artwork list
            chatbotStage.close();
            artworkStage.show();

            System.out.println("Opened artwork list from chatbot and closed chatbot");
        } catch (Exception e) {
            showErrorAlert("Navigation Error", "Failed to open artwork gallery: " + e.getMessage());
        }
    }

    /**
     * Open auction window and close chatbot
     */
    private void openAuction() {
        try {
            // Stop any playing sound
            stopCurrentSound();

            // End chat session before closing
            if (currentSession != null) {
                chatbotService.endChatSession(currentUserId);
            }

            // Get current chatbot stage
            Stage chatbotStage = (Stage) backButton.getScene().getWindow();

            // Load auction
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/view/lelang.fxml"));
            Parent root = loader.load();

            Stage auctionStage = new Stage();
            Scene scene = new Scene(root);

            auctionStage.setScene(scene);
            auctionStage.setTitle("SeniMatic - Auction");
            auctionStage.setMaximized(true);

            // Close chatbot and show auction
            chatbotStage.close();
            auctionStage.show();

            System.out.println("Opened auction from chatbot and closed chatbot");
        } catch (Exception e) {
            showErrorAlert("Navigation Error", "Failed to open auction: " + e.getMessage());
        }
    }

    /**
     * Save bot message to database (for welcome messages, etc.)
     */
    private void saveBotMessageToDatabase(String message) {
        try {
            if (currentSession != null) {
                // Create a ChatLog entry for bot message
                ChatLog chatLog = new ChatLog(currentSession.getSessionId(), message, false);
                // Save through chatbot service
                chatbotService.saveChatMessage(chatLog);
            }
        } catch (Exception e) {
            System.err.println("Error saving bot message to database: " + e.getMessage());
        }
    }

    /**
     * Save user message to database immediately
     */
    private void saveUserMessageToDatabase(String message) {
        try {
            if (currentSession != null) {
                ChatLog chatLog = new ChatLog(currentSession.getSessionId(), message, true);
                boolean saved = chatbotService.saveChatMessage(chatLog);
                if (saved) {
                    System.out.println("     USER MESSAGE SAVED: " + message);
                } else {
                    System.err.println("    FAILED TO SAVE USER MESSAGE: " + message);
                }
            }
        } catch (Exception e) {
            System.err.println("    ERROR saving user message to database: " + e.getMessage());
        }
    }

    /**
     * Save bot response to database immediately
     */
    private void saveBotResponseToDatabase(String message) {
        try {
            if (currentSession != null) {
                ChatLog chatLog = new ChatLog(currentSession.getSessionId(), message, false);
                boolean saved = chatbotService.saveChatMessage(chatLog);
                if (saved) {
                    System.out.println("     BOT RESPONSE SAVED: " + message.substring(0, Math.min(50, message.length())) + "...");
                } else {
                    System.err.println("    FAILED TO SAVE BOT RESPONSE");
                }
            }
        } catch (Exception e) {
            System.err.println("    ERROR saving bot response to database: " + e.getMessage());
        }
    }

    /**
     * Create message box for chat display
     */
    private VBox createMessageBox(String message, boolean isUser, LocalDateTime timestamp) {
        VBox messageBox = new VBox(5);
        messageBox.setPadding(new Insets(8, 12, 8, 12));
        messageBox.setMaxWidth(400);

        // Message content
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));

        // Timestamp
        Label timestampLabel = new Label(formatTimestamp(timestamp));
        timestampLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        timestampLabel.setTextFill(Color.GRAY);

        if (isUser) {
            // User message styling
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageBox.setStyle("-fx-background-color: #007ACC; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);");
            messageLabel.setTextFill(Color.WHITE);
            timestampLabel.setAlignment(Pos.CENTER_RIGHT);

            HBox container = new HBox();
            container.setAlignment(Pos.CENTER_RIGHT);
            container.getChildren().add(messageBox);

            VBox wrapper = new VBox(2);
            wrapper.setAlignment(Pos.CENTER_RIGHT);
            wrapper.getChildren().addAll(container, timestampLabel);

            messageBox.getChildren().add(messageLabel);
            return wrapper;
        } else {
            // Bot message styling
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageBox.setStyle("-fx-background-color: #F0F0F0; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);");
            messageLabel.setTextFill(Color.BLACK);

            HBox container = new HBox();
            container.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().add(messageBox);

            VBox wrapper = new VBox(2);
            wrapper.setAlignment(Pos.CENTER_LEFT);
            wrapper.getChildren().addAll(container, timestampLabel);

            messageBox.getChildren().add(messageLabel);
            return wrapper;
        }
    }

    /**
     * Handle clear chat action
     */
    @FXML
    private void handleClearChat() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Clear Chat");
        confirmAlert.setHeaderText("Clear Chat History");
        confirmAlert.setContentText("Are you sure you want to clear the chat history? This will also clear the database records.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Stop any playing sound
                stopCurrentSound();

                // Clear UI
                chatContainer.getChildren().clear();

                // End current session and start new one
                chatbotService.endChatSession(currentUserId);
                startChatSession();
            }
        });
    }

    /**
     * Handle back to welcome action
     */
    @FXML
    private void handleBack() {
        try {
            // Stop any playing sound
            stopCurrentSound();

            // End chat session before closing
            if (currentSession != null) {
                chatbotService.endChatSession(currentUserId);
            }

            // Get current chatbot stage
            Stage chatbotStage = (Stage) backButton.getScene().getWindow();

            // Load welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/welcome.fxml"));
            Parent root = loader.load();

            Stage welcomeStage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            welcomeStage.setScene(scene);
            welcomeStage.setTitle("Smart Museum - Welcome");
            welcomeStage.setMaximized(true);

            // Close chatbot and show welcome
            chatbotStage.close();
            welcomeStage.show();

        } catch (Exception e) {
            showErrorAlert("Navigation Error", "Failed to return to welcome: " + e.getMessage());
        }
    }

    /**
     * Update status label
     */
    private void updateStatus(String status) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText("Status: " + status);
            }
        });
    }

    /**
     * Update session information
     */
    private void updateSessionInfo() {
        Platform.runLater(() -> {
            if (sessionLabel != null && currentSession != null) {
                String sessionInfo = currentSession.isSessionActive() ?
                        "Active (ID: " + currentSession.getSessionId() + ")" : "Inactive";
                sessionLabel.setText("Session: " + sessionInfo);
            }
        });
    }

    /**
     * Format timestamp for display
     */
    private String formatTimestamp(LocalDateTime timestamp) {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Get current timestamp for messages
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Show error alert dialog
     */
    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Get current chatbot service
     */
    public ChatbotService getChatbotService() {
        return chatbotService;
    }

    /**
     * Get current chat session
     */
    public UserChatSession getCurrentSession() {
        return currentSession;
    }

    /**
     * Set current user ID (called from login)
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        if (currentSession != null) {
            chatbotService.endChatSession(this.currentUserId);
            startChatSession(); // Restart session with new user
        }
    }

    /**
     * Refresh chat from database
     */
    public void refreshChatFromDatabase() {
        chatContainer.getChildren().clear();
        loadChatHistory();
    }

    /**
     * Cleanup resources when controller is destroyed
     */
    public void cleanup() {
        stopCurrentSound();
    }
}