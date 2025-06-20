package org.example.smartmuseum.model.concrete;

import org.example.smartmuseum.model.abstracts.BaseUser;
import org.example.smartmuseum.model.interfaces.SystemObserver;
import org.example.smartmuseum.model.entity.Auction;
import org.example.smartmuseum.model.entity.Bid;
import org.example.smartmuseum.model.entity.UserChatSession;
import org.example.smartmuseum.model.enums.UserRole;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

/**
 * Visitor user class for gallery browsing and auction participation
 */
public class Visitor extends BaseUser implements SystemObserver {
    private List<Auction> participatedAuctions;

    public Visitor(int userId, String username) {
        super(userId, username, UserRole.VISITOR);
        this.participatedAuctions = new ArrayList<>();
    }

    @Override
    public boolean login(String username, String password) {
        System.out.println("Visitor login: " + username);
        return true; // Placeholder
    }

    @Override
    public void logout() {
        System.out.println("Visitor logged out: " + username);
    }

    @Override
    public void updateProfile(Object profile) {
        System.out.println("Visitor profile updated");
    }

    @Override
    public void displayDashboard() {
        System.out.println("=== VISITOR DASHBOARD ===");
        System.out.println("- Browse Artworks");
        System.out.println("- Participate in Auctions");
        System.out.println("- Chat Assistant");
        System.out.println("- My Bids");
    }

    @Override
    public List<String> getAvailableActions() {
        List<String> actions = new ArrayList<>();
        actions.add("Browse Artworks");
        actions.add("Place Bids");
        actions.add("View Auction Details");
        actions.add("Use Chat Assistant");
        actions.add("View My Bids");
        return actions;
    }

    public void browseArtworks() {
        System.out.println("Browsing available artworks");
    }

    public Bid placeBid(Auction auction, BigDecimal amount) {
        System.out.println("Placing bid of $" + amount + " on auction ID: " + auction.getAuctionId());
        // Create and return bid object
        return new Bid(0, auction.getAuctionId(), this.userId, amount, null);
    }

    public String viewArtworkDetails(int artworkId) {
        System.out.println("Viewing details for artwork ID: " + artworkId);
        return "Artwork details for ID: " + artworkId;
    }

    public UserChatSession startChatSession() {
        System.out.println("Starting chat session for visitor: " + username);
        return new UserChatSession(0, this.userId, 1, "", null, true);
    }

    public String sendChatInput(String input) {
        System.out.println("Chat input from " + username + ": " + input);
        return "Bot response to: " + input;
    }

    @Override
    public void onBidPlaced(Object event) {
        System.out.println("Visitor notified: New bid placed - " + event);
    }

    @Override
    public void onAttendanceChanged(Object event) {
        // Visitors may not need attendance notifications
    }

    // Getters and Setters
    public List<Auction> getParticipatedAuctions() { return participatedAuctions; }
    public void setParticipatedAuctions(List<Auction> participatedAuctions) { this.participatedAuctions = participatedAuctions; }
}