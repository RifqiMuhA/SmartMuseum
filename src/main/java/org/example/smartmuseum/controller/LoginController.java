package org.example.smartmuseum.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.smartmuseum.database.DatabaseConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML private TextField usernameTextField;
    @FXML private PasswordField enterPasswordField;
    @FXML private Label invalidLoginLabel;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        loginButton.setOnAction(e -> loginUser());
    }

    private void loginUser() {
        String username = usernameTextField.getText();
        String password = enterPasswordField.getText();

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String hashedPassword = hashPassword(password);

            String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                invalidLoginLabel.setVisible(false);
                showAlert(Alert.AlertType.INFORMATION, "Login berhasil sebagai " + rs.getString("role"));
                // TODO: navigasi ke halaman sesuai role
            } else {
                invalidLoginLabel.setText("Invalid login! Please try again!");
                invalidLoginLabel.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Terjadi kesalahan saat login: " + e.getMessage());
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
