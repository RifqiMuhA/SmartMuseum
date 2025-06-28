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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.smartmuseum.model.service.ChatbotService;
import org.example.smartmuseum.model.entity.UserChatSession;
import org.example.smartmuseum.model.entity.ChatLog;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for Chatbot UI interactions with database integration
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
    private int currentUserId = 1; // Mock user ID - in real app, get from login session
    private boolean isProcessing = false;

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
                String welcomeMessage = getWelcomeMessage();
                addBotMessageToUI(welcomeMessage);
                // Save welcome message to database
                saveBotMessageToDatabase(welcomeMessage);
            }

            updateStatus("Chat session started");
        } catch (Exception e) {
            updateStatus("Error starting chat session: " + e.getMessage());
            addBotMessageToUI("Maaf, terjadi kesalahan saat memulai sesi chat. Silakan coba lagi.");
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
                // Process through chatbot service (this will generate response only)
                return chatbotService.generateResponse(currentUserId, input);
            } catch (Exception e) {
                return "Maaf, terjadi kesalahan saat memproses input Anda: " + e.getMessage();
            }
        }).thenAcceptAsync(response -> {
            Platform.runLater(() -> {
                // Add bot response to UI
                addBotMessageToUI(response);

                // SAVE BOT RESPONSE TO DATABASE IMMEDIATELY
                saveBotResponseToDatabase(response);

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
     * Get welcome message for chatbot
     */
    private String getWelcomeMessage() {
        return "Selamat datang di SeniMatic Chat Assistant!         \n\n" +
                "Saya siap membantu Anda dengan:\n" +
                "1. Informasi Artwork\n" +
                "2. Cara Mengikuti Lelang\n" +
                "3. Info Galeri\n" +
                "4. Bantuan Teknis\n\n" +
                "Ketik nomor pilihan Anda untuk memulai:";
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
}
