package org.example.smartmuseum.model.service;

import org.example.smartmuseum.database.DatabaseConnection;
import org.example.smartmuseum.model.entity.Auction;
import org.example.smartmuseum.model.entity.Bid;
import org.example.smartmuseum.model.enums.AuctionStatus;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.sql.*;

public class AuctionService {
    private ConcurrentMap<Integer, Auction> activeAuctions;
    private ConcurrentMap<Integer, List<Bid>> auctionBids;

    public AuctionService() {
        this.activeAuctions = new ConcurrentHashMap<>();
        this.auctionBids = new ConcurrentHashMap<>();
        loadAuctionsFromDatabase();
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
                        // If status doesn't match any enum, default to UPCOMING
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
        // Don't reload every time, just return current data
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

    // Existing methods remain the same...
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

        BigDecimal minimumBid = auction.getCurrentBid().add(new BigDecimal("10.00"));
        if (bid.getBidAmount().compareTo(minimumBid) < 0) {
            System.out.println("Bid amount too low. Minimum: $" + minimumBid);
            return false;
        }

        boolean success = auction.placeBid(bid);
        if (success) {
            List<Bid> bids = auctionBids.computeIfAbsent(auctionId, k -> new ArrayList<>());
            bids.add(bid);
            System.out.println("Bid placed successfully: $" + bid.getBidAmount() + " on auction " + auctionId);
        }

        return success;
    }

    public Auction createAuction(int artworkId, String artworkTitle, BigDecimal startingBid) {
        Auction auction = new Auction();
        auction.setAuctionId(generateAuctionId());
        auction.setArtworkId(artworkId);
        auction.setStartingBid(startingBid);
        auction.setCurrentBid(startingBid);
        auction.setStatus(AuctionStatus.UPCOMING);
        auction.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        activeAuctions.put(auction.getAuctionId(), auction);
        System.out.println("Created auction for artwork: " + artworkTitle);

        return auction;
    }

    public Auction getAuction(int auctionId) {
        return activeAuctions.get(auctionId);
    }

    public List<Bid> getAuctionBids(int auctionId) {
        return auctionBids.getOrDefault(auctionId, new ArrayList<>());
    }

    public void startAuction(int auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction != null) {
            auction.setStatus(AuctionStatus.ACTIVE);
            auction.setStartDate(new Timestamp(System.currentTimeMillis()));
            System.out.println("Started auction: " + auctionId);
        }
    }

    public void endAuction(int auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction != null) {
            auction.setStatus(AuctionStatus.ENDED);
            auction.setEndDate(new Timestamp(System.currentTimeMillis()));

            List<Bid> bids = getAuctionBids(auctionId);
            if (!bids.isEmpty()) {
                Bid winningBid = bids.stream()
                        .max((b1, b2) -> b1.getBidAmount().compareTo(b2.getBidAmount()))
                        .orElse(null);

                if (winningBid != null) {
                    auction.setWinnerId(winningBid.getUserId());
                    System.out.println("Auction " + auctionId + " ended. Winner: User " + winningBid.getUserId());
                }
            }
        }
    }

    private int generateAuctionId() {
        return activeAuctions.size() + 1;
    }
}