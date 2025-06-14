package org.example.smartmuseum.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField enterPasswordField;

    @FXML
    public void logginButton(ActionEvent event) {
        // Ini hanya contoh, nanti bisa kamu isi dengan validasi username dan password
        String username = usernameTextField.getText();
        String password = enterPasswordField.getText();

        System.out.println("Login attempted by: " + username + " with password: " + password);

        // Misalnya nanti kamu mau masuk ke halaman dashboard, bisa load fxml lain di sini
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            // Load register.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/view/register.fxml"));
            Parent registerRoot = loader.load();

            // Buka halaman register
            Stage registerStage = new Stage();
            registerStage.setTitle("SmartMuseum - Register");
            registerStage.setScene(new Scene(registerRoot));
            registerStage.show();

            // Tutup halaman login
            Stage loginStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
