package org.example.smartmuseum.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

public class LelangController {

    @FXML private Label labelLogo;
    @FXML private Button backButton;
    @FXML private ImageView gambarUtamaDinamis;
    @FXML private Label namaBenda;
    @FXML private Label auctionInformation;
    @FXML private Label auctionIdDinamis;
    @FXML private Label dateDinamis;
    @FXML private Label waktuDinamis;
    @FXML private Label namaPemilikDinamis;
    @FXML private Label currentBidStatis;
    @FXML private Label hargaDinamis;
    @FXML private Label jumlahBidDinamis;
    @FXML private Label yourBid;
    @FXML private TextField inputBid;
    @FXML private Button buttonBid;
    @FXML private Label warning;
    @FXML private Label timeLeftStatis;
    @FXML private Label detikDinamis;
    @FXML private Label detikStatis;

    // Auction logic variables
    private double currentBid = 500000;
    private int bidCount = 0;
    private int timeLeft = 30;
    private NumberFormat currencyFormat;
    private boolean auctionEnded = false;
    private String instanceName;
    private Timeline timeline;

    @FXML
    public void initialize() {
        // Generate unique instance name
        instanceName = "User" + (new Random().nextInt(1000) + 1);

        // Setup currency formatter for Rupiah
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        // Set initial values
        namaBenda.setText("Antique Vase");
        auctionIdDinamis.setText("AUC-001");
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        dateDinamis.setText(currentDate.format(dateFormatter));
        updateTimeDisplay();
        namaPemilikDinamis.setText("owner.artwork@gmail.com");
        yourBid.setText("Your Bid (" + instanceName + ")");

        // Set sample image
        try {
            gambarUtamaDinamis.setImage(new Image(getClass().getResourceAsStream("/org/example/smartmuseum/images/vase.jpg")));
        } catch (Exception e) {
            System.out.println("Image not found: " + e.getMessage());
        }

        // Initialize auction data
        updateBidDisplay();
        updateTimeDisplay();

        // Setup button actions
        buttonBid.setOnAction(event -> placeBid());
        backButton.setOnAction(event -> handleBack());

        // Start countdown timer
        startCountdown();

        // Set window title
        Platform.runLater(() -> {
            Stage stage = (Stage) buttonBid.getScene().getWindow();
            if (stage != null) {
                stage.setTitle("SeniMatic Auction - " + instanceName);
            }
        });
    }

    private void handleBack() {
        try {
            cleanup();

            // Get current stage
            Stage currentStage = (Stage) backButton.getScene().getWindow();

            // Load welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/welcome.fxml"));
            Parent root = loader.load();

            Stage welcomeStage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            welcomeStage.setScene(scene);
            welcomeStage.setTitle("Smart Museum - Welcome");
            welcomeStage.setMaximized(true);

            // Close current stage and show welcome
            currentStage.close();
            welcomeStage.show();

            System.out.println("Returned to welcome from auction");
        } catch (Exception e) {
            System.out.println("Error returning to welcome: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startCountdown() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (!auctionEnded) {
                timeLeft--;
                updateTimeDisplay();
                if (timeLeft <= 0) {
                    endAuction();
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void placeBid() {
        if (auctionEnded) {
            warning.setText("Auction has ended!");
            warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
            return;
        }

        try {
            String bidText = inputBid.getText().trim();
            if (bidText.isEmpty()) {
                warning.setText("Please enter a bid amount!");
                warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
                return;
            }

            // Clean input
            bidText = bidText.replaceAll("[Rp\\s,.]", "");
            double newBid = Double.parseDouble(bidText);

            // Validate bid
            if (newBid <= currentBid) {
                warning.setText("Bid must be higher than " + formatCurrency(currentBid) + "!");
                warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
                return;
            }

            double minIncrement = 50000;
            if (newBid < currentBid + minIncrement) {
                warning.setText("Minimum bid increment is " + formatCurrency(minIncrement) + "!");
                warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
                return;
            }

            // Update bid
            currentBid = newBid;
            bidCount++;
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            waktuDinamis.setText(currentTime.format(timeFormatter));
            timeLeft = 30;
            updateBidDisplay();
            updateTimeDisplay();
            inputBid.clear();
            warning.setText("Bid placed successfully by " + instanceName + "!");
            warning.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px;");
            namaPemilikDinamis.setText("Last bid by: " + instanceName);
            startCountdown();

        } catch (NumberFormatException e) {
            warning.setText("Invalid bid format! Use numbers only (e.g., 1000000)");
            warning.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        }
    }

    private void updateBidDisplay() {
        hargaDinamis.setText(formatCurrency(currentBid));
        jumlahBidDinamis.setText("(" + bidCount + " bids)");
    }

    private void updateTimeDisplay() {
        detikDinamis.setText(String.valueOf(timeLeft));
        if (timeLeft <= 5) {
            detikDinamis.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 48px;");
            detikStatis.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 24px;");
        } else if (timeLeft <= 10) {
            detikDinamis.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-font-size: 48px;");
            detikStatis.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-font-size: 24px;");
        } else {
            detikDinamis.setStyle("-fx-text-fill: #3B82F6; -fx-font-weight: bold; -fx-font-size: 48px;");
            detikStatis.setStyle("-fx-text-fill: #3B82F6; -fx-font-size: 24px;");
        }
    }

    private void endAuction() {
        auctionEnded = true;
        if (timeline != null) {
            timeline.stop();
        }
        buttonBid.setDisable(true);
        inputBid.setDisable(true);
        warning.setText("Auction Ended! Winner: " + formatCurrency(currentBid));
        warning.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 14px;");
        if (bidCount > 0) {
            namaPemilikDinamis.setText("Winner: " + namaPemilikDinamis.getText().replace("Last bid by: ", ""));
        } else {
            namaPemilikDinamis.setText("No bids placed");
        }
        detikDinamis.setText("0");
        detikStatis.setText("Ended");
    }

    private String formatCurrency(double amount) {
        return currencyFormat.format(amount).replace("Rp", "Rp ").replace(",00", "");
    }

    public void cleanup() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}
