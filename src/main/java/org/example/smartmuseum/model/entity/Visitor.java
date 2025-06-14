package org.example.smartmuseum.model.entity;

import org.example.smartmuseum.model.enums.UserRole;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Visitor extends BaseUser {
    private List<Object> participatedAuctions;
    private List<Object> photoSessions;
    private Object preferences;

    public Visitor() {
        super();
        this.role = UserRole.VISITOR;
        this.participatedAuctions = new ArrayList<>();
        this.photoSessions = new ArrayList<>();
    }

    public Visitor(int userId, String username, String email) {
        super(userId, username, email, UserRole.VISITOR);
        this.participatedAuctions = new ArrayList<>();
        this.photoSessions = new ArrayList<>();
    }

    @Override
    public void displayDashboard() {
        System.out.println("=== VISITOR DASHBOARD ===");
        System.out.println("Welcome, " + username);
        System.out.println("Available Actions: " + String.join(", ", getAvailableActions()));
    }

    @Override
    public List<String> getAvailableActions() {
        return Arrays.asList(
                "BROWSE_ARTWORKS",
                "JOIN_AUCTIONS",
                "USE_PHOTOBOOTH",
                "SCAN_QR_CODES"
        );
    }

    @Override
    protected void initializeSession() {
        System.out.println("Visitor session initialized with limited access");
    }

    // Visitor-specific methods
    public void browseArtworks(Object filter) {
        System.out.println("Visitor " + username + " browsing artworks");
    }

    public void participateInAuction(Object auction) {
        participatedAuctions.add(auction);
        System.out.println("Visitor " + username + " joined auction");
    }

    public Object placeBid(Object auction, double amount) {
        System.out.println("Visitor " + username + " placed bid: $" + amount);
        return new Object(); // Bid object
    }

    public Object startPhotoSession(Object template) {
        Object session = new Object(); // PhotoSession object
        photoSessions.add(session);
        System.out.println("Visitor " + username + " started photo session");
        return session;
    }

    public Object scanArtworkQR(String qrCode) {
        System.out.println("Visitor " + username + " scanned QR: " + qrCode);
        return new Object(); // ArtworkInfo object
    }

    @Override
    public void onMessageReceived(Object event) {
        System.out.println("Visitor " + username + " received message");
    }

    // Getters and Setters
    public List<Object> getParticipatedAuctions() { return participatedAuctions; }
    public void setParticipatedAuctions(List<Object> participatedAuctions) {
        this.participatedAuctions = participatedAuctions;
    }

    public List<Object> getPhotoSessions() { return photoSessions; }
    public void setPhotoSessions(List<Object> photoSessions) {
        this.photoSessions = photoSessions;
    }

    public Object getPreferences() { return preferences; }
    public void setPreferences(Object preferences) { this.preferences = preferences; }
}