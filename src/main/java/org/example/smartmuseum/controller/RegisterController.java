package org.example.smartmuseum.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.smartmuseum.database.DatabaseConnection;

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

    @FXML
    private void initialize() {
        registerButton.setOnAction(e -> registerUser());
    }

    private void registerUser() {
        String username = usernameTextField.getText();
        String password = setPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText();
        String phone = noHPField.getText();

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

            showAlert(Alert.AlertType.INFORMATION, "Registrasi berhasil!");

            // Clear input fields
            usernameTextField.clear();
            setPasswordField.clear();
            confirmPasswordField.clear();
            emailField.clear();
            noHPField.clear();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Terjadi kesalahan saat registrasi: " + ex.getMessage());
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
