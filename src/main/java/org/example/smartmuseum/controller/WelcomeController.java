package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.enums.UserRole;
import org.example.smartmuseum.util.FXMLLoaderHelper;
import org.example.smartmuseum.util.SessionContext;
import org.example.smartmuseum.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import org.example.smartmuseum.controller.VideoCallController;

public class WelcomeController implements Initializable, SessionAwareController {

    @FXML private Label lblWelcomeMessage;
    @FXML private Label lblCurrentTime;
    @FXML private Button btnGoToDashboard;
    @FXML private Button btnExit;
    @FXML private Button btnMultiLoginDemo;

    private Timer clockTimer;
    private SessionContext sessionContext;

    @Override
    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
        System.out.println("WelcomeController received session context: " + sessionContext);
    }

    @Override
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupClock();
        setupWelcomeMessage();
        setupSessionMonitoring();
    }

    private void setupClock() {
        clockTimer = new Timer(true);
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (lblCurrentTime != null) {
                        String currentTime = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy - HH:mm:ss"));
                        lblCurrentTime.setText(currentTime);
                    }
                });
            }
        }, 0, 1000);
    }

    private void setupWelcomeMessage() {
        int hour = LocalDateTime.now().getHour();
        String greeting;

        if (hour < 12) {
            greeting = "Selamat Pagi!";
        } else if (hour < 17) {
            greeting = "Selamat Siang!";
        } else {
            greeting = "Selamat Malam!";
        }

        if (lblWelcomeMessage != null) {
            lblWelcomeMessage.setText(greeting + " Selamat datang di Smart Museum System.");
        }
    }

    private void setupSessionMonitoring() {
        // Monitor session count setiap 5 detik untuk debugging
        Timer sessionTimer = new Timer(true);
        sessionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    int activeCount = SessionManager.getActiveSessionCount();
                    int loggedInCount = SessionManager.getLoggedInUserCount();

                    // Update title untuk show session info
                    if (sessionContext != null && sessionContext.getStage() != null) {
                        sessionContext.getStage().setTitle(
                                String.format("Smart Museum - Welcome (Sessions: %d | Logged: %d)",
                                        activeCount, loggedInCount)
                        );
                    }
                });
            }
        }, 0, 5000);
    }

    @FXML
    private void handleGoToDashboard() {
        // FIXED: Check if any user is logged in anywhere in the system
        // Karena Welcome bisa diakses dari dashboard yang sudah login

        User userToLogin = null;

        // Check if current session context has logged in user
        if (sessionContext != null && sessionContext.getSessionManager() != null &&
                sessionContext.getSessionManager().isLoggedIn()) {
            userToLogin = sessionContext.getSessionManager().getCurrentUser();
            System.out.println("✅ Found user in current session: " + userToLogin.getUsername());
        } else {
            // Check if there's any active session with logged in user
            var activeSessions = SessionManager.getAllActiveSessions();
            for (SessionManager session : activeSessions.values()) {
                if (session.isLoggedIn()) {
                    userToLogin = session.getCurrentUser();
                    System.out.println("✅ Found user in active session: " + userToLogin.getUsername());
                    break;
                }
            }
        }

        // If no logged in user found anywhere, go to login
        if (userToLogin == null) {
            System.out.println("❌ No logged in user found, redirecting to login");
            showAlert(Alert.AlertType.INFORMATION,
                    "Please login first to access dashboard.");
            handleGoToLogin();
            return;
        }

        try {
            // Clean up timer
            if (clockTimer != null) {
                clockTimer.cancel();
            }

            // Create dashboard window dengan user yang sudah login
            Stage dashboardStage = FXMLLoaderHelper.createDashboardWindow(userToLogin);
            dashboardStage.show();

            // Close welcome window
            Stage currentStage = (Stage) btnGoToDashboard.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin() {
        try {
            // Clean up timer
            if (clockTimer != null) {
                clockTimer.cancel();
            }

            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnGoToDashboard.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Smart Museum - Login");
            stage.setMaximized(false);
            stage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Error loading login: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading login: " + e.getMessage());
        }
    }

    @FXML
    private void handleMultiLoginDemo() {
        try {
            // Demo untuk testing multi-login
            // Buat beberapa user test dan buka dashboard untuk masing-masing

            // User 1 - Admin
            User admin = createTestUser(1, "admin", UserRole.BOSS);
            Stage adminStage = FXMLLoaderHelper.createDashboardWindow(admin);
            adminStage.show();

            // User 2 - Staff1
            User staff1 = createTestUser(2, "staff1", UserRole.STAFF);
            Stage staff1Stage = FXMLLoaderHelper.createDashboardWindow(staff1);
            staff1Stage.show();

            // User 3 - Staff2
            User staff2 = createTestUser(3, "staff2", UserRole.STAFF);
            Stage staff2Stage = FXMLLoaderHelper.createDashboardWindow(staff2);
            staff2Stage.show();

            showAlert(Alert.AlertType.INFORMATION,
                    "Multi-Login Demo: Opened 3 dashboard windows!\n" +
                            "- Admin Dashboard\n" +
                            "- Staff1 Dashboard\n" +
                            "- Staff2 Dashboard\n\n" +
                            "Each window has its own independent session.");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error in multi-login demo: " + e.getMessage());
        }
    }

    private User createTestUser(int id, String username, UserRole role) {
        User user = new User();
        user.setUserId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setEmail(username + "@museum.com");
        user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return user;
    }

    @FXML
    private void handleExit() {
        // Cleanup semua sessions sebelum exit
        int activeCount = SessionManager.getActiveSessionCount();
        if (activeCount > 0) {
            System.out.println("Cleaning up " + activeCount + " active sessions...");
        }

        if (clockTimer != null) {
            clockTimer.cancel();
        }

        // Cleanup session context
        if (sessionContext != null) {
            sessionContext.cleanup();
        }

        Platform.exit();
    }

    @FXML
    private void handleLelangTerkini() {
        try {
            // Check if user is logged in
            User userToUse = null;

            // Check current session context
            if (sessionContext != null && sessionContext.getSessionManager() != null &&
                    sessionContext.getSessionManager().isLoggedIn()) {
                userToUse = sessionContext.getSessionManager().getCurrentUser();
                System.out.println("✅ Found user in current session: " + userToUse.getUsername());
            } else {
                // Check if there's any active session with logged in user
                var activeSessions = SessionManager.getAllActiveSessions();
                for (SessionManager session : activeSessions.values()) {
                    if (session.isLoggedIn()) {
                        userToUse = session.getCurrentUser();
                        System.out.println("✅ Found user in active session: " + userToUse.getUsername());
                        break;
                    }
                }
            }

            // Clean up timer
            if (clockTimer != null) {
                clockTimer.cancel();
            }

            Stage currentStage = (Stage) btnGoToDashboard.getScene().getWindow();

            if (userToUse != null) {
                // Create lelang window with user session
                System.out.println("🎯 Creating lelang window with user session: " + userToUse.getUsername());

                // Create SessionContext untuk lelang
                SessionContext lelangContext = SessionContext.createNewWindowContext("LELANG");
                lelangContext.getSessionManager().login(userToUse);

                // Load lelang.fxml dengan SessionContext
                FXMLLoaderHelper.LoadResult result = FXMLLoaderHelper.loadFXMLWithSession(
                        "/org/example/smartmuseum/view/lelang.fxml",
                        lelangContext
                );

                Scene scene = new Scene(result.getRoot());
                currentStage.setScene(scene);
                currentStage.setTitle("SeniMatic - Auction (" + userToUse.getUsername() + ")");
                currentStage.setMaximized(true);

                // Set stage ke session context
                lelangContext.setStage(currentStage);

            } else {
                // No user logged in - create guest session
                System.out.println("⚠️ No user logged in - creating guest session for lelang");

                // Create guest SessionContext
                SessionContext guestContext = SessionContext.createNewWindowContext("LELANG_GUEST");

                // Load lelang.fxml dengan guest SessionContext
                FXMLLoaderHelper.LoadResult result = FXMLLoaderHelper.loadFXMLWithSession(
                        "/org/example/smartmuseum/view/lelang.fxml",
                        guestContext
                );

                Scene scene = new Scene(result.getRoot());
                currentStage.setScene(scene);
                currentStage.setTitle("SeniMatic - Auction (Guest)");
                currentStage.setMaximized(true);

                // Set stage ke session context
                guestContext.setStage(currentStage);
            }

        } catch (IOException e) {
            System.err.println("Error loading lelang: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading auction: " + e.getMessage());
        }
    }

    @FXML
    private void handleListArtwork() {
        try {
            // Clean up timer
            if (clockTimer != null) {
                clockTimer.cancel();
            }

            // Load artwork list directly
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/view/artwork-list.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnGoToDashboard.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/artwork-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Smart Museum - List Artwork");
            stage.setMaximized(true);

        } catch (IOException e) {
            System.err.println("Error loading artwork list: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading artwork list: " + e.getMessage());
        }
    }

    @FXML
    private void handleChatbot() {
        try {
            // Clean up timer
            if (clockTimer != null) {
                clockTimer.cancel();
            }

            // Close current welcome window
            Stage currentStage = (Stage) btnGoToDashboard.getScene().getWindow();

            // Load chatbot window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/chatbot.fxml"));
            Parent chatbotWindow = loader.load();

            Stage chatbotStage = new Stage();
            chatbotStage.setTitle("Smart Museum - Chatbot Assistant");
            Scene scene = new Scene(chatbotWindow, 800, 600);
            chatbotStage.setScene(scene);

            // Close welcome and show chatbot
            currentStage.close();
            chatbotStage.show();

        } catch (IOException e) {
            System.err.println("Error loading chatbot: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading chatbot: " + e.getMessage());
        }
    }

    @FXML
    private void handleVideoCall() {
        try {
            // Create dialog untuk visitor input Room ID
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Join Video Conference");
            dialog.setHeaderText("SeniMatic Video Conference");
            dialog.setContentText("Enter Conference ID to join video conference:");

            // Custom styling untuk dialog
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm()
            );

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String roomId = result.get().trim();

                // Clean up timer
                if (clockTimer != null) {
                    clockTimer.cancel();
                }

                // Load video call window
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/video-call.fxml"));
                Parent root = loader.load();

                VideoCallController videoController = loader.getController();
                videoController.setRoomIdForVisitor(roomId);

                Stage stage = (Stage) btnGoToDashboard.getScene().getWindow();
                Scene scene = new Scene(root, 800, 600);
                scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

                stage.setScene(scene);
                stage.setTitle("SeniMatic Video Conference - " + roomId);
                stage.setMaximized(true);

            }

        } catch (IOException e) {
            System.err.println("Error loading video call: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading video call: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowActiveSessions() {
        // Debug method untuk show active sessions
        var sessions = SessionManager.getAllActiveSessions();

        StringBuilder info = new StringBuilder("Active Sessions:\n\n");
        if (sessions.isEmpty()) {
            info.append("No active sessions");
        } else {
            sessions.forEach((id, session) -> {
                info.append(String.format("Session: %s\n", id.substring(0, 8) + "..."));
                info.append(String.format("User: %s\n", session.getCurrentUsername()));
                info.append(String.format("Role: %s\n", session.getCurrentUserRole()));
                info.append(String.format("Window: %s\n", session.getWindowTitle()));
                info.append(String.format("Login: %s\n", session.getLoginTime()));
                info.append("---\n");
            });
        }

        showAlert(Alert.AlertType.INFORMATION, info.toString());
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void cleanup() {
        if (clockTimer != null) {
            clockTimer.cancel();
        }
        SessionAwareController.super.cleanup();
    }
}