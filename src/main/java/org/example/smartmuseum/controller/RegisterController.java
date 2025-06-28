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
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

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
        System.out.println("🔧 RegisterController initialized");
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        System.out.println("📝 Registration attempt - Username: " + username);

        // Validasi input
        if (!ValidationHelper.isValidRequiredString(username, "Username")) {
            showError("Username tidak boleh kosong!");
            return;
        }

        if (!ValidationHelper.isValidUsername(username)) {
            showError("Username harus 3-50 karakter dan hanya boleh huruf, angka, dan underscore!");
            return;
        }

        if (!ValidationHelper.isValidRequiredString(password, "Password")) {
            showError("Password tidak boleh kosong!");
            return;
        }

        if (!ValidationHelper.isValidPassword(password)) {
            showError("Password harus minimal 6 karakter!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Password dan konfirmasi password tidak cocok!");
            return;
        }

        if (!ValidationHelper.isValidRequiredString(email, "Email")) {
            showError("Email tidak boleh kosong!");
            return;
        }

        if (!ValidationHelper.isValidEmail(email)) {
            showError("Format email tidak valid!");
            return;
        }

        if (!ValidationHelper.isValidRequiredString(phone, "Phone")) {
            showError("Nomor HP tidak boleh kosong!");
            return;
        }

        if (!ValidationHelper.isValidPhone(phone)) {
            showError("Format nomor HP tidak valid!");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String hashedPassword = hashPassword(password);

            String sql = "INSERT INTO users (username, password_hash, email, phone, role) VALUES (?, ?, ?, ?, 'visitor')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);
            stmt.setString(4, phone);

            int result = stmt.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Registration successful for user: " + username);
                hideError();
                showAlert(Alert.AlertType.INFORMATION, "✅ Registrasi berhasil! Silakan login dengan akun baru Anda.");

                // Clear form
                clearForm();

                // Go to login page
                handleLogin();
            } else {
                showError("❌ Gagal melakukan registrasi!");
            }

            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Database error: " + e.getMessage());

            if (e.getMessage().contains("Duplicate entry")) {
                if (e.getMessage().contains("username")) {
                    showError("❌ Username sudah digunakan!");
                } else if (e.getMessage().contains("email")) {
                    showError("❌ Email sudah terdaftar!");
                } else {
                    showError("❌ Data sudah ada dalam sistem!");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "❌ Database error: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Registration error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "❌ Error saat registrasi: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("SeniMatic - Login");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Error loading login page: " + e.getMessage());
        }
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        emailField.clear();
        phoneField.clear();
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
