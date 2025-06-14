package org.example.smartmuseum.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.smartmuseum.SmartMuseumAttendanceApp;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Button btnGoToWelcome;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblUserInfo;

    // Tab pane and tabs
    @FXML private TabPane tabPane;
    @FXML private Tab tabAttendance;
    @FXML private Tab tabEmployeeManagement;
    @FXML private Tab tabQRGenerator;

    // Content containers
    @FXML private VBox attendanceContent;
    @FXML private VBox employeeContent;
    @FXML private VBox qrGeneratorContent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setupTimeUpdater();
            setupUserInfo();
            loadTabContents();

            System.out.println("✅ Dashboard initialized successfully");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error initializing dashboard: " + e.getMessage());
        }
    }

    private void setupTimeUpdater() {
        updateTime();

        // Update time every second
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> updateTime())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        String timeString = now.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy - HH:mm:ss"));
        lblCurrentTime.setText(timeString);
    }

    private void setupUserInfo() {
        lblUserInfo.setText("Logged in as: Admin (Boss)");
    }

    private void loadTabContents() {
        try {
            // Load QR Attendance Scanner - The main feature
            loadQRAttendanceScanner();

            // Load placeholder for other features
            loadComingSoonPlaceholder(employeeContent, "👥 Employee Management",
                    "Full CRUD operations for employee management will be available soon.");

            loadComingSoonPlaceholder(qrGeneratorContent, "🏷️ QR Code Generator",
                    "QR code generation and management features coming soon.");

            System.out.println("✅ All tab contents loaded successfully");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading tab contents: " + e.getMessage());
        }
    }

    private void loadQRAttendanceScanner() {
        try {
            // Create the attendance scanner directly without external FXML
            QRAttendanceScannerController scannerController = new QRAttendanceScannerController();
            VBox scannerContent = scannerController.createScannerUI();
            attendanceContent.getChildren().clear();
            attendanceContent.getChildren().add(scannerContent);

            System.out.println("✅ QR Attendance Scanner loaded successfully");

        } catch (Exception e) {
            e.printStackTrace();
            showErrorInTab(attendanceContent, "Error loading QR Scanner: " + e.getMessage());
        }
    }

    private void loadComingSoonPlaceholder(VBox container, String title, String description) {
        container.getChildren().clear();

        VBox placeholder = new VBox(20);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.setStyle("-fx-padding: 50; -fx-background-color: #f8f9fa;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(400);

        Label comingSoonLabel = new Label("🚧 Coming Soon 🚧");
        comingSoonLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");

        placeholder.getChildren().addAll(titleLabel, descLabel, comingSoonLabel);
        container.getChildren().add(placeholder);
    }

    private void showErrorInTab(VBox container, String errorMessage) {
        container.getChildren().clear();

        VBox errorBox = new VBox(10);
        errorBox.setAlignment(javafx.geometry.Pos.CENTER);
        errorBox.setStyle("-fx-padding: 50; -fx-background-color: #ffebee;");

        Label errorLabel = new Label("❌ Error");
        errorLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        Label messageLabel = new Label(errorMessage);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #c0392b;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);

        errorBox.getChildren().addAll(errorLabel, messageLabel);
        container.getChildren().add(errorBox);
    }

    @FXML
    private void handleGoToWelcome() {
        System.out.println("Navigating to Welcome screen...");
        SmartMuseumAttendanceApp.showWelcomeScreen();
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Confirm Logout");
        alert.setContentText("Are you sure you want to logout?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("User logged out");
                SmartMuseumAttendanceApp.showWelcomeScreen();
            }
        });
    }

    @FXML
    private void handleRefreshAll() {
        try {
            System.out.println("Refreshing dashboard...");
            loadTabContents();
            System.out.println("✅ Dashboard refreshed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error refreshing dashboard: " + e.getMessage());
        }
    }
}