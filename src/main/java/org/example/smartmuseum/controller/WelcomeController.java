package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeController implements Initializable {

    @FXML private Label lblWelcomeMessage;
    @FXML private Label lblCurrentTime;
    @FXML private Button btnGoToDashboard;
    @FXML private Button btnExit;

    private Timer clockTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupClock();
        setupWelcomeMessage();
    }

    private void setupClock() {
        clockTimer = new Timer(true);
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    String currentTime = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy - HH:mm:ss"));
                    lblCurrentTime.setText(currentTime);
                });
            }
        }, 0, 1000);
    }

    private void setupWelcomeMessage() {
        int hour = LocalDateTime.now().getHour();
        String greeting;

        if (hour < 12) {
            greeting = "Selamat Pagi!";
        } else if (hour < 17) {
            greeting = "Selamat Siang!";
        } else {
            greeting = "Selamat Malam!";
        }

        lblWelcomeMessage.setText(greeting + " Selamat datang di Smart Museum System.");
    }

    @FXML
    private void handleGoToDashboard() {
        try {
            // Clean up timer
            if (clockTimer != null) {
                clockTimer.cancel();
            }

            // Load dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnGoToDashboard.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Smart Museum - Dashboard");
            stage.setMaximized(true);

        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        if (clockTimer != null) {
            clockTimer.cancel();
        }
        Platform.exit();
    }

    @FXML
    private void handleLelangTerkini() {
        try {
            // Clean up timer
            if (clockTimer != null) {
                clockTimer.cancel();
            }

            // Load lelang.fxml directly
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/view/lelang.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnGoToDashboard.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("SeniMatic - Auction");
            stage.setMaximized(true);

        } catch (IOException e) {
            System.err.println("Error loading lelang: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleListArtwork() {
        try {
            // Clean up timer
            if (clockTimer != null) {
                clockTimer.cancel();
            }

            // Load artwork list directly
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/view/artwork-list.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnGoToDashboard.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/artwork-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Smart Museum - List Artwork");
            stage.setMaximized(true);

        } catch (IOException e) {
            System.err.println("Error loading artwork list: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChatbot() {
        try {
            // Clean up timer
            if (clockTimer != null) {
                clockTimer.cancel();
            }

            // Close current welcome window
            Stage currentStage = (Stage) btnGoToDashboard.getScene().getWindow();

            // Load chatbot window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/chatbot.fxml"));
            Parent chatbotWindow = loader.load();

            Stage chatbotStage = new Stage();
            chatbotStage.setTitle("Smart Museum - Chatbot Assistant");
            Scene scene = new Scene(chatbotWindow, 800, 600);
            chatbotStage.setScene(scene);

            // Close welcome and show chatbot
            currentStage.close();
            chatbotStage.show();

        } catch (IOException e) {
            System.err.println("Error loading chatbot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
