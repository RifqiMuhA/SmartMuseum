package org.example.smartmuseum.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField setPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField noHPField;

    @FXML
    private Button registerButton;

    @FXML
    private Button closeButton;

    @FXML
    private void initialize() {
        registerButton.setOnAction(this::handleRegister);
        closeButton.setOnAction(this::handleClose);
    }

    private void handleRegister(ActionEvent event) {
        String username = usernameTextField.getText();
        String password = setPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText();
        String noHP = noHPField.getText();

        // Validasi sederhana
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || noHP.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Semua field harus diisi!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password dan konfirmasi tidak cocok!");
            return;
        }

        if (!email.contains("@")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Format email tidak valid!");
            return;
        }

        // Simulasi registrasi berhasil
        showAlert(Alert.AlertType.INFORMATION, "Registrasi Berhasil", "Selamat, akun berhasil dibuat!");

        // Bersihkan field
        usernameTextField.clear();
        setPasswordField.clear();
        confirmPasswordField.clear();
        emailField.clear();
        noHPField.clear();
    }

    private void handleClose(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/view/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = new Stage();
            stage.setTitle("SmartMuseum - Login");
            stage.setScene(new Scene(loginRoot));
            stage.show();

            // Tutup jendela register
            Stage registerStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            registerStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Gagal membuka halaman login.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
