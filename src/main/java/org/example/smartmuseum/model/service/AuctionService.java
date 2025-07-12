package org.example.smartmuseum.model.service;

import org.example.smartmuseum.database.DatabaseConnection;
import org.example.smartmuseum.model.entity.Auction;
import org.example.smartmuseum.model.entity.Bid;
import org.example.smartmuseum.model.enums.AuctionStatus;
import org.example.smartmuseum.controller.AuctionSocketController;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.sql.*;

/**
 * Enhanced AuctionService with socket integration and timer management
 * Handles real-time auction operations with automatic timer controls
 */
public class AuctionService {
    private static final BigDecimal MIN_BID_INCREMENT = new BigDecimal("10000");

    private ConcurrentMap<Integer, Auction> activeAuctions;
    private ConcurrentMap<Integer, List<Bid>> auctionBids;
    private AuctionSocketController socketController;
    private AuctionTimer auctionTimer;

    public AuctionService() {
        this.activeAuctions = new ConcurrentHashMap<>();
        this.auctionBids = new ConcurrentHashMap<>();
        this.auctionTimer = new AuctionTimer();

        // Initialize socket controller
        initializeSocketController();

        loadAuctionsFromDatabase();
    }

    private void initializeSocketController() {
        this.socketController = new AuctionSocketController();
        this.socketController.setAuctionService(this);

        // Start socket server in background
        Thread socketThread = new Thread(() -> {
            try {
                socketController.start();
            } catch (Exception e) {
                System.err.println("Failed to start socket server: " + e.getMessage());
            }
        });
        socketThread.setDaemon(true);
        socketThread.start();
    }

    public boolean hasActiveAuction() {
        return activeAuctions.values().stream()
                .anyMatch(auction -> auction.getStatus() == AuctionStatus.ACTIVE);
    }

    public Auction getActiveAuction() {
        return activeAuctions.values().stream()
                .filter(auction -> auction.getStatus() == AuctionStatus.ACTIVE)
                .findFirst()
                .orElse(null);
    }

    public AuctionSocketController getSocketController() {
        return socketController;
    }

    public void setSocketController(AuctionSocketController socketController) {
        this.socketController = socketController;
    }

