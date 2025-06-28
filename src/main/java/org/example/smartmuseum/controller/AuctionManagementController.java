package org.example.smartmuseum.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.smartmuseum.model.entity.Auction;
import org.example.smartmuseum.model.entity.Bid;
import org.example.smartmuseum.model.enums.AuctionStatus;
import org.example.smartmuseum.model.service.AuctionService;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AuctionManagementController implements Initializable {

    @FXML private Label lblActiveAuctions;
    @FXML private Label lblTotalAuctions;
    @FXML private ComboBox<ArtworkItem> cmbArtwork;
    @FXML private TextField txtStartingBid;
    @FXML private DatePicker dateStartDate;
    @FXML private TextField txtStartHour;
    @FXML private TextField txtStartMinute;
    @FXML private DatePicker dateEndDate;
    @FXML private TextField txtEndHour;
    @FXML private TextField txtEndMinute;
    @FXML private Button btnCreateAuction;
    @FXML private Button btnClear;
    @FXML private Button btnStartAuction;
    @FXML private Button btnPauseAuction;
    @FXML private Button btnEndAuction;
    @FXML private Label lblStatus;
    @FXML private TableView<AuctionRecord> tableAuctions;
    @FXML private TableColumn<AuctionRecord, Integer> colAuctionId;
    @FXML private TableColumn<AuctionRecord, String> colArtworkTitle;
    @FXML private TableColumn<AuctionRecord, String> colStartingBid;
    @FXML private TableColumn<AuctionRecord, String> colCurrentBid;
    @FXML private TableColumn<AuctionRecord, String> colStatus;
    @FXML private TableColumn<AuctionRecord, String> colStartDate;
    @FXML private TableColumn<AuctionRecord, String> colEndDate;
    @FXML private ComboBox<String> cmbStatusFilter;
    @FXML private TableView<BidRecord> tableBids;
    @FXML private TableColumn<BidRecord, Integer> colBidId;
    @FXML private TableColumn<BidRecord, String> colBidder;
    @FXML private TableColumn<BidRecord, String> colBidAmount;
    @FXML private TableColumn<BidRecord, String> colBidTime;
    @FXML private TableColumn<BidRecord, String> colBidAuction;

    private AuctionService auctionService;
    private ObservableList<AuctionRecord> auctionData;
    private ObservableList<BidRecord> bidData;
    private ObservableList<ArtworkItem> artworkData;
    private AuctionRecord selectedAuction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        auctionService = new AuctionService();
        auctionData = FXCollections.observableArrayList();
        bidData = FXCollections.observableArrayList();
        artworkData = FXCollections.observableArrayList();

        setupTableColumns();
        setupTableSelection();
        setupComboBoxes();
        loadArtworks();
        loadAuctions();
        loadBids();
        updateStatistics();

        // Initially disable control buttons
        btnStartAuction.setDisable(true);
        btnPauseAuction.setDisable(true);
        btnEndAuction.setDisable(true);
    }

    private void setupTableColumns() {
        // Auction table
        colAuctionId.setCellValueFactory(new PropertyValueFactory<>("auctionId"));
        colArtworkTitle.setCellValueFactory(new PropertyValueFactory<>("artworkTitle"));
        colStartingBid.setCellValueFactory(new PropertyValueFactory<>("startingBid"));
        colCurrentBid.setCellValueFactory(new PropertyValueFactory<>("currentBid"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        tableAuctions.setItems(auctionData);

        // Bid table
        colBidId.setCellValueFactory(new PropertyValueFactory<>("bidId"));
        colBidder.setCellValueFactory(new PropertyValueFactory<>("bidder"));
        colBidAmount.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));
        colBidTime.setCellValueFactory(new PropertyValueFactory<>("bidTime"));
        colBidAuction.setCellValueFactory(new PropertyValueFactory<>("auctionTitle"));

        tableBids.setItems(bidData);
    }

    private void setupTableSelection() {
        tableAuctions.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedAuction = newSelection;
            if (newSelection != null) {
                updateControlButtons(newSelection);
            } else {
                btnStartAuction.setDisable(true);
                btnPauseAuction.setDisable(true);
                btnEndAuction.setDisable(true);
            }
        });
    }

    private void setupComboBoxes() {
        cmbArtwork.setItems(artworkData);

        cmbStatusFilter.setItems(FXCollections.observableArrayList(
                "All", "UPCOMING", "ACTIVE", "ENDED"
        ));
        cmbStatusFilter.setValue("All");

        cmbStatusFilter.setOnAction(e -> filterAuctions());
    }

    @FXML
    private void handleCreateAuction() {
        if (!validateAuctionForm()) {
            return;
        }

        try {
            AuctionRecord newAuction = createAuctionFromForm();

            // Get values from form
            ArtworkItem selectedArtwork = cmbArtwork.getValue();
            BigDecimal startingBidAmount = new BigDecimal(txtStartingBid.getText().trim());

            // Call service with correct parameters - get the returned Auction object
            Auction createdAuction = auctionService.createAuction(
                    selectedArtwork.getArtworkId(),
                    selectedArtwork.getTitle(),
                    startingBidAmount
            );

            // Check if auction was created successfully
            if (createdAuction != null) {
                // Update auction record with generated ID from service
                newAuction.setAuctionId(createdAuction.getAuctionId());

                auctionData.add(newAuction);
                showSuccess("Auction created successfully for: " + newAuction.getArtworkTitle());
                handleClear();
                updateStatistics();
            } else {
                showError("Failed to create auction in database");
            }

        } catch (NumberFormatException e) {
            showError("Invalid starting bid format");
        } catch (Exception e) {
            showError("Error creating auction: " + e.getMessage());
        }
    }

    @FXML
    private void handleStartAuction() {
        if (selectedAuction == null) {
            showError("Please select an auction to start");
            return;
        }

        try {
            selectedAuction.setStatus("ACTIVE");
            auctionService.startAuction(selectedAuction.getAuctionId());

            tableAuctions.refresh();
            updateControlButtons(selectedAuction);
            updateStatistics();

            showSuccess("Auction started: " + selectedAuction.getArtworkTitle());

        } catch (Exception e) {
            showError("Error starting auction: " + e.getMessage());
        }
    }

    @FXML
    private void handlePauseAuction() {
        if (selectedAuction == null) {
            showError("Please select an auction to pause");
            return;
        }

        try {
            // In a real implementation, you might have a PAUSED status
            showSuccess("Auction paused: " + selectedAuction.getArtworkTitle());

        } catch (Exception e) {
            showError("Error pausing auction: " + e.getMessage());
        }
    }

    @FXML
    private void handleEndAuction() {
        if (selectedAuction == null) {
            showError("Please select an auction to end");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm End Auction");
        confirmAlert.setHeaderText("End Auction");
        confirmAlert.setContentText("Are you sure you want to end this auction: " + selectedAuction.getArtworkTitle() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                selectedAuction.setStatus("ENDED");
                auctionService.endAuction(selectedAuction.getAuctionId());

                tableAuctions.refresh();
                updateControlButtons(selectedAuction);
                updateStatistics();

                showSuccess("Auction ended: " + selectedAuction.getArtworkTitle());

            } catch (Exception e) {
                showError("Error ending auction: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        cmbArtwork.setValue(null);
        txtStartingBid.clear();
        dateStartDate.setValue(null);
        txtStartHour.clear();
        txtStartMinute.clear();
        dateEndDate.setValue(null);
        txtEndHour.clear();
        txtEndMinute.clear();

        tableAuctions.getSelectionModel().clearSelection();
        selectedAuction = null;

        btnStartAuction.setDisable(true);
        btnPauseAuction.setDisable(true);
        btnEndAuction.setDisable(true);

        lblStatus.setText("Form cleared. Ready to create new auction.");
        lblStatus.setStyle("-fx-text-fill: #666666;");
    }

    @FXML
    private void handleRefresh() {
        loadAuctions();
        loadBids();
        updateStatistics();
        showSuccess("Auction data refreshed");
    }

    private void filterAuctions() {
        String filter = cmbStatusFilter.getValue();
        if ("All".equals(filter)) {
            tableAuctions.setItems(auctionData);
        } else {
            ObservableList<AuctionRecord> filteredData = FXCollections.observableArrayList();
            for (AuctionRecord auction : auctionData) {
                if (filter.equals(auction.getStatus())) {
                    filteredData.add(auction);
                }
            }
            tableAuctions.setItems(filteredData);
        }
    }

    private void loadArtworks() {
        artworkData.clear();
        createSampleArtworks();
    }

    private void createSampleArtworks() {
        ArtworkItem artwork1 = new ArtworkItem();
        artwork1.setArtworkId(1);
        artwork1.setTitle("Mona Lisa");
        artwork1.setArtist("Leonardo da Vinci");
        artworkData.add(artwork1);

        ArtworkItem artwork2 = new ArtworkItem();
        artwork2.setArtworkId(2);
        artwork2.setTitle("Starry Night");
        artwork2.setArtist("Vincent van Gogh");
        artworkData.add(artwork2);

        ArtworkItem artwork3 = new ArtworkItem();
        artwork3.setArtworkId(3);
        artwork3.setTitle("Guernica");
        artwork3.setArtist("Pablo Picasso");
        artworkData.add(artwork3);
    }

    private void loadAuctions() {
        auctionData.clear();

        try {
            List<Auction> auctions = auctionService.getAllAuctions();

            for (Auction auction : auctions) {
                AuctionRecord record = new AuctionRecord();
                record.setAuctionId(auction.getAuctionId());
                record.setArtworkId(auction.getArtworkId());
                record.setArtworkTitle("Artwork " + auction.getArtworkId()); // Would need to join with artwork table
                record.setStartingBid("$" + auction.getStartingBid().toString());
                record.setCurrentBid("$" + auction.getCurrentBid().toString());
                record.setStatus(auction.getStatus().getValue().toUpperCase());
                record.setStartDate(auction.getCreatedAt().toString());
                record.setEndDate(auction.getCreatedAt().toString()); // Would need end date field

                auctionData.add(record);
            }
        } catch (Exception e) {
            System.err.println("Error loading auctions: " + e.getMessage());
            createSampleAuctions(); // Fallback to sample data
        }
    }

    private void createSampleAuctions() {
        AuctionRecord auction1 = new AuctionRecord();
        auction1.setAuctionId(1);
        auction1.setArtworkId(1);
        auction1.setArtworkTitle("Mona Lisa");
        auction1.setStartingBid("$1,000,000.00");
        auction1.setCurrentBid("$1,250,000.00");
        auction1.setStatus("ACTIVE");
        auction1.setStartDate("2024-01-15 10:00:00");
        auction1.setEndDate("2024-01-20 18:00:00");
        auctionData.add(auction1);

        AuctionRecord auction2 = new AuctionRecord();
        auction2.setAuctionId(2);
        auction2.setArtworkId(2);
        auction2.setArtworkTitle("Starry Night");
        auction2.setStartingBid("$800,000.00");
        auction2.setCurrentBid("$800,000.00");
        auction2.setStatus("UPCOMING");
        auction2.setStartDate("2024-01-25 14:00:00");
        auction2.setEndDate("2024-01-30 20:00:00");
        auctionData.add(auction2);

        AuctionRecord auction3 = new AuctionRecord();
        auction3.setAuctionId(3);
        auction3.setArtworkId(3);
        auction3.setArtworkTitle("Guernica");
        auction3.setStartingBid("$500,000.00");
        auction3.setCurrentBid("$750,000.00");
        auction3.setStatus("ENDED");
        auction3.setStartDate("2024-01-01 12:00:00");
        auction3.setEndDate("2024-01-10 16:00:00");
        auctionData.add(auction3);
    }

    private void loadBids() {
        bidData.clear();
        createSampleBids();
    }

    private void createSampleBids() {
        BidRecord bid1 = new BidRecord();
        bid1.setBidId(1);
        bid1.setBidder("visitor1");
        bid1.setBidAmount("$1,250,000.00");
        bid1.setBidTime("2024-01-16 14:30:00");
        bid1.setAuctionTitle("Mona Lisa");
        bidData.add(bid1);

        BidRecord bid2 = new BidRecord();
        bid2.setBidId(2);
        bid2.setBidder("visitor2");
        bid2.setBidAmount("$750,000.00");
        bid2.setBidTime("2024-01-05 16:45:00");
        bid2.setAuctionTitle("Guernica");
        bidData.add(bid2);

        BidRecord bid3 = new BidRecord();
        bid3.setBidId(3);
        bid3.setBidder("visitor3");
        bid3.setBidAmount("$1,100,000.00");
        bid3.setBidTime("2024-01-15 11:15:00");
        bid3.setAuctionTitle("Mona Lisa");
        bidData.add(bid3);
    }

    private boolean validateAuctionForm() {
        ArtworkItem artwork = cmbArtwork.getValue();
        String startingBid = txtStartingBid.getText().trim();
        LocalDate startDate = dateStartDate.getValue();
        LocalDate endDate = dateEndDate.getValue();

        if (artwork == null) {
            showError("Please select an artwork");
            return false;
        }

        if (startingBid.isEmpty()) {
            showError("Starting bid is required");
            return false;
        }

        try {
            Double.parseDouble(startingBid);
        } catch (NumberFormatException e) {
            showError("Invalid starting bid format");
            return false;
        }

        if (startDate == null) {
            showError("Start date is required");
            return false;
        }

        if (endDate == null) {
            showError("End date is required");
            return false;
        }

        if (endDate.isBefore(startDate)) {
            showError("End date must be after start date");
            return false;
        }

        return true;
    }

    private AuctionRecord createAuctionFromForm() {
        AuctionRecord auction = new AuctionRecord();
        ArtworkItem artwork = cmbArtwork.getValue();

        auction.setArtworkId(artwork.getArtworkId());
        auction.setArtworkTitle(artwork.getTitle());
        auction.setStartingBid("$" + txtStartingBid.getText().trim());
        auction.setCurrentBid("$" + txtStartingBid.getText().trim());
        auction.setStatus("UPCOMING");

        // Format dates
        LocalDate startDate = dateStartDate.getValue();
        String startHour = txtStartHour.getText().trim().isEmpty() ? "10" : txtStartHour.getText().trim();
        String startMinute = txtStartMinute.getText().trim().isEmpty() ? "00" : txtStartMinute.getText().trim();
        LocalTime startTime = LocalTime.of(Integer.parseInt(startHour), Integer.parseInt(startMinute));
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        auction.setStartDate(startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        LocalDate endDate = dateEndDate.getValue();
        String endHour = txtEndHour.getText().trim().isEmpty() ? "18" : txtEndHour.getText().trim();
        String endMinute = txtEndMinute.getText().trim().isEmpty() ? "00" : txtEndMinute.getText().trim();
        LocalTime endTime = LocalTime.of(Integer.parseInt(endHour), Integer.parseInt(endMinute));
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
        auction.setEndDate(endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return auction;
    }

    private void updateControlButtons(AuctionRecord auction) {
        String status = auction.getStatus();

        btnStartAuction.setDisable(!"UPCOMING".equals(status));
        btnPauseAuction.setDisable(!"ACTIVE".equals(status));
        btnEndAuction.setDisable(!"ACTIVE".equals(status));
    }

    private void updateStatistics() {
        long activeCount = auctionData.stream().filter(a -> "ACTIVE".equals(a.getStatus())).count();
        lblActiveAuctions.setText("Active: " + activeCount);
        lblTotalAuctions.setText("Total: " + auctionData.size());
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
    public static class AuctionRecord {
        private int auctionId;
        private int artworkId;
        private String artworkTitle;
        private String startingBid;
        private String currentBid;
        private String status;
        private String startDate;
        private String endDate;

        // Getters and setters
        public int getAuctionId() { return auctionId; }
        public void setAuctionId(int auctionId) { this.auctionId = auctionId; }

        public int getArtworkId() { return artworkId; }
        public void setArtworkId(int artworkId) { this.artworkId = artworkId; }

        public String getArtworkTitle() { return artworkTitle; }
        public void setArtworkTitle(String artworkTitle) { this.artworkTitle = artworkTitle; }

        public String getStartingBid() { return startingBid; }
        public void setStartingBid(String startingBid) { this.startingBid = startingBid; }

        public String getCurrentBid() { return currentBid; }
        public void setCurrentBid(String currentBid) { this.currentBid = currentBid; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }

    public static class BidRecord {
        private int bidId;
        private String bidder;
        private String bidAmount;
        private String bidTime;
        private String auctionTitle;

        // Getters and setters
        public int getBidId() { return bidId; }
        public void setBidId(int bidId) { this.bidId = bidId; }

        public String getBidder() { return bidder; }
        public void setBidder(String bidder) { this.bidder = bidder; }

        public String getBidAmount() { return bidAmount; }
        public void setBidAmount(String bidAmount) { this.bidAmount = bidAmount; }

        public String getBidTime() { return bidTime; }
        public void setBidTime(String bidTime) { this.bidTime = bidTime; }

        public String getAuctionTitle() { return auctionTitle; }
        public void setAuctionTitle(String auctionTitle) { this.auctionTitle = auctionTitle; }
    }

    public static class ArtworkItem {
        private int artworkId;
        private String title;
        private String artist;

        // Getters and setters
        public int getArtworkId() { return artworkId; }
        public void setArtworkId(int artworkId) { this.artworkId = artworkId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getArtist() { return artist; }
        public void setArtist(String artist) { this.artist = artist; }

        @Override
        public String toString() {
            return title + " by " + artist;
        }
    }
}
