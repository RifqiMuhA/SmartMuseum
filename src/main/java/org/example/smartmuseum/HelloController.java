package org.example.smartmuseum;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.smartmuseum.model.concrete.Boss;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    Boss boss;

    HelloController(){

    }
}