    public void startSocketServer() {
        if (socketController != null && !socketController.isRunning()) {
            Thread socketThread = new Thread(() -> {
                try {
                    socketController.start();
                } catch (Exception e) {
                    System.err.println("Error starting socket server: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            socketThread.setDaemon(true);
            socketThread.start();
            System.out.println("🚀 Auction socket server started in background");
        } else {
            System.out.println("ℹ️ Socket server already running or controller is null");
        }
    }

    private void loadAuctionsFromDatabase() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "SELECT * FROM auctions ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Auction auction = new Auction();
                auction.setAuctionId(rs.getInt("auction_id"));
                auction.setArtworkId(rs.getInt("artwork_id"));
                auction.setStartingBid(rs.getBigDecimal("starting_bid"));
                auction.setCurrentBid(rs.getBigDecimal("current_bid"));

                // Handle case-insensitive status parsing
                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    try {
                        auction.setStatus(AuctionStatus.valueOf(statusStr.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Unknown auction status: " + statusStr + ", defaulting to UPCOMING");
                        auction.setStatus(AuctionStatus.UPCOMING);
                    }
                } else {
                    auction.setStatus(AuctionStatus.UPCOMING);
                }

                auction.setStartDate(rs.getTimestamp("start_date"));
                auction.setEndDate(rs.getTimestamp("end_date"));

                // Handle potential null winner_id
                int winnerId = rs.getInt("winner_id");
                if (!rs.wasNull()) {
                    auction.setWinnerId(winnerId);
                }

                auction.setCreatedAt(rs.getTimestamp("created_at"));
                activeAuctions.put(auction.getAuctionId(), auction);
            }

            System.out.println("Loaded " + activeAuctions.size() + " auctions from database");

        } catch (SQLException e) {
            System.err.println("Error loading auctions from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Auction> getAllAuctions() {
        return new ArrayList<>(activeAuctions.values());
    }

    public List<Auction> getActiveAuctions() {
        return activeAuctions.values().stream()
                .filter(auction -> auction.getStatus() == AuctionStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    public int getActiveAuctionCount() {
        return getActiveAuctions().size();
    }

    public boolean placeBid(int auctionId, Bid bid) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            System.out.println("Auction not found: " + auctionId);
            return false;
        }

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            System.out.println("Auction is not active: " + auctionId);
            return false;
        }

        BigDecimal minimumBid = auction.getCurrentBid().add(MIN_BID_INCREMENT);
        if (bid.getBidAmount().compareTo(minimumBid) < 0) {
            System.out.println("Bid amount too low. Minimum: Rp " + formatCurrency(minimumBid));
            return false;
        }

        // Update auction current bid
        auction.setCurrentBid(bid.getBidAmount());

        // Store bid
        List<Bid> bids = auctionBids.computeIfAbsent(auctionId, k -> new ArrayList<>());
        bids.add(bid);

        // Save to database
        saveBidToDatabase(bid);
        updateAuctionInDatabase(auction);

        System.out.println("✅ Bid placed successfully: Rp " + formatCurrency(bid.getBidAmount()) +
                " on auction " + auctionId + " by user " + bid.getUserId());

        return true;
    }

    public boolean startAuction(int auctionId) {
        // Check if another auction is already active
        if (hasActiveAuction()) {
            System.err.println("Cannot start auction " + auctionId + ": Another auction is already active");
            return false;
        }

        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            System.err.println("Auction not found: " + auctionId);
            return false;
        }

        if (auction.getStatus() != AuctionStatus.UPCOMING) {
            System.err.println("Auction " + auctionId + " is not in UPCOMING status");
            return false;
        }

        try {
            // Set start time to NOW
            Timestamp startTime = new Timestamp(System.currentTimeMillis());
            auction.setStartDate(startTime);
            auction.setStatus(AuctionStatus.ACTIVE);

            // Update in database
            updateAuctionInDatabase(auction);

            // Start through socket controller
            socketController.startAuction(auctionId);

            System.out.println("🚀 Auction " + auctionId + " started at " + startTime);
            return true;

        } catch (Exception e) {
            System.err.println("Error starting auction " + auctionId + ": " + e.getMessage());
            return false;
        }
    }

    public void endAuction(int auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            System.err.println("Auction not found: " + auctionId);
            return;
        }

        try {
            // Set end time to NOW
            Timestamp endTime = new Timestamp(System.currentTimeMillis());
            auction.setEndDate(endTime);
            auction.setStatus(AuctionStatus.ENDED);

            // Determine winner
            List<Bid> bids = getAuctionBids(auctionId);
            if (!bids.isEmpty()) {
                Bid winningBid = bids.stream()
                        .max((b1, b2) -> b1.getBidAmount().compareTo(b2.getBidAmount()))
                        .orElse(null);

                if (winningBid != null) {
                    auction.setWinnerId(winningBid.getUserId());
                    System.out.println("🏆 Auction " + auctionId + " winner: User " +
                            winningBid.getUserId() + " with bid: Rp " +
                            formatCurrency(winningBid.getBidAmount()));
                }
            }

            // Update in database
            updateAuctionInDatabase(auction);

            // End through socket controller
            socketController.endAuction(auctionId);

            System.out.println("🏁 Auction " + auctionId + " ended at " + endTime);

        } catch (Exception e) {
            System.err.println("Error ending auction " + auctionId + ": " + e.getMessage());
        }
    }

    public Auction createAuction(int artworkId, String artworkTitle, BigDecimal startingBid) {
        // Check if there's already an active auction
        if (hasActiveAuction()) {
            System.err.println("Cannot create auction: Another auction is already active");
            return null;
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "INSERT INTO auctions (artwork_id, starting_bid, current_bid, status, created_at) VALUES (?, ?, ?, ?, NOW())";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, artworkId);
            stmt.setBigDecimal(2, startingBid);
            stmt.setBigDecimal(3, startingBid);
            stmt.setString(4, AuctionStatus.UPCOMING.getValue());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int auctionId = generatedKeys.getInt(1);

                    Auction auction = new Auction();
                    auction.setAuctionId(auctionId);
                    auction.setArtworkId(artworkId);
                    auction.setStartingBid(startingBid);
                    auction.setCurrentBid(startingBid);
                    auction.setStatus(AuctionStatus.UPCOMING);
                    auction.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    // start_date dan end_date akan diisi otomatis saat start/end

                    activeAuctions.put(auctionId, auction);
                    System.out.println("✅ Created auction " + auctionId + " for artwork: " + artworkTitle);

                    return auction;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating auction: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void saveBidToDatabase(Bid bid) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "INSERT INTO bids (auction_id, user_id, bid_amount, bid_time) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, bid.getAuctionId());
            stmt.setInt(2, bid.getUserId());
            stmt.setBigDecimal(3, bid.getBidAmount());
            stmt.setTimestamp(4, bid.getBidTime());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving bid to database: " + e.getMessage());
        }
    }

