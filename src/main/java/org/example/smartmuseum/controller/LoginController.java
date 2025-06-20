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
import java.sql.ResultSet;

public class LoginController {
    @FXML private TextField usernameTextField;
    @FXML private PasswordField enterPasswordField;
    @FXML private Label invalidLoginLabel;
    @FXML private Button loginButton;
    @FXML private Button registerPageButton;

    @FXML
    private void initialize() {
        // Remove the manual event handler since we're using onAction in FXML
    }

    // This method is called by the FXML onAction="#loginButton"
    @FXML
    private void loginButton() {
        loginUser();
    }

    // This method is called by the FXML onAction="#handleRegister"
    @FXML
    private void handleRegister() {
        try {
            // Load register.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/view/register.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) registerPageButton.getScene().getWindow();

            // Set new scene
            stage.setScene(new Scene(root));
            stage.setTitle("SmartMuseum - Register");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading register page: " + e.getMessage());
        }
    }

    private void handleWelcome(){
        try {
            // Load login.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/welcome.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Set new scene
            stage.setScene(new Scene(root));
            stage.setTitle("SeniMatic - Welcome");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading welcome page: " + e.getMessage());
        }
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
                if (invalidLoginLabel != null) {
                    invalidLoginLabel.setVisible(false);
                }
                showAlert(Alert.AlertType.INFORMATION, "Login berhasil sebagai " + rs.getString("role"));
                handleWelcome();
            } else {
                if (invalidLoginLabel != null) {
                    invalidLoginLabel.setText("Invalid login! Please try again!");
                    invalidLoginLabel.setVisible(true);
                }
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