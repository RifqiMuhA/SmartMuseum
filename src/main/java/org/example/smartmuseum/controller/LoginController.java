package org.example.smartmuseum.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Node;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField enterPasswordField;

    @FXML
    public void logginButton(ActionEvent event) {
        System.out.println("Login attempted by: " + usernameTextField.getText());
    }

    @FXML
    public void cancelButton(ActionEvent event) {
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            Parent registerRoot = FXMLLoader.load(getClass().getResource("/org/example/smartmuseum/view/register.fxml"));
            Stage registerStage = new Stage();
            registerStage.setTitle("SmartMuseum - Register");
            registerStage.setScene(new Scene(registerRoot));
            registerStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
