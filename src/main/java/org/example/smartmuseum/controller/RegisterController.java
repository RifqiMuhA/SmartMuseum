package org.example.smartmuseum.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {
    @FXML private TextField usernameTextField;
    @FXML private PasswordField setPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField noHPField;
    @FXML private Button registerButton;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {

    }

    // This method is called by the FXML onAction="#registerUser"
    @FXML
    private void registerUser() {
        String username = usernameTextField.getText();
        String password = setPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText();
        String phone = noHPField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Semua field harus diisi.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Password dan konfirmasi tidak cocok.");
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

            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Registrasi berhasil! Silakan login.");

            // Clear input fields
            usernameTextField.clear();
            setPasswordField.clear();
            confirmPasswordField.clear();
            emailField.clear();
            noHPField.clear();

            // Otomatis pindah ke halaman login
            handleLogin();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Terjadi kesalahan saat registrasi: " + ex.getMessage());
        }
    }

    // This method is called by the FXML onAction="#handleLogin"
    @FXML
    private void handleLogin() {
        try {
            // Load login.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/view/login.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Set new scene
            stage.setScene(new Scene(root));
            stage.setTitle("SmartMuseum - Login");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading login page: " + e.getMessage());
        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}