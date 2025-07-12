package org.example.smartmuseum.model.interfaces;

/**
 * Observer interface for auction system events
 * Used to notify observers about bid placements and auction changes
 */
public interface SystemObserver {

    /**
     * Called when a bid is placed in an auction
     * @param event The bid event details
     */
    void onBidPlaced(Object event);

    /**
     * Called when an auction starts
     * @param auctionId The ID of the auction that started
     */
    default void onAuctionStarted(int auctionId) {
        // Default empty implementation
    }

    /**
     * Called when an auction ends
     * @param auctionId The ID of the auction that ended
     * @param winnerId The ID of the winning bidder (null if no bids)
     */
    default void onAuctionEnded(int auctionId, Integer winnerId) {
        // Default empty implementation
    }

    /**
     * Called when auction timer updates
     * @param auctionId The auction ID
     * @param remainingSeconds Seconds remaining
     */
    default void onTimerUpdate(int auctionId, int remainingSeconds) {
        // Default empty implementation
    }

    /**
     * Called when a user joins an auction
     * @param auctionId The auction ID
     * @param userId The user who joined
     */
    default void onUserJoined(int auctionId, int userId) {
        // Default empty implementation
    }

    /**
     * Called when a user leaves an auction
     * @param auctionId The auction ID
     * @param userId The user who left
     */
    default void onUserLeft(int auctionId, int userId) {
        // Default empty implementation
    }
}