package org.example.smartmuseum.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.smartmuseum.model.entity.Artwork;
import org.example.smartmuseum.model.service.ArtworkService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ArtworkListController implements Initializable {

    @FXML private FlowPane artworkFlowPane;
    @FXML private Label clockLabel;
    @FXML private TextField searchField;
    @FXML private Button backButton;
    @FXML private ComboBox<String> typeFilterComboBox;
    @FXML private Button resetFilterButton;
    @FXML private Label countLabel;

    private final ArtworkService artworkService = new ArtworkService();
    private List<Artwork> allArtworks;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            allArtworks = artworkService.getAllArtworks();
            setupTypeFilter();
            showArtworks(allArtworks);
            startClock();
            updateCountLabel(allArtworks.size());

            // Listener untuk search field
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });

            // Listener untuk type filter
            typeFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
        } catch (Exception e) {
            System.err.println("Error initializing ArtworkListController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTypeFilter() {
        try {
            // Get unique artwork types
            List<String> types = allArtworks.stream()
                    .map(Artwork::getArtworkType)
                    .filter(type -> type != null && !type.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // Add "Semua Jenis" as first option
            types.add(0, "Semua Jenis");

            typeFilterComboBox.setItems(FXCollections.observableArrayList(types));
            typeFilterComboBox.setValue("Semua Jenis");
        } catch (Exception e) {
            System.err.println("Error setting up type filter: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        try {
            String searchKeyword = searchField.getText();
            String selectedType = typeFilterComboBox.getValue();

            List<Artwork> filtered = allArtworks.stream()
                    .filter(artwork -> {
                        // Filter by search keyword
                        boolean matchesSearch = searchKeyword == null || searchKeyword.isEmpty() ||
                                artwork.getTitle().toLowerCase().contains(searchKeyword.toLowerCase()) ||
                                (artwork.getArtistName() != null && artwork.getArtistName().toLowerCase().contains(searchKeyword.toLowerCase()));

                        // Filter by type
                        boolean matchesType = selectedType == null || selectedType.isEmpty() ||
                                selectedType.equals("Semua Jenis") ||
                                selectedType.equals(artwork.getArtworkType());

                        return matchesSearch && matchesType;
                    })
                    .collect(Collectors.toList());

            showArtworks(filtered);
            updateCountLabel(filtered.size());
        } catch (Exception e) {
            System.err.println("Error applying filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showArtworks(List<Artwork> artworks) {
        try {
            artworkFlowPane.getChildren().clear();

            for (Artwork artwork : artworks) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/view/artwork-card.fxml"));
                    Parent card = loader.load();

                    ArtworkCardController controller = loader.getController();
                    controller.setData(artwork);

                    artworkFlowPane.getChildren().add(card);
                } catch (IOException e) {
                    System.err.println("Error loading artwork card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Error showing artworks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCountLabel(int count) {
        countLabel.setText("Menampilkan: " + count + " karya");
    }

    @FXML
    private void handleResetFilter() {
        try {
            searchField.clear();
            typeFilterComboBox.setValue("Semua Jenis");
            showArtworks(allArtworks);
            updateCountLabel(allArtworks.size());
        } catch (Exception e) {
            System.err.println("Error resetting filter: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startClock() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(0), event -> {
                        String time = LocalTime.now().format(formatter);
                        if (clockLabel != null) {
                            clockLabel.setText("🕐 " + time);
                        }
                    }),
                    new KeyFrame(Duration.seconds(1))
            );
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        } catch (Exception e) {
            System.err.println("Error starting clock: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/welcome.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Smart Museum - Welcome");
            stage.setMaximized(false);
            stage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Error loading welcome page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
