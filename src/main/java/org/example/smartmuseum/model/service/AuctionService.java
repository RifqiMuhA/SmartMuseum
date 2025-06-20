package org.example.smartmuseum.model.service;

import org.example.smartmuseum.model.entity.Auction;
import org.example.smartmuseum.model.entity.Bid;
import org.example.smartmuseum.model.entity.Artwork;
import org.example.smartmuseum.model.enums.AuctionStatus;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class AuctionService {
    private ConcurrentMap<Integer, Auction> activeAuctions;
    private ConcurrentMap<Integer, List<Bid>> auctionBids;

    public AuctionService() {
        this.activeAuctions = new ConcurrentHashMap<>();
        this.auctionBids = new ConcurrentHashMap<>();
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

        // Validate bid amount
        BigDecimal minimumBid = auction.getCurrentBid().add(new BigDecimal("10.00")); // Minimum increment
        if (bid.getBidAmount().compareTo(minimumBid) < 0) {
            System.out.println("Bid amount too low. Minimum: $" + minimumBid);
            return false;
        }

        // Process bid
        boolean success = auction.placeBid(bid);
        if (success) {
            // Store bid record
            List<Bid> bids = auctionBids.computeIfAbsent(auctionId, k -> new ArrayList<>());
            bids.add(bid);

            System.out.println("Bid placed successfully: $" + bid.getBidAmount() + " on auction " + auctionId);
        }

        return success;
    }

    public List<Auction> getActiveAuctions() {
        return activeAuctions.values().stream()
                .filter(auction -> auction.getStatus() == AuctionStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    public Auction createAuction(Artwork artwork) {
        Auction auction = new Auction();
        auction.setAuctionId(generateAuctionId());
        auction.setArtworkId(artwork.getArtworkId());
        auction.setStartingBid(new BigDecimal("100.00")); // Default starting bid
        auction.setCurrentBid(new BigDecimal("100.00"));
        auction.setStatus(AuctionStatus.UPCOMING);
        auction.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        activeAuctions.put(auction.getAuctionId(), auction);
        System.out.println("Created auction for artwork: " + artwork.getTitle());

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

            // Determine winner
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
        // Simple ID generation - in real app, would use database auto-increment
        return activeAuctions.size() + 1;
    }
}
