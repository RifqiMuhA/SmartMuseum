package org.example.smartmuseum.model.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Bid {
    private int bidId;
    private int auctionId;
    private int userId;
    private BigDecimal bidAmount;
    private Timestamp bidTime;

    // Constructors
    public Bid() {}

    public Bid(int bidId, int auctionId, int userId, BigDecimal bidAmount, Timestamp bidTime) {
        this.bidId = bidId;
        this.auctionId = auctionId;
        this.userId = userId;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime != null ? bidTime : new Timestamp(System.currentTimeMillis());
    }

    public BigDecimal getAmount() { return bidAmount; }
    public Timestamp getTimestamp() { return bidTime; }

    // Getters and Setters
    public int getBidId() { return bidId; }
    public void setBidId(int bidId) { this.bidId = bidId; }

    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }

    public Timestamp getBidTime() { return bidTime; }
    public void setBidTime(Timestamp bidTime) { this.bidTime = bidTime; }
}
