package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.smartmuseum.model.abstracts.BaseUser;
import org.example.smartmuseum.model.entity.Auction;
import org.example.smartmuseum.model.entity.Artwork;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.enums.UserRole;
import org.example.smartmuseum.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import org.example.smartmuseum.model.service.ArtworkService;
import org.example.smartmuseum.model.service.AuctionService;
import org.example.smartmuseum.model.service.UserService;
import org.example.smartmuseum.database.DatabaseManager;

public class DashboardController implements Initializable {

    // Header elements
    @FXML private Label lblPageTitle;
    @FXML private Label lblPageSubtitle;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblUserInfo;

    // Navigation buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnAttendance;
    @FXML private Button btnEmployees;
    @FXML private Button btnUsers;
    @FXML private Button btnArtworks;
    @FXML private Button btnAuctions;
    @FXML private Button btnQRGenerator;
    @FXML private Button btnProfile;

    // Content area
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardContent;
    @FXML private VBox activityList;

    // Statistics labels
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalArtworks;
    @FXML private Label lblActiveAuctions;
    @FXML private Label lblTodayAttendance;

    private Timer clockTimer;
    private Button currentActiveButton;

    // Services
    private UserService userService;
    private ArtworkService artworkService;
    private AuctionService auctionService;
    private DatabaseManager databaseManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check authorization first
        if (!checkAuthorization()) {
            return;
        }

        // Initialize Services
        userService = new UserService();
        artworkService = new ArtworkService();
        auctionService = new AuctionService();
        databaseManager = new DatabaseManager();

        setupClock();
        setupNavigation();
        loadDashboardData();

