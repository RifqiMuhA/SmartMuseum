package org.example.smartmuseum.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.smartmuseum.controller.AuctionSocketController;
import org.example.smartmuseum.database.DatabaseConnection;
import org.example.smartmuseum.model.entity.Auction;
import org.example.smartmuseum.model.entity.Bid;
import org.example.smartmuseum.model.enums.AuctionStatus;
import org.example.smartmuseum.model.service.AuctionService;
import org.example.smartmuseum.util.SessionContext;
import org.example.smartmuseum.model.service.ArtworkService;
import org.example.smartmuseum.model.entity.Artwork;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Updated AuctionManagementController with socket integration
 * Manages auctions with real-time bidding support
 */
public class AuctionManagementController implements Initializable, SessionAwareController {

    @FXML private Label lblActiveAuctions;
    @FXML private Label lblTotalAuctions;
    @FXML private ComboBox<ArtworkItem> cmbArtwork;
    @FXML private TextField txtStartingBid;
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
    private AuctionSocketController socketController;
    private ObservableList<AuctionRecord> auctionData;
    private ObservableList<BidRecord> bidData;
    private ObservableList<ArtworkItem> artworkData;
    private AuctionRecord selectedAuction;
    private SessionContext sessionContext;
    private ArtworkService artworkService;