    private void updateAuctionInDatabase(Auction auction) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "UPDATE auctions SET current_bid = ?, status = ?, start_date = ?, end_date = ?, winner_id = ? WHERE auction_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setBigDecimal(1, auction.getCurrentBid());
            stmt.setString(2, auction.getStatus().getValue());
            stmt.setTimestamp(3, auction.getStartDate());
            stmt.setTimestamp(4, auction.getEndDate());

            if (auction.getWinnerId() != null) {
                stmt.setInt(5, auction.getWinnerId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setInt(6, auction.getAuctionId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating auction in database: " + e.getMessage());
        }
    }

    public Auction getAuction(int auctionId) {
        return activeAuctions.get(auctionId);
    }

    public List<Bid> getAuctionBids(int auctionId) {
        return auctionBids.getOrDefault(auctionId, new ArrayList<>());
    }

    public List<Bid> getAuctionBidsFromDatabase(int auctionId) {
        List<Bid> bids = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "SELECT * FROM bids WHERE auction_id = ? ORDER BY bid_time DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, auctionId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Bid bid = new Bid();
                bid.setBidId(rs.getInt("bid_id"));
                bid.setAuctionId(rs.getInt("auction_id"));
                bid.setUserId(rs.getInt("user_id"));
                bid.setBidAmount(rs.getBigDecimal("bid_amount"));
                bid.setBidTime(rs.getTimestamp("bid_time"));
                bids.add(bid);
            }
        } catch (SQLException e) {
            System.err.println("Error loading bids from database: " + e.getMessage());
        }
        return bids;
    }

    public boolean isAuctionActive(int auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        return auction != null && auction.getStatus() == AuctionStatus.ACTIVE;
    }

    public BigDecimal getMinimumBid(int auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction != null) {
            return auction.getCurrentBid().add(MIN_BID_INCREMENT);
        }
        return BigDecimal.ZERO;
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,d", amount.longValue());
    }

    public AuctionTimer getAuctionTimer() {
        return auctionTimer;
    }

    public void cleanup() {
        if (auctionTimer != null) {
            auctionTimer.stopAllTimers();
        }
        if (socketController != null) {
            socketController.stop();
        }
    }

    public String getAuctionStats() {
        StringBuilder stats = new StringBuilder("Auction Service Statistics:\n");
        stats.append("Total Auctions: ").append(activeAuctions.size()).append("\n");
        stats.append("Active Auctions: ").append(getActiveAuctionCount()).append("\n");

        long upcomingCount = activeAuctions.values().stream()
                .filter(a -> a.getStatus() == AuctionStatus.UPCOMING)
                .count();
        stats.append("Upcoming Auctions: ").append(upcomingCount).append("\n");

        long endedCount = activeAuctions.values().stream()
                .filter(a -> a.getStatus() == AuctionStatus.ENDED)
                .count();
        stats.append("Ended Auctions: ").append(endedCount).append("\n");

        return stats.toString();
    }
}