        // Set user info from session
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && lblUserInfo != null) {
            lblUserInfo.setText(currentUser.getUsername());
        } else if (lblUserInfo != null) {
            lblUserInfo.setText("Staff1tadd");
        }

        currentActiveButton = btnDashboard;
        showDashboard();

        // Set dashboard to full screen after everything is loaded
        Platform.runLater(() -> {
            try {
                if (lblUserInfo != null && lblUserInfo.getScene() != null && lblUserInfo.getScene().getWindow() != null) {
                    Stage stage = (Stage) lblUserInfo.getScene().getWindow();
                    stage.setMaximized(true);
                    System.out.println("Dashboard set to full screen");
                }
            } catch (Exception e) {
                System.err.println("Could not set full screen: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private boolean checkAuthorization() {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        // Only STAFF and BOSS can access dashboard
        if (currentUser != null) {
            UserRole userRole = currentUser.getRole();
            if (userRole != UserRole.STAFF && userRole != UserRole.BOSS) {
                showUnauthorizedAlert("Access denied. Dashboard is only available for Staff and Boss.");
                redirectToWelcome();
                return false;
            }
        } else {
            // If no user in session, assume STAFF for testing
            System.out.println("No user in session, allowing access for testing");
        }

        return true;
    }

    private void showUnauthorizedAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Unauthorized Access");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void redirectToWelcome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/welcome.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblUserInfo.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Smart Museum - Welcome");
            stage.setMaximized(true);

        } catch (IOException e) {
            System.err.println("Error loading welcome screen: " + e.getMessage());
        }
    }

    private void setupClock() {
        if (lblCurrentTime != null) {
            clockTimer = new Timer(true);
            clockTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (lblCurrentTime != null) {
                            String currentTime = LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                            lblCurrentTime.setText(currentTime);
                        }
                    });
                }
            }, 0, 1000);
        }
    }

    private void setupNavigation() {
        Button[] navButtons = {btnDashboard, btnAttendance, btnEmployees, btnUsers,
                btnArtworks, btnAuctions, btnQRGenerator, btnProfile};

        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().removeAll("nav-item-active");
            }
        }
    }

    private void setActiveNavButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().removeAll("nav-item-active");
        }
        if (button != null) {
            button.getStyleClass().add("nav-item-active");
            currentActiveButton = button;
        }
    }

    private void loadDashboardData() {
        Platform.runLater(() -> {
            try {
                // Load statistics from database using DatabaseManager
                List<BaseUser> users = databaseManager.getAllUsers();
                List<Artwork> artworks = databaseManager.getAllArtworks();
                List<Auction> activeAuctions = databaseManager.getActiveAuctions();
                int todayAttendance = databaseManager.getTodayAttendanceCount();

                if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(users.size()));
                if (lblTotalArtworks != null) lblTotalArtworks.setText(String.valueOf(artworks.size()));
                if (lblActiveAuctions != null) lblActiveAuctions.setText(String.valueOf(activeAuctions.size()));
                if (lblTodayAttendance != null) lblTodayAttendance.setText(String.valueOf(todayAttendance));

                // Load recent activity
                loadRecentActivity();

                System.out.println("Dashboard data loaded successfully:");
                System.out.println("- Total Users: " + users.size());
                System.out.println("- Total Artworks: " + artworks.size());
                System.out.println("- Active Auctions: " + activeAuctions.size());
                System.out.println("- Today Attendance: " + todayAttendance);

            } catch (Exception e) {
                System.err.println("Error loading dashboard data: " + e.getMessage());
                e.printStackTrace();

                // Set default values if database fails
                if (lblTotalUsers != null) lblTotalUsers.setText("0");
                if (lblTotalArtworks != null) lblTotalArtworks.setText("0");
                if (lblActiveAuctions != null) lblActiveAuctions.setText("0");
                if (lblTodayAttendance != null) lblTodayAttendance.setText("0");
            }
        });
    }

    private void loadRecentActivity() {
        if (activityList != null) {
            activityList.getChildren().clear();

            try {
                List<Auction> recentAuctions = databaseManager.getAllAuctions();
                List<Artwork> recentArtworks = databaseManager.getAllArtworks();

                // Add recent auction activities
                for (int i = 0; i < Math.min(3, recentAuctions.size()); i++) {
                    Auction auction = recentAuctions.get(i);
                    HBox activityItem = createActivityItem("🔨",
                            "Lelang: " + auction.getStatus().getValue(),
                            "Beberapa menit yang lalu");
                    activityList.getChildren().add(activityItem);
                }

                // Add recent artwork activities
                for (int i = 0; i < Math.min(2, recentArtworks.size()); i++) {
                    Artwork artwork = recentArtworks.get(i);
                    HBox activityItem = createActivityItem("🎨",
                            "Karya seni: " + artwork.getTitle(),
                            "1 jam yang lalu");
                    activityList.getChildren().add(activityItem);
                }

            } catch (Exception e) {
                System.err.println("Error loading recent activity: " + e.getMessage());

                // Add default activity items
                HBox defaultItem = createActivityItem("ℹ️",
                        "Sistem berjalan normal",
                        "Sekarang");
                activityList.getChildren().add(defaultItem);
            }
        }
    }

    private HBox createActivityItem(String icon, String text, String time) {
        HBox item = new HBox();
        item.setSpacing(15.0);
        item.getStyleClass().add("activity-item");

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("activity-icon");

        VBox textBox = new VBox();
        textBox.setSpacing(2.0);

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("activity-text");

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("activity-time");

        textBox.getChildren().addAll(textLabel, timeLabel);
        item.getChildren().addAll(iconLabel, textBox);

        return item;
    }

    @FXML
    private void showDashboard() {
        setActiveNavButton(btnDashboard);
        if (lblPageTitle != null) lblPageTitle.setText("Hello! Smart Museum");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Selamat datang di sistem manajemen museum digital");

        if (dashboardContent != null) {
            showContent(dashboardContent);
        }
        loadDashboardData();
    }

    @FXML
    private void showAttendance() {
        setActiveNavButton(btnAttendance);
        if (lblPageTitle != null) lblPageTitle.setText("Attendance Scanner");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Scan QR codes for employee attendance tracking");

        loadAndShowContent("/org/example/smartmuseum/fxml/attendance-scanner.fxml");
    }

    @FXML
    private void showEmployees() {
        setActiveNavButton(btnEmployees);
        if (lblPageTitle != null) lblPageTitle.setText("Employee Management");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Manage employee information and QR codes");

        loadAndShowContent("/org/example/smartmuseum/fxml/employee-management.fxml");
    }

    @FXML
    private void showUsers() {
        setActiveNavButton(btnUsers);
        if (lblPageTitle != null) lblPageTitle.setText("User Management");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Manage system users and their roles");

        loadAndShowContent("/org/example/smartmuseum/fxml/user-management.fxml");
    }

    @FXML
    public void showArtworks() {
        setActiveNavButton(btnArtworks);
        if (lblPageTitle != null) lblPageTitle.setText("Artwork Management");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Kelola koleksi seni dan artefak museum");

        loadAndShowContent("/org/example/smartmuseum/fxml/artwork-management.fxml");
    }

    @FXML
    public void showAuctions() {
        setActiveNavButton(btnAuctions);
        if (lblPageTitle != null) lblPageTitle.setText("Lelang Terkini");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Kelola lelang yang sedang berlangsung");

        loadAndShowContent("/org/example/smartmuseum/fxml/auction-management.fxml");
    }

    @FXML
    private void showQRGenerator() {
        setActiveNavButton(btnQRGenerator);
        if (lblPageTitle != null) lblPageTitle.setText("QR Code Generator");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Generate QR codes for employees and artworks");

        loadAndShowContent("/org/example/smartmuseum/fxml/qr-generator.fxml");
    }

    @FXML
    private void showProfile() {
        setActiveNavButton(btnProfile);
        if (lblPageTitle != null) lblPageTitle.setText("Profile Settings");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Manage your profile and account settings");

        loadAndShowContent("/org/example/smartmuseum/fxml/profile.fxml");
    }

    @FXML
    private void handleBackToWelcome() {
        try {
            // Clean up resources
            shutdown();

            // Load welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/welcome.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblUserInfo.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Smart Museum - Welcome");
            stage.setMaximized(true);

        } catch (IOException e) {
            System.err.println("Error loading welcome screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAndShowContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            String css = getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm();
            content.getStylesheets().add(css);

            showContent(content);
        } catch (IOException e) {
            System.err.println("Error loading content: " + fxmlPath);
            e.printStackTrace();
            showPlaceholderContent("Error", "Failed to load content: " + e.getMessage());
        }
    }

    private void showContent(Node content) {
        if (contentArea != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        }
    }

    private void showPlaceholderContent(String title, String message) {
        VBox placeholder = new VBox();
        placeholder.setSpacing(20);
        placeholder.setStyle("-fx-alignment: center; -fx-padding: 50;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");

        placeholder.getChildren().addAll(titleLabel, messageLabel);
        showContent(placeholder);
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear session
            SessionManager.getInstance().logout();

            // Clean up resources
            shutdown();

            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblUserInfo.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Smart Museum - Login");
            stage.setMaximized(false);
            stage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (clockTimer != null) {
            clockTimer.cancel();
        }
    }
}
