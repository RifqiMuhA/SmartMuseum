package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.smartmuseum.model.entity.Artwork;
import org.example.smartmuseum.service.TextToSpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class ArtworkCardController {
    private static final Logger logger = LoggerFactory.getLogger(ArtworkCardController.class);

    @FXML private Label titleLabel;
    @FXML private Label artistLabel;
    @FXML private Label techniqueLabel;
    @FXML private TextArea descriptionText;
    @FXML private ImageView artworkImage;
    @FXML private Label typeBadge;
    @FXML private Label yearBadge;
    @FXML private Label titleOverlay;
    @FXML private Button soundButton;
    @FXML private Label statusLabel;

    private Artwork currentArtwork;
    private TextToSpeechService ttsService;

    @FXML
    private void initialize() {
        ttsService = TextToSpeechService.getInstance();
        updateSoundButtonState();
    }

    public void setData(Artwork artwork) {
        this.currentArtwork = artwork;

        // Set informasi teks
        titleLabel.setText(artwork.getTitle());
        artistLabel.setText(artwork.getArtistName() != null ? artwork.getArtistName() : "Unknown Artist");
        techniqueLabel.setText(artwork.getTechnique());
        descriptionText.setText(artwork.getDescription());

        // Set title overlay
        titleOverlay.setText(artwork.getTitle());

        // Set type badge
        if (artwork.getArtworkType() != null) {
            typeBadge.setText(artwork.getArtworkType().toUpperCase());
            typeBadge.setStyle("-fx-background-color: " + artwork.getBadgeColor() + ";");
        } else {
            typeBadge.setText("UNKNOWN");
            typeBadge.setStyle("-fx-background-color: #6c757d;");
        }

        // Set year badge
        yearBadge.setText(String.valueOf(artwork.getYear()));

        // Load image using the provided method
        loadArtworkImage(artwork);

        // Update sound button state
        updateSoundButtonState();
    }

    @FXML
    private void handleSoundButton() {
        if (currentArtwork == null) {
            logger.warn("No artwork data available for TTS");
            return;
        }

        if (!ttsService.isInitialized()) {
            updateStatusLabel("❌ TTS tidak tersedia", "error-status");
            logger.warn("TTS service not initialized");
            return;
        }

        if (ttsService.isSpeaking()) {
            // Stop current speech
            ttsService.stopSpeaking();
            updateStatusLabel("⏹️ Dihentikan", "stopped-status");
            soundButton.setText("🔊");

            // Reset status after 2 seconds
            Platform.runLater(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(() -> updateStatusLabel("🔇 Siap", "ready-status"));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });
        } else {
            // Start speech
            updateStatusLabel("🎵 Memutar...", "playing-status");
            soundButton.setText("⏸️");

            ttsService.speakArtwork(currentArtwork).thenRun(() -> {
                Platform.runLater(() -> {
                    updateStatusLabel("✅ Selesai", "finished-status");
                    soundButton.setText("🔊");

                    // Reset status after 2 seconds
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            Platform.runLater(() -> updateStatusLabel("🔇 Siap", "ready-status"));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    updateStatusLabel("❌ Error", "error-status");
                    soundButton.setText("🔊");
                    logger.error("TTS error: {}", throwable.getMessage(), throwable);
                });
                return null;
            });
        }
    }

    private void updateSoundButtonState() {
        if (ttsService.isInitialized()) {
            soundButton.setDisable(false);
            soundButton.setStyle("-fx-opacity: 1.0;");
        } else {
            soundButton.setDisable(true);
            soundButton.setStyle("-fx-opacity: 0.5;");
            updateStatusLabel("❌ TTS tidak tersedia", "error-status");
        }
    }

    private void updateStatusLabel(String text, String styleClass) {
        statusLabel.setText(text);
        statusLabel.getStyleClass().removeAll("ready-status", "playing-status", "stopped-status", "finished-status", "error-status");
        statusLabel.getStyleClass().add(styleClass);
    }

    private void loadArtworkImage(Artwork artwork) {
        try {
            String imagePath = artwork.getImagePath();

            // Method 1: Menggunakan URL
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                artworkImage.setImage(image);
                System.out.println("Image loaded successfully from: " + imageUrl);
            } else {
                System.err.println("Image resource not found: " + imagePath);

                // Method 2: Coba path alternatif
                String altPath = "images/affandi_1.jpg"; // Without leading slash
                imageUrl = getClass().getResource(altPath);
                if (imageUrl != null) {
                    Image image = new Image(imageUrl.toExternalForm());
                    artworkImage.setImage(image);
                    System.out.println("Image loaded from alternative path: " + imageUrl);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
