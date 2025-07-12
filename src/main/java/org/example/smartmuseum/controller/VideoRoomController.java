package org.example.smartmuseum.controller;

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

public class VideoRoomController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(VideoRoomController.class.getName());

    @FXML private TextField roomIdField;
    @FXML private Button generateRoomButton;
    @FXML private Button copyDetailsButton;
    @FXML private Button openInBrowserButton;
    @FXML private Label instructionLabel;
    @FXML private TextArea detailsArea;

    private VideoCallService videoCallService;
    private User currentUser;
    private String generatedRoomId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls();
        loadCurrentUser();
        videoCallService = new VideoCallService();
        updateUI();
    }

    private void setupControls() {
        generateRoomButton.setOnAction(e -> generateNewRoom());
        copyDetailsButton.setOnAction(e -> copyDetailsToClipboard());
        openInBrowserButton.setOnAction(e -> openInExternalBrowser());

        // Initially disable copy and open buttons
        copyDetailsButton.setDisable(true);
        openInBrowserButton.setDisable(true);
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
            showAlert("Error", "No user session found.");
            return;
        }
    }

    private void updateUI() {
        if (currentUser != null) {
            String role = currentUser.getRole().toString();
            instructionLabel.setText("Welcome " + role + " - Create Video Conference Room");

            detailsArea.setText(
                    "Video Conference Setup:\n\n" +
                            "1. Click 'Generate Conference ID' to create a new room\n" +
                            "2. Copy the conference details and share with participants\n" +
                            "3. Click 'Open in Browser' to start the conference\n" +
                            "4. Participants can join using the Conference ID\n\n" +
                            "Features:\n" +
                            "• HD video calls with up to 50 participants\n" +
                            "• Screen sharing and text chat\n" +
                            "• Works in any modern browser\n" +
                            "• No software installation required"
            );
        }
    }

    @FXML
    private void generateNewRoom() {
        try {
            generatedRoomId = videoCallService.generateRoomId();
            roomIdField.setText(generatedRoomId);

            // Enable copy and open buttons
            copyDetailsButton.setDisable(false);
            openInBrowserButton.setDisable(false);

            String userId = String.valueOf(currentUser.getUserId());
            String userName = currentUser.getUsername();
            if (currentUser.getRole() != null) {
                userName += " (" + currentUser.getRole() + ")";
            }

            String directUrl = videoCallService.generateVideoCallURL(generatedRoomId, userId, userName);

            detailsArea.setText(
                    "Conference Created Successfully!\n\n" +
                            "Conference ID: " + generatedRoomId + "\n" +
                            "Host: " + userName + "\n" +
                            "Direct URL: " + directUrl + "\n\n" +
                            "Share this information with participants:\n" +
                            "• Send the Conference ID: " + generatedRoomId + "\n" +
                            "• Or share the direct URL above\n\n" +
                            "How participants can join:\n" +
                            "• Visit SeniMatic welcome page\n" +
                            "• Enter Conference ID and click 'Join Video Conference'\n" +
                            "• Or paste the direct URL in any browser\n\n" +
                            "Conference Features:\n" +
                            "• Up to 50 participants\n" +
                            "• HD video and audio\n" +
                            "• Screen sharing\n" +
                            "• Text chat\n" +
                            "• No downloads required"
            );

            showInfo("Conference Room Created",
                    "Conference ID: " + generatedRoomId + "\n\n" +
                            "Your video conference room is ready!\n" +
                            "Share the Conference ID with participants or use the 'Copy Details' button.");

            LOGGER.info("Generated new room ID: " + generatedRoomId + " by user: " + currentUser.getUsername());

        } catch (Exception e) {
            LOGGER.severe("Error generating room ID: " + e.getMessage());
            showAlert("Error", "Failed to generate conference room: " + e.getMessage());
        }
    }

    @FXML
    private void copyDetailsToClipboard() {
        if (generatedRoomId != null && !generatedRoomId.isEmpty()) {
            String userId = String.valueOf(currentUser.getUserId());
            String userName = currentUser.getUsername();
            if (currentUser.getRole() != null) {
                userName += " (" + currentUser.getRole() + ")";
            }

            String directUrl = videoCallService.generateVideoCallURL(generatedRoomId, userId, userName);

            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();

            String clipboardText = "🎥 SeniMatic Video Conference\n\n" +
                    "Conference ID: " + generatedRoomId + "\n" +
                    "Host: " + userName + "\n" +
                    "Direct URL: " + directUrl + "\n\n" +
                    "How to Join:\n" +
                    "1. Visit SeniMatic app and go to Welcome page\n" +
                    "2. Enter Conference ID: " + generatedRoomId + "\n" +
                    "3. Click 'Join Video Conference'\n\n" +
                    "OR paste this direct URL in your browser:\n" +
                    directUrl + "\n\n" +
                    "Requirements:\n" +
                    "• Modern browser (Chrome, Firefox, Safari, Edge)\n" +
                    "• Camera and microphone access\n" +
                    "• Stable internet connection\n\n" +
                    "Features: HD Video, Screen Share, Chat, Up to 50 participants";

            content.putString(clipboardText);
            clipboard.setContent(content);

            showInfo("Conference Details Copied",
                    "Complete conference details have been copied to clipboard!\n\n" +
                            "You can now paste this information in:\n" +
                            "• WhatsApp or other messaging apps\n" +
                            "• Email\n" +
                            "• Any text document\n\n" +
                            "The message includes the Conference ID, direct URL, and join instructions.");
        }
    }

    @FXML
    private void openInExternalBrowser() {
        if (generatedRoomId == null || generatedRoomId.isEmpty()) {
            showAlert("Error", "Please generate a Conference ID first");
            return;
        }

        try {
            String userId = String.valueOf(currentUser.getUserId());
            String userName = currentUser.getUsername();
            if (currentUser.getRole() != null) {
                userName += " (" + currentUser.getRole() + ")";
            }

            String videoCallUrl = videoCallService.generateVideoCallURL(generatedRoomId, userId, userName);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(videoCallUrl));

                showInfo("Conference Started",
                        "Video conference has been opened in your default browser.\n\n" +
                                "Conference Details:\n" +
                                "• Conference ID: " + generatedRoomId + "\n" +
                                "• Host: " + userName + "\n\n" +
                                "Important:\n" +
                                "• Allow camera and microphone access when prompted\n" +
                                "• Keep this application open for room management\n" +
                                "• Share the Conference ID with participants\n\n" +
                                "Participants can join by entering the Conference ID on the SeniMatic welcome page.");

                LOGGER.info("Opened video conference in external browser: " + videoCallUrl);
            } else {
                // Fallback - copy URL to clipboard
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(videoCallUrl);
                clipboard.setContent(content);

                showAlert("Browser Not Available",
                        "Cannot open browser automatically.\n\n" +
                                "Conference URL has been copied to clipboard:\n" +
                                videoCallUrl + "\n\n" +
                                "Please paste this URL in your browser manually.");
            }

        } catch (Exception e) {
            LOGGER.severe("Error opening external browser: " + e.getMessage());
            showAlert("Error", "Failed to open external browser: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}