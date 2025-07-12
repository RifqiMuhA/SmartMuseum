package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.service.VideoCallService;
import org.example.smartmuseum.util.SessionManager;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class VideoCallController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(VideoCallController.class.getName());

    @FXML private TextField roomIdField;
    @FXML private Button joinButton;
    @FXML private Button copyUrlButton;
    @FXML private Button backToWelcomeButton;
    @FXML private Label statusLabel;
    @FXML private Label roomInfoLabel;
    @FXML private Label urlLabel;

    private VideoCallService videoCallService;
    private User currentUser;
    private String currentRoomId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls();
        loadCurrentUser();
        videoCallService = new VideoCallService();

        statusLabel.setText("Ready to join video conference");
    }

    private void setupControls() {
        joinButton.setOnAction(e -> openVideoCallInBrowser());
        copyUrlButton.setOnAction(e -> copyUrlToClipboard());
        backToWelcomeButton.setOnAction(e -> goBackToWelcome());

        // Room ID validation
        roomIdField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.trim().isEmpty()) {
                String cleanedRoomId = videoCallService.cleanRoomId(newText);
                if (!cleanedRoomId.equals(newText)) {
                    Platform.runLater(() -> roomIdField.setText(cleanedRoomId));
                }
                updateUrlPreview();
            } else {
                urlLabel.setText("");
                copyUrlButton.setDisable(true);
            }
        });

        // Initially disable copy button
        copyUrlButton.setDisable(true);
    }

    private void loadCurrentUser() {
        var activeSessions = SessionManager.getAllActiveSessions();
        for (SessionManager session : activeSessions.values()) {
            if (session.isLoggedIn()) {
                currentUser = session.getCurrentUser();
                break;
            }
        }

        if (currentUser == null) {
            currentUser = createGuestUser();
        }

        LOGGER.info("Current user for video call: " + currentUser.getUsername());
    }

    private User createGuestUser() {
        User guest = new User();
        guest.setUserId(9999);
        guest.setUsername("Visitor_" + System.currentTimeMillis());
        return guest;
    }

    private void updateUrlPreview() {
        String roomId = roomIdField.getText().trim();
        if (!roomId.isEmpty() && videoCallService.isValidRoomId(roomId)) {
            String userId = String.valueOf(currentUser.getUserId());
            String userName = currentUser.getUsername();
            if (currentUser.getRole() != null) {
                userName += " (" + currentUser.getRole() + ")";
            }

            String url = videoCallService.generateVideoCallURL(roomId, userId, userName);
            urlLabel.setText("URL: " + url);
            copyUrlButton.setDisable(false);
        } else {
            urlLabel.setText("");
            copyUrlButton.setDisable(true);
        }
    }

    @FXML
    private void openVideoCallInBrowser() {
        String roomId = roomIdField.getText().trim();

        if (!videoCallService.isValidRoomId(roomId)) {
            showAlert("Invalid Conference ID",
                    "Please enter a valid Conference ID (3-50 characters, letters, numbers, underscore, or dash only).");
            return;
        }

        try {
            String userId = String.valueOf(currentUser.getUserId());
            String userName = currentUser.getUsername();
            if (currentUser.getRole() != null) {
                userName += " (" + currentUser.getRole() + ")";
            }

            String videoCallUrl = videoCallService.generateVideoCallURL(roomId, userId, userName);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(videoCallUrl));

                statusLabel.setText("Video conference opened in browser");
                roomInfoLabel.setText("Joining: " + roomId + " as " + userName);

                showInfo("Conference Opened",
                        "Video conference has been opened in your default browser.\n\n" +
                                "Conference ID: " + roomId + "\n" +
                                "User: " + userName + "\n\n" +
                                "Make sure to allow camera and microphone access when prompted by your browser.");

                LOGGER.info("Opened video conference in browser: " + videoCallUrl);
            } else {
                throw new Exception("Desktop not supported");
            }

        } catch (Exception e) {
            LOGGER.severe("Error opening video conference: " + e.getMessage());
            statusLabel.setText("Failed to open video conference");

            // Fallback - copy URL to clipboard
            copyUrlToClipboard();
            showAlert("Error Opening Browser",
                    "Cannot open browser automatically.\n\n" +
                            "The conference URL has been copied to your clipboard.\n" +
                            "Please paste it into your browser manually.");
        }
    }

    @FXML
    private void copyUrlToClipboard() {
        String roomId = roomIdField.getText().trim();

        if (!videoCallService.isValidRoomId(roomId)) {
            showAlert("Invalid Conference ID", "Please enter a valid Conference ID first.");
            return;
        }

        try {
            String userId = String.valueOf(currentUser.getUserId());
            String userName = currentUser.getUsername();
            if (currentUser.getRole() != null) {
                userName += " (" + currentUser.getRole() + ")";
            }

            String videoCallUrl = videoCallService.generateVideoCallURL(roomId, userId, userName);

            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(videoCallUrl);
            clipboard.setContent(content);

            statusLabel.setText("Conference URL copied to clipboard");

            showInfo("URL Copied",
                    "Conference URL has been copied to clipboard:\n\n" +
                            videoCallUrl + "\n\n" +
                            "You can now paste this URL into any modern browser to join the video conference.");

        } catch (Exception e) {
            LOGGER.severe("Error copying URL: " + e.getMessage());
            showAlert("Error", "Failed to copy URL: " + e.getMessage());
        }
    }

    @FXML
    private void goBackToWelcome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/welcome.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) roomIdField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Smart Museum - Welcome");
            stage.setMaximized(true);

        } catch (IOException e) {
            LOGGER.severe("Error loading welcome screen: " + e.getMessage());
            showAlert("Navigation Error", "Failed to return to welcome screen: " + e.getMessage());
        }
    }

    // External integration methods
    public void setRoomIdForVisitor(String roomId) {
        this.currentRoomId = roomId;
        roomIdField.setText(roomId);
        roomIdField.setEditable(false);
        roomInfoLabel.setText("Joining video conference as Visitor");
        updateUrlPreview();

        // Auto-open for visitor
        Platform.runLater(() -> openVideoCallInBrowser());
    }

    public void setAsRoomCreator(String generatedRoomId) {
        this.currentRoomId = generatedRoomId;
        roomIdField.setText(generatedRoomId);
        roomIdField.setEditable(false);
        roomInfoLabel.setText("Video conference created - Conference ID: " + generatedRoomId);
        updateUrlPreview();

        // Auto-open for room creator
        Platform.runLater(() -> openVideoCallInBrowser());
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}