    @Override
    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
        System.out.println("AuctionManagementController received session context: " + sessionContext);
    }

    @Override
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        auctionService = new AuctionService();
        artworkService = new ArtworkService();

        // Ambil socket controller dari auction service (JANGAN buat baru)
        socketController = auctionService.getSocketController();

        // Initialize data structures
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
        updateControlButtonsState();

        // Socket server sudah distart otomatis di AuctionService constructor
        showInfo("Auction management system ready. Socket server started automatically.");
    }

    private void setupTableColumns() {
        // Auction table
        colAuctionId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getAuctionId()).asObject());
        colArtworkTitle.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getArtworkTitle()));
        colStartingBid.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStartingBid()));
        colCurrentBid.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCurrentBid()));
        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));
        colStartDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStartDate()));
        colEndDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEndDate()));

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
                loadBidsForAuction(newSelection.getAuctionId());
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
        // Cek apakah ada auction yang sedang aktif
        if (auctionService.hasActiveAuction()) {
            showError("Cannot create auction: There is already an active auction running");
            return;
        }

        if (!validateAuctionForm()) {
            return;
        }

        try {
            ArtworkItem selectedArtwork = cmbArtwork.getValue();
            BigDecimal startingBidAmount = new BigDecimal(txtStartingBid.getText().trim());

            // Create auction (tanpa start/end date manual)
            Auction createdAuction = auctionService.createAuction(
                    selectedArtwork.getArtworkId(),
                    selectedArtwork.getTitle(),
                    startingBidAmount
            );

            if (createdAuction != null) {
                AuctionRecord newAuction = createAuctionRecordFromEntity(createdAuction, selectedArtwork);
                auctionData.add(newAuction);

                showSuccess("Auction created successfully for: " + newAuction.getArtworkTitle());
                handleClear();
                updateStatistics();
                updateControlButtonsState();
            } else {
                showError("Failed to create auction in database");
            }

        } catch (NumberFormatException e) {
            showError("Invalid starting bid format");
        } catch (Exception e) {
            showError("Error creating auction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStartAuction() {
        if (selectedAuction == null) {
            showError("Please select an auction to start");
            return;
        }

        // Cek apakah ada auction lain yang aktif
        if (auctionService.hasActiveAuction()) {
            // Cek apakah auction yang dipilih adalah yang sedang aktif
            Auction activeAuction = auctionService.getActiveAuction();
            if (activeAuction == null || activeAuction.getAuctionId() != selectedAuction.getAuctionId()) {
                showError("Cannot start auction: Another auction is already active");
                return;
            }
        }

        if (!"UPCOMING".equals(selectedAuction.getStatus())) {
            showError("Can only start auctions with UPCOMING status");
            return;
        }

        // Confirm dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Start Auction");
        confirmAlert.setHeaderText("Start Live Auction");
        confirmAlert.setContentText("Start auction for: " + selectedAuction.getArtworkTitle() +
                "\nThis will begin a 30-second countdown timer.\n" +
                "Start and end times will be set automatically.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                System.out.println("🚀 Attempting to start auction " + selectedAuction.getAuctionId());

                // Start auction - ini akan otomatis set start_date dan mulai timer
                boolean started = auctionService.startAuction(selectedAuction.getAuctionId());

                if (started) {
                    // Update UI
                    selectedAuction.setStatus("ACTIVE");
                    selectedAuction.setStartDate(java.time.LocalDateTime.now().toString());

                    tableAuctions.refresh();
                    updateControlButtonsState();
                    updateStatistics();

                    showSuccess("🚀 Auction started! 30-second timer is running. Start time recorded automatically.");
                    System.out.println("✅ Successfully started auction " + selectedAuction.getAuctionId());
                } else {
                    showError("Failed to start auction. Check console for details.");
                }

            } catch (Exception e) {
                showError("Error starting auction: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handlePauseAuction() {
        if (selectedAuction == null) {
            showError("Please select an auction to pause");
            return;
        }

        try {
            // For now, pause is not implemented in socket controller
            showInfo("Pause functionality not yet implemented for real-time auctions");

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
                // Update status in table
                selectedAuction.setStatus("ENDED");

                // End auction through service (which will handle socket communication)
                auctionService.endAuction(selectedAuction.getAuctionId());

                tableAuctions.refresh();
                updateControlButtons(selectedAuction);
                updateStatistics();

                showSuccess("Auction ended: " + selectedAuction.getArtworkTitle() + " (ID: " + selectedAuction.getAuctionId() + ")");
                System.out.println("🏁 Ended auction " + selectedAuction.getAuctionId() + " through socket controller");

            } catch (Exception e) {
                showError("Error ending auction: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleClear() {
        cmbArtwork.setValue(null);
        txtStartingBid.clear();

        tableAuctions.getSelectionModel().clearSelection();
        selectedAuction = null;

        updateControlButtonsState();

        lblStatus.setText("Form cleared. Ready to create new auction.");
        lblStatus.setStyle("-fx-text-fill: #666666;");
    }

    @FXML
    private void handleRefresh() {
        try {
            loadArtworks();
            loadAuctions();
            loadBids();
            updateStatistics();

            // Reset filter
            cmbStatusFilter.setValue("All");
            filterAuctions();

            showSuccess("All data refreshed successfully from database");

        } catch (Exception e) {
            showError("Error refreshing data: " + e.getMessage());
            System.err.println("Error during refresh: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowSocketStats() {
        if (socketController != null) {
            String stats = socketController.getAuctionStats();

            // Tambahkan info timer
            String timerInfo = "\nTimer Status:\n";
            if (auctionService.hasActiveAuction()) {
                Auction activeAuction = auctionService.getActiveAuction();
                if (activeAuction != null) {
                    int remainingTime = socketController.getAuctionTimer().getRemainingTime(activeAuction.getAuctionId());
                    timerInfo += "Active Auction " + activeAuction.getAuctionId() + ": " + remainingTime + " seconds remaining\n";
                }
            } else {
                timerInfo += "No active auction\n";
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Socket Server Statistics");
            alert.setHeaderText("Real-time Auction Statistics");
            alert.setContentText(stats + timerInfo);
            alert.showAndWait();
        } else {
            showError("Socket controller not available");
        }
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

        try {
            List<Artwork> artworks = artworkService.getAllArtworks();

            for (Artwork artwork : artworks) {
                ArtworkItem item = new ArtworkItem();
                item.setArtworkId(artwork.getArtworkId());
                item.setTitle(artwork.getTitle());

                // Gunakan artist name jika ada, atau fallback ke artist_id
                if (artwork.getArtistName() != null) {
                    item.setArtist(artwork.getArtistName());
                } else {
                    item.setArtist("Artist " + artwork.getArtistId());
                }

                artworkData.add(item);
            }

            System.out.println("✅ Loaded " + artworkData.size() + " artworks from database via ArtworkService");

        } catch (Exception e) {
            System.err.println("Error loading artworks via service: " + e.getMessage());
            e.printStackTrace();
            // Fallback ke sample data jika gagal
            createSampleArtworks();
        }
    }

    private void createSampleArtworks() {
        ArtworkItem artwork1 = new ArtworkItem();
        artwork1.setArtworkId(1);
        artwork1.setTitle("Ming Dynasty Vase");
        artwork1.setArtist("Unknown Artist");
        artworkData.add(artwork1);

        ArtworkItem artwork2 = new ArtworkItem();
        artwork2.setArtworkId(2);
        artwork2.setTitle("Ancient Pottery");
        artwork2.setArtist("Traditional Craftsman");
        artworkData.add(artwork2);

        ArtworkItem artwork3 = new ArtworkItem();
        artwork3.setArtworkId(3);
        artwork3.setTitle("Bronze Sculpture");
        artwork3.setArtist("Classical Artist");
        artworkData.add(artwork3);
    }

    private void loadAuctions() {
        auctionData.clear();

        try {
            // Ambil data dari service
            List<Auction> auctions = auctionService.getAllAuctions();
            List<Artwork> artworks = artworkService.getAllArtworks();

            // Buat map untuk mapping artwork_id ke artwork
            Map<Integer, Artwork> artworkMap = artworks.stream()
                    .collect(Collectors.toMap(Artwork::getArtworkId, artwork -> artwork));

            for (Auction auction : auctions) {
                AuctionRecord record = new AuctionRecord();
                record.setAuctionId(auction.getAuctionId());
                record.setArtworkId(auction.getArtworkId());

                // Gunakan artwork title dari map atau fallback
                Artwork artwork = artworkMap.get(auction.getArtworkId());
                if (artwork != null) {
                    record.setArtworkTitle(artwork.getTitle());
                } else {
                    record.setArtworkTitle("Artwork " + auction.getArtworkId());
                }

                record.setStartingBid("Rp " + formatCurrency(auction.getStartingBid()));
                record.setCurrentBid("Rp " + formatCurrency(auction.getCurrentBid()));
                record.setStatus(auction.getStatus().getValue().toUpperCase());

                if (auction.getStartDate() != null) {
                    record.setStartDate(auction.getStartDate().toString());
                } else {
                    record.setStartDate("-");
                }

                if (auction.getEndDate() != null) {
                    record.setEndDate(auction.getEndDate().toString());
                } else {
                    record.setEndDate("-");
                }

                auctionData.add(record);
            }

            System.out.println("✅ Loaded " + auctionData.size() + " auctions from database via AuctionService");

        } catch (Exception e) {
            System.err.println("Error loading auctions via service: " + e.getMessage());
            e.printStackTrace();
            // Fallback ke sample data jika gagal
            createSampleAuctions();
        }
    }

    private void createSampleAuctions() {
        // Keep existing sample data as fallback
        AuctionRecord auction1 = new AuctionRecord();
        auction1.setAuctionId(1);
        auction1.setArtworkId(1);
        auction1.setArtworkTitle("Ming Dynasty Vase");
        auction1.setStartingBid("Rp 500,000");
        auction1.setCurrentBid("Rp 750,000");
        auction1.setStatus("UPCOMING");
        auction1.setStartDate("2024-01-15 10:00:00");
        auction1.setEndDate("2024-01-20 18:00:00");
        auctionData.add(auction1);
    }

    private void loadBids() {
        bidData.clear();

        if (selectedAuction != null) {
            loadBidsForAuction(selectedAuction.getAuctionId());
        } else {
            // Jika tidak ada auction yang dipilih, bisa memuat semua bids atau kosong
            System.out.println("No auction selected, bid table cleared");
        }
    }

    private void loadBidsForAuction(int auctionId) {
        bidData.clear();

        try {
            List<Bid> bids = auctionService.getAuctionBidsFromDatabase(auctionId);

            // Ambil info auction untuk title
            Auction auction = auctionService.getAuction(auctionId);
            String auctionTitle = "Auction " + auctionId;

            if (auction != null) {
                // Cari artwork untuk auction ini
                List<Artwork> artworks = artworkService.getAllArtworks();
                for (Artwork artwork : artworks) {
                    if (artwork.getArtworkId() == auction.getArtworkId()) {
                        auctionTitle = artwork.getTitle();
                        break;
                    }
                }
            }

            for (Bid bid : bids) {
                BidRecord record = new BidRecord();
                record.setBidId(bid.getBidId());
                record.setBidder("User " + bid.getUserId()); // Bisa diperbaiki jika ada UserService
                record.setBidAmount("Rp " + formatCurrency(bid.getBidAmount()));
                record.setBidTime(bid.getBidTime().toString());
                record.setAuctionTitle(auctionTitle);
                bidData.add(record);
            }

            System.out.println("✅ Loaded " + bids.size() + " bids for auction " + auctionId + " via AuctionService");

        } catch (Exception e) {
            System.err.println("Error loading bids for auction " + auctionId + " via service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createSampleBids() {
        // Keep sample bids as fallback
        BidRecord bid1 = new BidRecord();
        bid1.setBidId(1);
        bid1.setBidder("user1");
        bid1.setBidAmount("Rp 750,000");
        bid1.setBidTime("2024-01-16 14:30:00");
        bid1.setAuctionTitle("Ming Dynasty Vase");
        bidData.add(bid1);
    }

    private boolean validateAuctionForm() {
        ArtworkItem artwork = cmbArtwork.getValue();
        String startingBid = txtStartingBid.getText().trim();

        if (artwork == null) {
            showError("Please select an artwork");
            return false;
        }

        if (startingBid.isEmpty()) {
            showError("Starting bid is required");
            return false;
        }

        try {
            BigDecimal bidAmount = new BigDecimal(startingBid);
            if (bidAmount.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Starting bid must be greater than 0");
                return false;
            }
            if (bidAmount.compareTo(new BigDecimal("50000")) < 0) {
                showError("Starting bid must be at least Rp 50,000");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid starting bid format. Please enter numbers only.");
            return false;
        }

        return true;
    }

    private AuctionRecord createAuctionRecordFromEntity(Auction auction, ArtworkItem artwork) {
        AuctionRecord record = new AuctionRecord();
        record.setAuctionId(auction.getAuctionId());
        record.setArtworkId(auction.getArtworkId());
        record.setArtworkTitle(artwork.getTitle());
        record.setStartingBid("Rp " + formatCurrency(auction.getStartingBid()));
        record.setCurrentBid("Rp " + formatCurrency(auction.getCurrentBid()));
        record.setStatus(auction.getStatus().getValue().toUpperCase());

        if (auction.getStartDate() != null) {
            record.setStartDate(auction.getStartDate().toString());
        } else {
            record.setStartDate("-");
        }

        if (auction.getEndDate() != null) {
            record.setEndDate(auction.getEndDate().toString());
        } else {
            record.setEndDate("-");
        }

        return record;
    }

    private void updateControlButtons(AuctionRecord auction) {
        String status = auction.getStatus();

        btnStartAuction.setDisable(!"UPCOMING".equals(status));
        btnPauseAuction.setDisable(!"ACTIVE".equals(status));
        btnEndAuction.setDisable(!"ACTIVE".equals(status));
    }

    private void updateControlButtonsState() {
        boolean hasActiveAuction = auctionService.hasActiveAuction();

        if (selectedAuction == null) {
            btnStartAuction.setDisable(true);
            btnPauseAuction.setDisable(true);
            btnEndAuction.setDisable(true);
            return;
        }

        String status = selectedAuction.getStatus();

        // Start button: enabled if UPCOMING and no other auction is active
        btnStartAuction.setDisable(!"UPCOMING".equals(status) || hasActiveAuction);

        // End button: enabled if this auction is ACTIVE
        btnEndAuction.setDisable(!"ACTIVE".equals(status));

        // Pause button: disabled (not implemented in real-time auction)
        btnPauseAuction.setDisable(true);

        // Create button: disabled if there's an active auction
        btnCreateAuction.setDisable(hasActiveAuction);
    }

    private void updateStatistics() {
        long activeCount = auctionData.stream().filter(a -> "ACTIVE".equals(a.getStatus())).count();
        lblActiveAuctions.setText("Active: " + activeCount);
        lblTotalAuctions.setText("Total: " + auctionData.size());
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,d", amount.longValue());
    }

    private void showSuccess(String message) {
        lblStatus.setText("✓ " + message);
        lblStatus.setStyle("-fx-text-fill: #4CAF50;");
    }

    private void showError(String message) {
        lblStatus.setText("✗ " + message);
        lblStatus.setStyle("-fx-text-fill: #F44336;");
    }

    private void showInfo(String message) {
        lblStatus.setText("ℹ " + message);
        lblStatus.setStyle("-fx-text-fill: #0D6EFD;");
    }

    @Override
    public void cleanup() {
        if (socketController != null) {
            socketController.stop();
        }
        if (auctionService != null) {
            auctionService.cleanup();
        }
        SessionAwareController.super.cleanup();
    }

    // Inner classes for table data (same as before)
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