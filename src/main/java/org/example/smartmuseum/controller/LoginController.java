package org.example.smartmuseum.controller;

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
import org.example.smartmuseum.util.ValidationHelper;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    @FXML
    private void initialize() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        System.out.println("🔧 LoginController initialized");
        testDatabaseConnection();
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
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String hashedPassword = hashPassword(password);

            System.out.println("🔑 Hashed password: " + hashedPassword);

            String sql = "SELECT user_id, username, role FROM users WHERE username = ? AND password_hash = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                hideError();
                String role = rs.getString("role");
                int userId = rs.getInt("user_id");

                System.out.println("✅ Login successful for user: " + username + " (Role: " + role + ")");

                // Simpan session user
                System.setProperty("current.user.id", String.valueOf(userId));
                System.setProperty("current.user.name", username);
                System.setProperty("current.user.role", role);

                showAlert(Alert.AlertType.INFORMATION, "✅ Login berhasil sebagai " + role.toUpperCase());
                goToWelcome();
            } else {
                System.out.println("❌ Login failed - Invalid credentials");
                showError("❌ Username atau password salah!");
            }

            rs.close();
            stmt.close();

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

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
