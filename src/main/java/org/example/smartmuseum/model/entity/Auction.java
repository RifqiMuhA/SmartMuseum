package org.example.smartmuseum.model.entity;

import org.example.smartmuseum.model.interfaces.SystemObserver;
import org.example.smartmuseum.model.enums.AuctionStatus;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

public class Auction {
    private int auctionId;
    private int artworkId;
    private Timestamp startDate;
    private Timestamp endDate;
    private BigDecimal startingBid;
    private BigDecimal currentBid;
    private AuctionStatus status;
    private Integer winnerId;
    private Timestamp createdAt;
    private List<SystemObserver> observers;

    // Constructors
    public Auction() {
        this.observers = new ArrayList<>();
    }

    public Auction(int auctionId, int artworkId, Timestamp startDate, Timestamp endDate, BigDecimal startingBid, BigDecimal currentBid, AuctionStatus status, Integer winnerId, Timestamp createdAt) {
        this.auctionId = auctionId;
        this.artworkId = artworkId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startingBid = startingBid;
        this.currentBid = currentBid;
        this.status = status;
        this.winnerId = winnerId;
        this.createdAt = createdAt;
        this.observers = new ArrayList<>();
    }

    public boolean placeBid(Bid bid) {
        if (bid.getBidAmount().compareTo(this.currentBid) > 0) {
            this.currentBid = bid.getBidAmount();
            notifyObservers("Bid placed: $" + bid.getBidAmount());
            return true;
        }
        return false;
    }

    public void addObserver(SystemObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void notifyObservers(Object event) {
        for (SystemObserver observer : observers) {
            observer.onBidPlaced(event);
        }
    }

    public BigDecimal getCurrentBid() { return currentBid; }

    // Getters and Setters
    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }

    public int getArtworkId() { return artworkId; }
    public void setArtworkId(int artworkId) { this.artworkId = artworkId; }

    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }

    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }

    public BigDecimal getStartingBid() { return startingBid; }
    public void setStartingBid(BigDecimal startingBid) { this.startingBid = startingBid; }

    public void setCurrentBid(BigDecimal currentBid) { this.currentBid = currentBid; }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }

    public Integer getWinnerId() { return winnerId; }
    public void setWinnerId(Integer winnerId) { this.winnerId = winnerId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<SystemObserver> getObservers() { return observers; }
    public void setObservers(List<SystemObserver> observers) { this.observers = observers; }
}
