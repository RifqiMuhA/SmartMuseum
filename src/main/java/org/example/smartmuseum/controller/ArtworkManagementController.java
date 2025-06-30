package org.example.smartmuseum.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.smartmuseum.model.entity.Artist;
import org.example.smartmuseum.model.entity.Artwork;
import org.example.smartmuseum.model.service.ArtworkService;
import org.example.smartmuseum.util.QRCodeGenerator;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class ArtworkManagementController implements Initializable {

    @FXML private Label lblTotalArtworks;
    @FXML private TextField txtTitle;
    @FXML private ComboBox<ArtistItem> cmbArtist;
    @FXML private TextField txtYear;
    @FXML private TextField txtTechnique;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtImagePath;
    @FXML private ComboBox<String> cmbArtworkType; // New field for artwork type
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private ImageView imgQRCode;
    @FXML private Label lblQRCodeText;
    @FXML private Button btnGenerateQR;
    @FXML private Label lblStatus;
    @FXML private TableView<ArtworkRecord> tableArtworks;
    @FXML private TableColumn<ArtworkRecord, Integer> colArtworkId;
    @FXML private TableColumn<ArtworkRecord, String> colTitle;
    @FXML private TableColumn<ArtworkRecord, String> colArtist;
    @FXML private TableColumn<ArtworkRecord, Integer> colYear;
    @FXML private TableColumn<ArtworkRecord, String> colTechnique;
    @FXML private TableColumn<ArtworkRecord, String> colArtworkType; // New column
    @FXML private TableColumn<ArtworkRecord, String> colQRCode;

    // Artist management fields
    @FXML private TextField txtArtistName;
    @FXML private TextField txtNationality;
    @FXML private TextField txtBirthYear;
    @FXML private TextArea txtBiography;

    private ArtworkService artworkService;
    private ObservableList<ArtworkRecord> artworkData;
    private ObservableList<ArtistItem> artistData;
    private ArtworkRecord selectedArtwork;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        artworkService = new ArtworkService();
        artworkData = FXCollections.observableArrayList();
        artistData = FXCollections.observableArrayList();

        setupArtworkTypeComboBox();
        setupTableColumns();
        setupTableSelection();
        setupArtistComboBox();
        loadArtists();
        loadArtworks();
        updateStatistics();

        // Initially disable update and delete buttons
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnGenerateQR.setDisable(true);
    }

    private void setupArtworkTypeComboBox() {
        ObservableList<String> artworkTypes = FXCollections.observableArrayList(
                "Lukisan", "Patung", "Keramik", "Tekstil", "Kaligrafi", "Fotografi", "Instalasi"
        );
        cmbArtworkType.setItems(artworkTypes);
        cmbArtworkType.setValue("Lukisan"); // Default value
    }

    private void setupTableColumns() {
        colArtworkId.setCellValueFactory(new PropertyValueFactory<>("artworkId"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artistName"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colTechnique.setCellValueFactory(new PropertyValueFactory<>("technique"));
        colArtworkType.setCellValueFactory(new PropertyValueFactory<>("artworkType"));
        colQRCode.setCellValueFactory(new PropertyValueFactory<>("qrCode"));

        // Add custom cell factory for artwork type with colored badges
        colArtworkType.setCellFactory(column -> new TableCell<ArtworkRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = getArtworkTypeColor(item);
                    setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5px; -fx-background-radius: 10px;");
                }
            }
        });

        tableArtworks.setItems(artworkData);
    }

    private String getArtworkTypeColor(String artworkType) {
        if (artworkType == null) return "#6c757d";

        switch (artworkType.toLowerCase()) {
            case "lukisan": return "#e74c3c"; // Red
            case "patung": return "#3498db"; // Blue
            case "keramik": return "#f39c12"; // Orange
            case "tekstil": return "#9b59b6"; // Purple
            case "kaligrafi": return "#27ae60"; // Green
            case "fotografi": return "#34495e"; // Dark gray
            case "instalasi": return "#e67e22"; // Dark orange
            default: return "#6c757d"; // Default gray
        }
    }

    private void setupTableSelection() {
        tableArtworks.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedArtwork = newSelection;
            if (newSelection != null) {
                populateForm(newSelection);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
                btnGenerateQR.setDisable(false);

                // Display existing QR code if available
                if (newSelection.getQrCode() != null && !newSelection.getQrCode().isEmpty()) {
                    displayQRCode(newSelection.getQrCode());
                }
            } else {
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);
                btnGenerateQR.setDisable(true);
                clearQRCode();
            }
        });
    }

    private void setupArtistComboBox() {
        cmbArtist.setItems(artistData);
    }

    private void loadArtworks() {
        artworkData.clear();

        try {
            // Load from database using service
            List<Artwork> artworks = artworkService.getAllArtworks();

            for (Artwork artwork : artworks) {
                ArtworkRecord record = new ArtworkRecord();
                record.setArtworkId(artwork.getArtworkId());
                record.setTitle(artwork.getTitle());
                record.setArtistId(artwork.getArtistId());
                record.setArtistName(artwork.getArtistName());
                record.setYear(artwork.getYear());
                record.setTechnique(artwork.getTechnique());
                record.setDescription(artwork.getDescription());
                record.setImagePath(artwork.getImagePath());
                record.setQrCode(artwork.getQrCode());
                record.setArtworkType(artwork.getArtworkType()); // New field

                artworkData.add(record);
            }

        } catch (Exception e) {
            System.err.println("Error loading artworks: " + e.getMessage());
            e.printStackTrace();
            // Fallback to sample data if database fails
            createSampleArtworks();
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            return;
        }

        try {
            ArtworkRecord newArtwork = createArtworkFromForm();
            newArtwork.setArtworkId(artworkData.size() + 1);

            // Create Artwork entity
            Artwork artwork = new Artwork();
            artwork.setArtworkId(newArtwork.getArtworkId());
            artwork.setTitle(newArtwork.getTitle());
            artwork.setArtistId(newArtwork.getArtistId());
            artwork.setYear(newArtwork.getYear());
            artwork.setTechnique(newArtwork.getTechnique());
            artwork.setDescription(newArtwork.getDescription());
            artwork.setImagePath(newArtwork.getImagePath());
            artwork.setArtworkType(newArtwork.getArtworkType()); // New field
            artwork.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            artworkData.add(newArtwork);

            showSuccess("Artwork added successfully: " + newArtwork.getTitle());
            handleClear();
            updateStatistics();

        } catch (Exception e) {
            showError("Error adding artwork: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedArtwork == null) {
            showError("Please select an artwork to update");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            updateArtworkFromForm(selectedArtwork);
            tableArtworks.refresh();

            showSuccess("Artwork updated successfully: " + selectedArtwork.getTitle());
            handleClear();

        } catch (Exception e) {
            showError("Error updating artwork: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedArtwork == null) {
            showError("Please select an artwork to delete");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Artwork");
        confirmAlert.setContentText("Are you sure you want to delete artwork: " + selectedArtwork.getTitle() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                artworkData.remove(selectedArtwork);
                showSuccess("Artwork deleted successfully: " + selectedArtwork.getTitle());
                handleClear();
                updateStatistics();

            } catch (Exception e) {
                showError("Error deleting artwork: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        txtTitle.clear();
        cmbArtist.setValue(null);
        txtYear.clear();
        txtTechnique.clear();
        txtDescription.clear();
        txtImagePath.clear();
        cmbArtworkType.setValue("Lukisan"); // Reset to default

        tableArtworks.getSelectionModel().clearSelection();
        selectedArtwork = null;
        clearQRCode();

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnGenerateQR.setDisable(true);

        lblStatus.setText("Form cleared. Ready to add new artwork.");
        lblStatus.setStyle("-fx-text-fill: #666666;");
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Artwork Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) txtImagePath.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            txtImagePath.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleGenerateQR() {
        if (selectedArtwork == null) {
            showError("Please select an artwork to generate QR code");
            return;
        }

        try {
            String qrData = "ART" + selectedArtwork.getArtworkId() + "_" +
                    selectedArtwork.getTitle().replaceAll("\\s+", "_");
            String qrCode = QRCodeGenerator.generateCustomQRData(qrData);

            selectedArtwork.setQrCode(qrCode);
            tableArtworks.refresh();

            displayQRCode(qrCode);

            showSuccess("QR code generated for: " + selectedArtwork.getTitle());

        } catch (Exception e) {
            showError("Error generating QR code: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddArtist() {
        String name = txtArtistName.getText().trim();
        String nationality = txtNationality.getText().trim();
        String birthYearStr = txtBirthYear.getText().trim();
        String biography = txtBiography.getText().trim();

        if (name.isEmpty()) {
            showError("Artist name is required");
            return;
        }

        try {
            int birthYear = birthYearStr.isEmpty() ? 0 : Integer.parseInt(birthYearStr);

            ArtistItem newArtist = new ArtistItem();
            newArtist.setArtistId(artistData.size() + 1);
            newArtist.setName(name);
            newArtist.setNationality(nationality);
            newArtist.setBirthYear(birthYear);
            newArtist.setBiography(biography);

            artistData.add(newArtist);

            // Clear artist form
            txtArtistName.clear();
            txtNationality.clear();
            txtBirthYear.clear();
            txtBiography.clear();

            showSuccess("Artist added successfully: " + name);

        } catch (NumberFormatException e) {
            showError("Invalid birth year format");
        } catch (Exception e) {
            showError("Error adding artist: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadArtworks();
        loadArtists();
        updateStatistics();
        showSuccess("Artwork data refreshed");
    }

    private void loadArtists() {
        artistData.clear();
        createSampleArtists();
    }

    private void createSampleArtists() {
        ArtistItem artist1 = new ArtistItem();
        artist1.setArtistId(1);
        artist1.setName("Leonardo da Vinci");
        artist1.setNationality("Italian");
        artist1.setBirthYear(1452);
        artist1.setBiography("Renaissance polymath");
        artistData.add(artist1);

        ArtistItem artist2 = new ArtistItem();
        artist2.setArtistId(2);
        artist2.setName("Vincent van Gogh");
        artist2.setNationality("Dutch");
        artist2.setBirthYear(1853);
        artist2.setBiography("Post-impressionist painter");
        artistData.add(artist2);

        ArtistItem artist3 = new ArtistItem();
        artist3.setArtistId(3);
        artist3.setName("Pablo Picasso");
        artist3.setNationality("Spanish");
        artist3.setBirthYear(1881);
        artist3.setBiography("Cubist painter and sculptor");
        artistData.add(artist3);
    }

    private void createSampleArtworks() {
        ArtworkRecord artwork1 = new ArtworkRecord();
        artwork1.setArtworkId(1);
        artwork1.setTitle("Mona Lisa");
        artwork1.setArtistId(1);
        artwork1.setArtistName("Leonardo da Vinci");
        artwork1.setYear(1503);
        artwork1.setTechnique("Oil on poplar");
        artwork1.setDescription("Famous portrait painting");
        artwork1.setQrCode("QR_ART1_Mona_Lisa_12345678");
        artwork1.setArtworkType("Lukisan");
        artworkData.add(artwork1);

        ArtworkRecord artwork2 = new ArtworkRecord();
        artwork2.setArtworkId(2);
        artwork2.setTitle("Starry Night");
        artwork2.setArtistId(2);
        artwork2.setArtistName("Vincent van Gogh");
        artwork2.setYear(1889);
        artwork2.setTechnique("Oil on canvas");
        artwork2.setDescription("Post-impressionist masterpiece");
        artwork2.setQrCode("QR_ART2_Starry_Night_87654321");
        artwork2.setArtworkType("Lukisan");
        artworkData.add(artwork2);

        ArtworkRecord artwork3 = new ArtworkRecord();
        artwork3.setArtworkId(3);
        artwork3.setTitle("David");
        artwork3.setArtistId(3);
        artwork3.setArtistName("Michelangelo");
        artwork3.setYear(1504);
        artwork3.setTechnique("Marble sculpture");
        artwork3.setDescription("Renaissance sculpture masterpiece");
        artwork3.setQrCode("");
        artwork3.setArtworkType("Patung");
        artworkData.add(artwork3);
    }

    private boolean validateForm() {
        String title = txtTitle.getText().trim();
        ArtistItem artist = cmbArtist.getValue();
        String yearStr = txtYear.getText().trim();
        String artworkType = cmbArtworkType.getValue();

        if (title.isEmpty()) {
            showError("Artwork title is required");
            return false;
        }

        if (artist == null) {
            showError("Please select an artist");
            return false;
        }

        if (artworkType == null || artworkType.isEmpty()) {
            showError("Please select artwork type");
            return false;
        }

        if (!yearStr.isEmpty()) {
            try {
                Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                showError("Invalid year format");
                return false;
            }
        }

        return true;
    }

    private ArtworkRecord createArtworkFromForm() {
        ArtworkRecord artwork = new ArtworkRecord();
        artwork.setTitle(txtTitle.getText().trim());
        artwork.setArtistId(cmbArtist.getValue().getArtistId());
        artwork.setArtistName(cmbArtist.getValue().getName());
        artwork.setYear(txtYear.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtYear.getText().trim()));
        artwork.setTechnique(txtTechnique.getText().trim());
        artwork.setDescription(txtDescription.getText().trim());
        artwork.setImagePath(txtImagePath.getText().trim());
        artwork.setArtworkType(cmbArtworkType.getValue()); // New field
        artwork.setQrCode("");
        return artwork;
    }

    private void updateArtworkFromForm(ArtworkRecord artwork) {
        artwork.setTitle(txtTitle.getText().trim());
        artwork.setArtistId(cmbArtist.getValue().getArtistId());
        artwork.setArtistName(cmbArtist.getValue().getName());
        artwork.setYear(txtYear.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtYear.getText().trim()));
        artwork.setTechnique(txtTechnique.getText().trim());
        artwork.setDescription(txtDescription.getText().trim());
        artwork.setImagePath(txtImagePath.getText().trim());
        artwork.setArtworkType(cmbArtworkType.getValue()); // New field
    }

    private void populateForm(ArtworkRecord artwork) {
        txtTitle.setText(artwork.getTitle());
        txtYear.setText(artwork.getYear() > 0 ? String.valueOf(artwork.getYear()) : "");
        txtTechnique.setText(artwork.getTechnique());
        txtDescription.setText(artwork.getDescription());
        txtImagePath.setText(artwork.getImagePath());
        cmbArtworkType.setValue(artwork.getArtworkType()); // New field

        // Set artist
        for (ArtistItem artist : artistData) {
            if (artist.getArtistId() == artwork.getArtistId()) {
                cmbArtist.setValue(artist);
                break;
            }
        }
    }

    private void displayQRCode(String qrCode) {
        lblQRCodeText.setText(qrCode);
        lblQRCodeText.setVisible(true);
    }

    private void clearQRCode() {
        imgQRCode.setImage(null);
        lblQRCodeText.setText("");
        lblQRCodeText.setVisible(false);
    }

    private void updateStatistics() {
        lblTotalArtworks.setText("Total Artworks: " + artworkData.size());
    }

    private void showSuccess(String message) {
        lblStatus.setText("✓ " + message);
        lblStatus.setStyle("-fx-text-fill: #4CAF50;");
    }

    private void showError(String message) {
        lblStatus.setText("✗ " + message);
        lblStatus.setStyle("-fx-text-fill: #F44336;");
    }

    // Inner classes for table data
    public static class ArtworkRecord {
        private int artworkId;
        private String title;
        private int artistId;
        private String artistName;
        private int year;
        private String technique;
        private String description;
        private String imagePath;
        private String qrCode;
        private String artworkType; // New field

        // Getters and setters
        public int getArtworkId() { return artworkId; }
        public void setArtworkId(int artworkId) { this.artworkId = artworkId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public int getArtistId() { return artistId; }
        public void setArtistId(int artistId) { this.artistId = artistId; }

        public String getArtistName() { return artistName; }
        public void setArtistName(String artistName) { this.artistName = artistName; }

        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public String getTechnique() { return technique; }
        public void setTechnique(String technique) { this.technique = technique; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }

        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }

        public String getArtworkType() { return artworkType; }
        public void setArtworkType(String artworkType) { this.artworkType = artworkType; }
    }

    public static class ArtistItem {
        private int artistId;
        private String name;
        private String nationality;
        private int birthYear;
        private String biography;

        // Getters and setters
        public int getArtistId() { return artistId; }
        public void setArtistId(int artistId) { this.artistId = artistId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getNationality() { return nationality; }
        public void setNationality(String nationality) { this.nationality = nationality; }

        public int getBirthYear() { return birthYear; }
        public void setBirthYear(int birthYear) { this.birthYear = birthYear; }

        public String getBiography() { return biography; }
        public void setBiography(String biography) { this.biography = biography; }

        @Override
        public String toString() {
            return name + " (" + nationality + ")";
        }
    }
}
