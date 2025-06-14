package org.example.smartmuseum.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.smartmuseum.SmartMuseumAttendanceApp;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class WelcomeController implements Initializable {

    @FXML private Button btnGoToDashboard;
    @FXML private Button btnExit;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblWelcomeMessage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateTime();
        setupWelcomeMessage();

        // Update time every second
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> updateTime())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        String timeString = now.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy - HH:mm:ss"));
        lblCurrentTime.setText(timeString);
    }

    private void setupWelcomeMessage() {
        LocalDateTime now = LocalDateTime.now();
        String greeting;

        if (now.getHour() < 12) {
            greeting = "Selamat Pagi";
        } else if (now.getHour() < 17) {
            greeting = "Selamat Siang";
        } else {
            greeting = "Selamat Sore";
        }

        lblWelcomeMessage.setText(greeting + "! Selamat datang di Smart Museum");
    }

    @FXML
    private void handleGoToDashboard() {
        System.out.println("Navigating to Dashboard...");
        SmartMuseumAttendanceApp.showDashboard();
    }

    @FXML
    private void handleExit() {
        System.out.println("Exiting application...");
        System.exit(0);
    }
}