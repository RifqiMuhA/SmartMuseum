package org.example.smartmuseum.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Message class for socket communication in auction system
 * Handles real-time bid updates, timer updates, and auction events
 */
public class BidMessage {

    public enum MessageType {
        BID_PLACED,
        TIMER_UPDATE,
        AUCTION_STARTED,
        AUCTION_ENDED,
        USER_JOINED,
        USER_LEFT,
        WINNER_ANNOUNCED,
        ERROR,
        HEARTBEAT
    }

    private MessageType type;
    private int auctionId;
    private int userId;
    private String username;
    private String sessionId;
    private BigDecimal bidAmount;
    private int remainingTime;
    private String message;
    private LocalDateTime timestamp;
    private BigDecimal currentHighestBid;
    private String currentWinner;
    private boolean isSuccess;

    // Constructors
    public BidMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public BidMessage(MessageType type, int auctionId) {
        this();
        this.type = type;
        this.auctionId = auctionId;
    }

    public BidMessage(MessageType type, int auctionId, int userId, String username, String sessionId) {
        this(type, auctionId);
        this.userId = userId;
        this.username = username;
        this.sessionId = sessionId;
    }

    // Factory methods for common message types
    public static BidMessage createBidPlaced(int auctionId, int userId, String username,
                                             String sessionId, BigDecimal bidAmount) {
        BidMessage message = new BidMessage(MessageType.BID_PLACED, auctionId, userId, username, sessionId);
        message.setBidAmount(bidAmount);
        message.setSuccess(true);
        message.setMessage(username + " placed bid: " + bidAmount);
        return message;
    }

    public static BidMessage createTimerUpdate(int auctionId, int remainingTime) {
        BidMessage message = new BidMessage(MessageType.TIMER_UPDATE, auctionId);
        message.setRemainingTime(remainingTime);
        return message;
    }

    public static BidMessage createAuctionStarted(int auctionId, BigDecimal startingBid) {
        BidMessage message = new BidMessage(MessageType.AUCTION_STARTED, auctionId);
        message.setCurrentHighestBid(startingBid);
        message.setMessage("Auction " + auctionId + " started with starting bid: " + startingBid);
        message.setSuccess(true);
        return message;
    }

    public static BidMessage createAuctionEnded(int auctionId, String winner, BigDecimal winningBid) {
        BidMessage message = new BidMessage(MessageType.AUCTION_ENDED, auctionId);
        message.setCurrentWinner(winner);
        message.setCurrentHighestBid(winningBid);
        message.setMessage("Auction " + auctionId + " ended. Winner: " + winner + " with bid: " + winningBid);
        message.setSuccess(true);
        return message;
    }

    public static BidMessage createUserJoined(int auctionId, int userId, String username, String sessionId) {
        BidMessage message = new BidMessage(MessageType.USER_JOINED, auctionId, userId, username, sessionId);
        message.setMessage(username + " joined the auction");
        message.setSuccess(true);
        return message;
    }

    public static BidMessage createUserLeft(int auctionId, int userId, String username, String sessionId) {
        BidMessage message = new BidMessage(MessageType.USER_LEFT, auctionId, userId, username, sessionId);
        message.setMessage(username + " left the auction");
        message.setSuccess(true);
        return message;
    }

    public static BidMessage createError(int auctionId, String errorMessage) {
        BidMessage message = new BidMessage(MessageType.ERROR, auctionId);
        message.setMessage(errorMessage);
        message.setSuccess(false);
        return message;
    }

    public static BidMessage createHeartbeat() {
        return new BidMessage(MessageType.HEARTBEAT, -1);
    }

    // Utility methods
    public String toSocketMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.name()).append("|");
        sb.append(auctionId).append("|");
        sb.append(userId).append("|");
        sb.append(username != null ? username : "").append("|");
        sb.append(sessionId != null ? sessionId : "").append("|");
        sb.append(bidAmount != null ? bidAmount.toString() : "").append("|");
        sb.append(remainingTime).append("|");
        sb.append(message != null ? message : "").append("|");
        sb.append(currentHighestBid != null ? currentHighestBid.toString() : "").append("|");
        sb.append(currentWinner != null ? currentWinner : "").append("|");
        sb.append(isSuccess).append("|");
        sb.append(timestamp.toString());
        return sb.toString();
    }

    public static BidMessage fromSocketMessage(String socketMessage) {
        try {
            String[] parts = socketMessage.split("\\|");
            if (parts.length < 12) {
                return createError(-1, "Invalid message format");
            }

            BidMessage message = new BidMessage();
            message.setType(MessageType.valueOf(parts[0]));
            message.setAuctionId(Integer.parseInt(parts[1]));
            message.setUserId(Integer.parseInt(parts[2]));
            message.setUsername(parts[3].isEmpty() ? null : parts[3]);
            message.setSessionId(parts[4].isEmpty() ? null : parts[4]);
            message.setBidAmount(parts[5].isEmpty() ? null : new BigDecimal(parts[5]));
            message.setRemainingTime(Integer.parseInt(parts[6]));
            message.setMessage(parts[7].isEmpty() ? null : parts[7]);
            message.setCurrentHighestBid(parts[8].isEmpty() ? null : new BigDecimal(parts[8]));
            message.setCurrentWinner(parts[9].isEmpty() ? null : parts[9]);
            message.setSuccess(Boolean.parseBoolean(parts[10]));
            message.setTimestamp(LocalDateTime.parse(parts[11]));

            return message;
        } catch (Exception e) {
            return createError(-1, "Error parsing message: " + e.getMessage());
        }
    }

    // Getters and Setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }

    public int getRemainingTime() { return remainingTime; }
    public void setRemainingTime(int remainingTime) { this.remainingTime = remainingTime; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public BigDecimal getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(BigDecimal currentHighestBid) { this.currentHighestBid = currentHighestBid; }

    public String getCurrentWinner() { return currentWinner; }
    public void setCurrentWinner(String currentWinner) { this.currentWinner = currentWinner; }

    public boolean isSuccess() { return isSuccess; }
    public void setSuccess(boolean success) { isSuccess = success; }

    @Override
    public String toString() {
        return String.format("BidMessage{type=%s, auctionId=%d, user=%s, amount=%s, time=%d, message='%s'}",
                type, auctionId, username, bidAmount, remainingTime, message);
    }
}