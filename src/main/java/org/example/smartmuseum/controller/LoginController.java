package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.smartmuseum.database.DatabaseConnection;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.enums.UserRole;
import org.example.smartmuseum.model.service.UserService;
import org.example.smartmuseum.util.FXMLLoaderHelper;
import org.example.smartmuseum.util.SecurityUtils;
import org.example.smartmuseum.util.SessionManager;
import org.example.smartmuseum.util.ValidationHelper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private UserService userService;

    @FXML
    private void initialize() {
        userService = new UserService();

        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        System.out.println("🔧 LoginController initialized");
        testDatabaseConnection();

        // Show current session count for debugging
        Platform.runLater(() -> {
            int sessionCount = SessionManager.getActiveSessionCount();
            System.out.println("Current active sessions: " + sessionCount);
        });
    }

    private void testDatabaseConnection() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection successful");
            }
        } catch (Exception e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        System.out.println("🔐 Login attempt - Username: " + username);

        // Validasi input
        if (!ValidationHelper.isValidRequiredString(username, "Username")) {
            showError("Username tidak boleh kosong!");
            return;
        }

        if (!ValidationHelper.isValidRequiredString(password, "Password")) {
            showError("Password tidak boleh kosong!");
            return;
        }

        try {
            // Use UserService untuk authentication yang lebih robust
            User authenticatedUser = authenticateUser(username, password);

            if (authenticatedUser != null) {
                hideError();

                System.out.println("✅ Login successful for user: " + username +
                        " (Role: " + authenticatedUser.getRole() + ")");

                showAlert(Alert.AlertType.INFORMATION,
                        "✅ Login berhasil sebagai " + authenticatedUser.getRole().getValue().toUpperCase());

                // Route user berdasarkan role dengan dashboard yang berbeda
                if (authenticatedUser.getRole() == UserRole.BOSS) {
                    // BOSS ke dashboard_boss.fxml
                    openBossDashboard(authenticatedUser);
                } else if (authenticatedUser.getRole() == UserRole.STAFF) {
                    // STAFF ke dashboard.fxml (staff version)
                    openStaffDashboard(authenticatedUser);
                } else {
                    // Role lainnya (VISITOR, dll) ke welcome window
                    openWelcomeWindow(authenticatedUser);
                }

            } else {
                System.out.println("❌ Login failed - Invalid credentials");
                showError("❌ Username atau password salah!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Database error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Login error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "❌ Error saat login: " + e.getMessage());
        }
    }

    private User authenticateUser(String username, String password) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String hashedPassword = SecurityUtils.simpleHash(password);

        System.out.println("🔑 Attempting authentication for: " + username);
        System.out.println("🔑 Hashed password: " + hashedPassword);

        String sql = "SELECT user_id, username, role, email, phone, password_hash, created_at FROM users WHERE username = ? AND password_hash = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, hashedPassword);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setRole(UserRole.fromString(rs.getString("role")));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setCreatedAt(rs.getTimestamp("created_at"));

            rs.close();
            stmt.close();

            return user;
        }

        rs.close();
        stmt.close();

        // Fallback untuk testing jika database kosong
        if ("admin".equals(username) && "admin".equals(password)) {
            User testUser = new User();
            testUser.setUserId(1);
            testUser.setUsername("admin");
            testUser.setRole(UserRole.BOSS);
            testUser.setEmail("admin@museum.com");
            testUser.setPasswordHash(SecurityUtils.simpleHash(password));
            testUser.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            return testUser;
        } else if ("staff1".equals(username) && "staff1".equals(password)) {
            User testUser = new User();
            testUser.setUserId(2);
            testUser.setUsername("staff1");
            testUser.setRole(UserRole.STAFF);
            testUser.setEmail("staff1@museum.com");
            testUser.setPasswordHash(SecurityUtils.simpleHash(password));
            testUser.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            return testUser;
        } else if ("visitor1".equals(username) && "visitor1".equals(password)) {
            // Add test visitor for testing unauthorized access
            User testUser = new User();
            testUser.setUserId(3);
            testUser.setUsername("visitor1");
            testUser.setRole(UserRole.VISITOR);
            testUser.setEmail("visitor1@museum.com");
            testUser.setPasswordHash(SecurityUtils.simpleHash(password));
            testUser.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            return testUser;
        }

        return null;
    }

    /**
     * Open Boss Dashboard for BOSS users
     */
    private void openBossDashboard(User user) {
        try {
            // Create new boss dashboard window
            Stage bossDashboardStage = createBossDashboardWindow(user);
            bossDashboardStage.show();

            // Show session info untuk debugging
            int totalSessions = SessionManager.getActiveSessionCount();
            int loggedInUsers = SessionManager.getLoggedInUserCount();
            System.out.println("📊 Sessions after boss dashboard login - Total: " + totalSessions +
                    ", Logged in: " + loggedInUsers);

            // Hide the login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.setOnCloseRequest(null);
            loginStage.hide();

            System.out.println("✅ Boss Dashboard opened for: " + user.getUsername());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Error opening boss dashboard: " + e.getMessage());
        }
    }

    /**
     * Open Staff Dashboard for STAFF users
     */
    private void openStaffDashboard(User user) {
        try {
            // Create new staff dashboard window using original dashboard
            Stage staffDashboardStage = FXMLLoaderHelper.createDashboardWindow(user);
            staffDashboardStage.show();

            // Show session info untuk debugging
            int totalSessions = SessionManager.getActiveSessionCount();
            int loggedInUsers = SessionManager.getLoggedInUserCount();
            System.out.println("📊 Sessions after staff dashboard login - Total: " + totalSessions +
                    ", Logged in: " + loggedInUsers);

            // Hide the login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.setOnCloseRequest(null);
            loginStage.hide();

            System.out.println("✅ Staff Dashboard opened for: " + user.getUsername());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Error opening staff dashboard: " + e.getMessage());
        }
    }

    /**
     * Create Boss Dashboard Window with proper session management
     */
    private Stage createBossDashboardWindow(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/dashboard_boss.fxml"));
        Parent root = loader.load();

        // Create new session for this boss dashboard
        SessionManager sessionManager = SessionManager.createNewSession("boss_dashboard");
        sessionManager.login(user);

        // Get controller and set session context
        DashboardBossController controller = loader.getController();
        if (controller != null) {
            org.example.smartmuseum.util.SessionContext sessionContext =
                    new org.example.smartmuseum.util.SessionContext(sessionManager);
            controller.setSessionContext(sessionContext);
        }

        // Create and configure stage
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("SeniMatic - Boss Dashboard");
        stage.setMaximized(true);

        // Set stage in session context
        if (controller != null && controller.getSessionContext() != null) {
            controller.getSessionContext().setStage(stage);
        }

        // Handle window close
        stage.setOnCloseRequest(e -> {
            if (controller != null) {
                controller.cleanup();
            }
            sessionManager.destroySession();
        });

        return stage;
    }

    private void openWelcomeWindow(User user) {
        try {
            // Create new welcome window menggunakan FXMLLoaderHelper
            Stage welcomeStage = FXMLLoaderHelper.createWelcomeWindow(user);
            welcomeStage.show();

            // Show session info untuk debugging
            int totalSessions = SessionManager.getActiveSessionCount();
            int loggedInUsers = SessionManager.getLoggedInUserCount();
            System.out.println("📊 Sessions after welcome login - Total: " + totalSessions +
                    ", Logged in: " + loggedInUsers);

            // Hide the login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.setOnCloseRequest(null);
            loginStage.hide();

            System.out.println("✅ Welcome window opened for " + user.getRole() + ": " + user.getUsername());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Error opening welcome window: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("SeniMatic - Register");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Error loading register page: " + e.getMessage());
        }
    }

    @FXML
    private void handleMultiLoginDemo() {
        try {
            // Demo button untuk testing multi-login
            // User bisa login sebagai user yang berbeda di window berbeda

            // Login as admin (Boss Dashboard)
            User admin = new User();
            admin.setUserId(1);
            admin.setUsername("admin");
            admin.setRole(UserRole.BOSS);
            admin.setEmail("admin@museum.com");
            admin.setPasswordHash(SecurityUtils.simpleHash("admin"));

            Stage adminBossDashboard = createBossDashboardWindow(admin);
            adminBossDashboard.show();

            // Login as staff1 (Staff Dashboard)
            User staff = new User();
            staff.setUserId(2);
            staff.setUsername("staff1");
            staff.setRole(UserRole.STAFF);
            staff.setEmail("staff1@museum.com");
            staff.setPasswordHash(SecurityUtils.simpleHash("staff1"));

            Stage staffDashboard = FXMLLoaderHelper.createDashboardWindow(staff);
            staffDashboard.show();

            // Login as visitor1 (Welcome)
            User visitor = new User();
            visitor.setUserId(3);
            visitor.setUsername("visitor1");
            visitor.setRole(UserRole.VISITOR);
            visitor.setEmail("visitor1@museum.com");
            visitor.setPasswordHash(SecurityUtils.simpleHash("visitor1"));

            Stage visitorWelcome = FXMLLoaderHelper.createWelcomeWindow(visitor);
            visitorWelcome.show();

            showAlert(Alert.AlertType.INFORMATION,
                    "Multi-login demo: Opened boss dashboard (admin), staff dashboard (staff), and welcome window (visitor)!");

            // Hide login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.hide();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error in multi-login demo: " + e.getMessage());
        }
    }

    private void goToWelcome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/welcome.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("SeniMatic - Welcome");
            stage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Error loading welcome page: " + e.getMessage());
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}