package org.example.smartmuseum.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.smartmuseum.model.entity.Artist;
import org.example.smartmuseum.model.entity.Artwork;
import org.example.smartmuseum.model.service.ArtworkService;
import org.example.smartmuseum.model.service.ArtistService;

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
    @FXML private ComboBox<String> cmbArtworkType;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private ImageView imgPreview;
    @FXML private Label lblImageStatus;
    @FXML private Button btnLoadImage;
    @FXML private Label lblStatus;
    @FXML private TableView<ArtworkRecord> tableArtworks;
    @FXML private TableColumn<ArtworkRecord, Integer> colArtworkId;
    @FXML private TableColumn<ArtworkRecord, String> colTitle;
    @FXML private TableColumn<ArtworkRecord, String> colArtist;
    @FXML private TableColumn<ArtworkRecord, Integer> colYear;
    @FXML private TableColumn<ArtworkRecord, String> colTechnique;
    @FXML private TableColumn<ArtworkRecord, String> colArtworkType;
    @FXML private TableColumn<ArtworkRecord, String> colImagePath;

    // Artist management fields
    @FXML private TextField txtArtistName;
    @FXML private TextField txtNationality;
    @FXML private TextField txtBirthYear;
    @FXML private TextArea txtBiography;

    private ArtworkService artworkService;
    private ArtistService artistService;
    private ObservableList<ArtworkRecord> artworkData;
    private ObservableList<ArtistItem> artistData;
    private ArtworkRecord selectedArtwork;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        artworkService = new ArtworkService();
        artistService = new ArtistService();
        artworkData = FXCollections.observableArrayList();
        artistData = FXCollections.observableArrayList();

        setupArtworkTypeComboBox();
        setupTableColumns();
        setupTableSelection();
        setupArtistComboBox();
        loadArtistsFromDatabase();
        loadArtworks();
        updateStatistics();

        // Initially disable update and delete buttons
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnLoadImage.setDisable(true);
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
        colImagePath.setCellValueFactory(new PropertyValueFactory<>("imagePath"));

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
                btnLoadImage.setDisable(false);

                // Display existing image if available
                if (newSelection.getImagePath() != null && !newSelection.getImagePath().isEmpty()) {
                    loadImagePreview(newSelection.getImagePath());
                }
            } else {
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);
                btnLoadImage.setDisable(true);
                clearImagePreview();
            }
        });
    }

    private void setupArtistComboBox() {
        cmbArtist.setItems(artistData);
    }

    private void loadArtistsFromDatabase() {
        artistData.clear();

        try {
            // Load artists from database using service
            List<Artist> artists = artistService.getAllArtists();

            for (Artist artist : artists) {
                ArtistItem artistItem = new ArtistItem();
                artistItem.setArtistId(artist.getArtistId());
                artistItem.setName(artist.getName());
                artistItem.setNationality(artist.getNationality());
                artistItem.setBirthYear(artist.getBirthYear());
                artistItem.setBiography(artist.getBiography());

                artistData.add(artistItem);
            }

        } catch (Exception e) {
            System.err.println("Error loading artists from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadArtworks() {
        artworkData.clear();

        try {
            // Load from database using service (already includes JOIN for artist name)
            List<Artwork> artworks = artworkService.getAllArtworks();

            for (Artwork artwork : artworks) {
                ArtworkRecord record = new ArtworkRecord();
                record.setArtworkId(artwork.getArtworkId());
                record.setTitle(artwork.getTitle());
                record.setArtistId(artwork.getArtistId());
                record.setArtistName(artwork.getArtistName()); // Already joined in service
                record.setYear(artwork.getYear());
                record.setTechnique(artwork.getTechnique());
                record.setDescription(artwork.getDescription());
                record.setImagePath(artwork.getImagePath());
                record.setArtworkType(artwork.getArtworkType());

                artworkData.add(record);
            }

        } catch (Exception e) {
            System.err.println("Error loading artworks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            return;
        }

        try {
            ArtworkRecord newArtwork = createArtworkFromForm();

            // Create Artwork entity for database
            Artwork artwork = new Artwork();
            artwork.setTitle(newArtwork.getTitle());
            artwork.setArtistId(newArtwork.getArtistId()); // Save foreign key
            artwork.setYear(newArtwork.getYear());
            artwork.setTechnique(newArtwork.getTechnique());
            artwork.setDescription(newArtwork.getDescription());
            artwork.setImagePath(newArtwork.getImagePath());
            artwork.setArtworkType(newArtwork.getArtworkType());
            artwork.setQrCode(""); // Empty QR code
            artwork.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            // Save to database using service
            boolean success = artworkService.addArtwork(artwork);

            if (success) {
                showSuccess("Artwork added successfully: " + newArtwork.getTitle());
                handleClear();
                loadArtworks(); // Reload to get updated data with IDs
                updateStatistics();
            } else {
                showError("Failed to add artwork to database");
            }

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

            // Update in database
            Artwork artwork = new Artwork();
            artwork.setArtworkId(selectedArtwork.getArtworkId());
            artwork.setTitle(selectedArtwork.getTitle());
            artwork.setArtistId(selectedArtwork.getArtistId()); // Save foreign key
            artwork.setYear(selectedArtwork.getYear());
            artwork.setTechnique(selectedArtwork.getTechnique());
            artwork.setDescription(selectedArtwork.getDescription());
            artwork.setImagePath(selectedArtwork.getImagePath());
            artwork.setArtworkType(selectedArtwork.getArtworkType());
            artwork.setQrCode(""); // Empty QR code

            boolean success = artworkService.updateArtwork(artwork);

            if (success) {
                tableArtworks.refresh();
                showSuccess("Artwork updated successfully: " + selectedArtwork.getTitle());
                handleClear();
            } else {
                showError("Failed to update artwork in database");
            }

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
                // Delete from database
                boolean success = artworkService.deleteArtwork(selectedArtwork.getArtworkId());

                if (success) {
                    artworkData.remove(selectedArtwork);
                    showSuccess("Artwork deleted successfully: " + selectedArtwork.getTitle());
                    handleClear();
                    updateStatistics();
                } else {
                    showError("Failed to delete artwork from database");
                }

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
        clearImagePreview();

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnLoadImage.setDisable(true);

        lblStatus.setText("Form cleared. Ready to add new artwork.");
        lblStatus.setStyle("-fx-text-fill: #666666;");
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Artwork Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) txtImagePath.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Upload image to resources and set resource path
            String resourcePath = uploadImageToResources(selectedFile);
            if (resourcePath != null) {
                txtImagePath.setText(resourcePath);
                loadImagePreview(resourcePath);
            }
        }
    }

    @FXML
    private void handleLoadImage() {
        String imagePath = txtImagePath.getText().trim();

        if (imagePath.isEmpty()) {
            showError("Please enter an image path or browse for an image file");
            return;
        }

        loadImagePreview(imagePath);
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

            // Create Artist entity for database
            Artist artist = new Artist();
            artist.setName(name);
            artist.setNationality(nationality);
            artist.setBirthYear(birthYear);
            artist.setBiography(biography);

            // Save to database
            boolean success = artistService.addArtist(artist);

            if (success) {
                // Reload artists from database to get the new ID
                loadArtistsFromDatabase();

                // Clear artist form
                txtArtistName.clear();
                txtNationality.clear();
                txtBirthYear.clear();
                txtBiography.clear();

                showSuccess("Artist added successfully: " + name);
            } else {
                showError("Failed to add artist to database");
            }

        } catch (NumberFormatException e) {
            showError("Invalid birth year format");
        } catch (Exception e) {
            showError("Error adding artist: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadArtistsFromDatabase(); // Reload from database
        loadArtworks();
        updateStatistics();
        showSuccess("Artwork data refreshed");
    }

    private String uploadImageToResources(File sourceFile) {
        try {
            // Get the primary target directory
            String primaryPath = getResourcesImagePath();

            if (primaryPath == null) {
                showError("Could not find resources/images directory");
                return null;
            }

            System.out.println("Primary upload path: " + primaryPath);

            // Create primary target directory
            File primaryDir = new File(primaryPath);
            if (!primaryDir.exists()) {
                boolean created = primaryDir.mkdirs();
                if (!created) {
                    showError("Could not create primary images directory");
                    return null;
                }
                System.out.println("Created primary directory: " + primaryDir.getAbsolutePath());
            }

            // Verify source file
            if (!sourceFile.exists() || !sourceFile.canRead()) {
                showError("Cannot read source file: " + sourceFile.getAbsolutePath());
                return null;
            }

            // Generate unique filename
            String originalFileName = sourceFile.getName();
            String fileExtension = "";
            String baseName = originalFileName;

            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                baseName = originalFileName.substring(0, lastDotIndex);
                fileExtension = originalFileName.substring(lastDotIndex);
            }

            // Generate unique name if file already exists
            File primaryTargetFile = new File(primaryDir, originalFileName);
            int counter = 1;
            while (primaryTargetFile.exists()) {
                String newFileName = baseName + "_" + counter + fileExtension;
                primaryTargetFile = new File(primaryDir, newFileName);
                counter++;
            }

            String finalFileName = primaryTargetFile.getName();
            System.out.println("Final filename: " + finalFileName);

            // Copy to primary location
            System.out.println("Copying to primary: " + primaryTargetFile.getAbsolutePath());
            boolean primarySuccess = copyFile(sourceFile, primaryTargetFile);

            if (!primarySuccess) {
                showError("Failed to copy file to primary location");
                return null;
            }

            // DUAL COPY: Also copy to both source and target if needed
            String userDir = System.getProperty("user.dir");
            String[] additionalPaths = {};

            // If primary is target/classes, also copy to src/main/resources
            if (primaryPath.contains("target/classes")) {
                String sourcePath = primaryPath.replace("/target/classes/", "/src/main/resources/");
                additionalPaths = new String[]{sourcePath};
                System.out.println("Primary is target, will also copy to source: " + sourcePath);
            }
            // If primary is src/main/resources, also copy to target/classes
            else if (primaryPath.contains("src/main/resources")) {
                String targetPath = primaryPath.replace("/src/main/resources/", "/target/classes/");
                File targetClassesCheck = new File(userDir + "/target/classes");
                if (targetClassesCheck.exists()) {
                    additionalPaths = new String[]{targetPath};
                    System.out.println("Primary is source, will also copy to target: " + targetPath);
                }
            }

            // Perform additional copies
            for (String additionalPath : additionalPaths) {
                try {
                    File additionalDir = new File(additionalPath);
                    if (!additionalDir.exists()) {
                        additionalDir.mkdirs();
                    }

                    File additionalFile = new File(additionalDir, finalFileName);
                    System.out.println("Additional copy to: " + additionalFile.getAbsolutePath());

                    boolean additionalSuccess = copyFile(sourceFile, additionalFile);
                    if (additionalSuccess) {
                        System.out.println("Additional copy successful");
                    } else {
                        System.err.println("Additional copy failed, but primary copy succeeded");
                    }
                } catch (Exception e) {
                    System.err.println("Additional copy error (non-fatal): " + e.getMessage());
                }
            }

            // Verify primary copy
            if (!primaryTargetFile.exists()) {
                showError("File copy verification failed");
                return null;
            }

            // Return resource path format
            String resourcePath = "/images/" + finalFileName;
            showSuccess("Image uploaded successfully: " + finalFileName);
            System.out.println("Upload completed successfully!");
            System.out.println("Resource path: " + resourcePath);
            System.out.println("File size: " + primaryTargetFile.length() + " bytes");

            return resourcePath;

        } catch (Exception e) {
            showError("Error uploading image: " + e.getMessage());
            System.err.println("Error uploading image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String getResourcesImagePath() {
        try {
            System.out.println("Looking for resources/images directory...");

            String userDir = System.getProperty("user.dir");
            System.out.println("User directory: " + userDir);

            // Priority 1: Upload to target/classes (immediately accessible)
            String targetClassesPath = userDir + "/target/classes/images";
            File targetDir = new File(targetClassesPath);

            // Check if target/classes exists (Maven compiled project)
            File targetClasses = new File(userDir + "/target/classes");
            if (targetClasses.exists() && targetClasses.isDirectory()) {
                System.out.println("Maven target/classes found, using: " + targetClassesPath);
                return targetClassesPath;
            }

            // Priority 2: Check if we can find existing target structure
            String[] targetPaths = {
                    userDir + "/target/classes/images",
                    userDir + "/target/classes/org/example/smartmuseum/images",
                    userDir + "/build/classes/main/images",  // Gradle equivalent
                    userDir + "/build/resources/main/images"
            };

            for (String path : targetPaths) {
                File dir = new File(path);
                File parent = dir.getParentFile();
                if (parent != null && parent.exists()) {
                    System.out.println("Found target directory parent, using: " + path);
                    return path;
                }
            }

            // Priority 3: Source paths (will need rebuild to be accessible)
            String[] sourcePaths = {
                    userDir + "/src/main/resources/images",
                    userDir + "/src/main/resources/org/example/smartmuseum/images"
            };

            for (String path : sourcePaths) {
                File dir = new File(path);
                File parent = dir.getParentFile();
                if (parent != null && parent.exists()) {
                    System.out.println("Using source directory: " + path);

                    // Also try to copy to target if it exists
                    String correspondingTarget = path.replace("/src/main/resources/", "/target/classes/");
                    File targetDirForSource = new File(correspondingTarget);
                    if (new File(userDir + "/target/classes").exists()) {
                        System.out.println("Will also copy to target: " + correspondingTarget);
                        // We'll handle dual copy in the upload method
                    }

                    return path;
                }
            }

            // Fallback: create in project root
            String fallbackPath = userDir + "/target/classes/images";
            System.out.println("Using fallback path: " + fallbackPath);
            return fallbackPath;

        } catch (Exception e) {
            System.err.println("Error finding resources path: " + e.getMessage());
            return System.getProperty("user.dir") + "/images";
        }
    }

    private boolean copyFile(File source, File target) {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(source);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(target);
             java.nio.channels.FileChannel sourceChannel = fis.getChannel();
             java.nio.channels.FileChannel targetChannel = fos.getChannel()) {

            // Use NIO for more efficient file copying
            long bytesTransferred = sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);

            System.out.println("Bytes transferred: " + bytesTransferred + " / " + source.length());

            // Verify the copy was successful
            boolean success = bytesTransferred == source.length() && target.exists() && target.length() == source.length();

            if (success) {
                System.out.println("File copy successful");
            } else {
                System.err.println("File copy verification failed");
                System.err.println("Expected size: " + source.length() + ", Actual size: " + (target.exists() ? target.length() : "file not found"));
            }

            return success;

        } catch (Exception e) {
            System.err.println("Error copying file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void loadImagePreview(String imagePath) {
        try {
            Image image = null;
            String loadedFrom = "";

            // Method 1: Try loading as resource (for images in resources folder)
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                image = new Image(imageUrl.toExternalForm());
                loadedFrom = "Resource: " + imageUrl;
                System.out.println("Image loaded successfully from resource: " + imageUrl);
            } else {
                // Method 2: Try alternative path without leading slash
                String altPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
                imageUrl = getClass().getResource(altPath);
                if (imageUrl != null) {
                    image = new Image(imageUrl.toExternalForm());
                    loadedFrom = "Alternative resource: " + imageUrl;
                    System.out.println("Image loaded from alternative resource path: " + imageUrl);
                } else {
                    // Method 3: Try as absolute file path (for external files)
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        image = new Image(imageFile.toURI().toString());
                        loadedFrom = "File: " + imageFile.getName();
                        System.out.println("Image loaded from file: " + imageFile.getAbsolutePath());
                    }
                }
            }

            // Set image if found
            if (image != null && !image.isError()) {
                imgPreview.setImage(image);
                lblImageStatus.setText("Image loaded succesfully");
                lblImageStatus.setStyle("-fx-text-fill: #4CAF50;");
            } else {
                clearImagePreview();
                lblImageStatus.setText("Image not found: " + imagePath);
                lblImageStatus.setStyle("-fx-text-fill: #F44336;");
                System.err.println("Image resource not found: " + imagePath);
            }

        } catch (Exception e) {
            clearImagePreview();
            lblImageStatus.setText("Error loading image: " + e.getMessage());
            lblImageStatus.setStyle("-fx-text-fill: #F44336;");
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearImagePreview() {
        imgPreview.setImage(null);
        lblImageStatus.setText("No image loaded");
        lblImageStatus.setStyle("-fx-text-fill: #666666;");
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
        artwork.setArtworkType(cmbArtworkType.getValue());
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
        artwork.setArtworkType(cmbArtworkType.getValue());
    }

    private void populateForm(ArtworkRecord artwork) {
        txtTitle.setText(artwork.getTitle());
        txtYear.setText(artwork.getYear() > 0 ? String.valueOf(artwork.getYear()) : "");
        txtTechnique.setText(artwork.getTechnique());
        txtDescription.setText(artwork.getDescription());
        txtImagePath.setText(artwork.getImagePath());
        cmbArtworkType.setValue(artwork.getArtworkType());

        // Set artist using foreign key
        for (ArtistItem artist : artistData) {
            if (artist.getArtistId() == artwork.getArtistId()) {
                cmbArtist.setValue(artist);
                break;
            }
        }
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
        private String artworkType;

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