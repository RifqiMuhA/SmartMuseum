package org.example.smartmuseum.model.concrete;

import org.example.smartmuseum.model.abstracts.BaseUser;
import org.example.smartmuseum.model.entity.UserChatSession;
import org.example.smartmuseum.model.enums.UserRole;

/**
 * Visitor user class extending BaseUser
 */
public class Visitor extends BaseUser {
    private String name;
    private String phoneNumber;
    private boolean isRegistered;
    private UserChatSession chatSession;
    private String visitPurpose;

    public Visitor() {
        super();
        this.role = UserRole.VISITOR;
        this.isRegistered = false;
    }

    public Visitor(int userId, String username) {
        super(userId, username, UserRole.VISITOR);
        this.name = username;
        this.isRegistered = true;
    }

    public Visitor(int visitorId, String name, String email) {
        super(visitorId, name, UserRole.VISITOR);
        this.name = name;
        this.email = email;
        this.isRegistered = true;
    }

    public Visitor(String name, String email, String phoneNumber) {
        super();
        this.role = UserRole.VISITOR;
        this.name = name;
        this.username = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.isRegistered = false;
    }

    @Override
    public String getDisplayName() {
        return name != null ? name : "Visitor " + userId;
    }

    @Override
    public boolean hasPermission(String permission) {
        // Visitors have very limited permissions
        switch (permission.toLowerCase()) {
            case "view_artworks":
            case "use_chatbot":
            case "view_gallery":
            case "participate_auction":
                return true;
            case "manage_artworks":
            case "view_reports":
            case "manage_staff":
                return false;
            default:
                return false;
        }
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public UserChatSession getChatSession() {
        return chatSession;
    }

    public void setChatSession(UserChatSession chatSession) {
        this.chatSession = chatSession;
    }

    public String getVisitPurpose() {
        return visitPurpose;
    }

    public void setVisitPurpose(String visitPurpose) {
        this.visitPurpose = visitPurpose;
    }

    /**
     * Get visitor ID (alias for getUserId for backward compatibility)
     */
    public int getVisitorId() {
        return this.userId;
    }

    /**
     * Set visitor ID (alias for setUserId for backward compatibility)
     */
    public void setVisitorId(int visitorId) {
        this.userId = visitorId;
    }

    /**
     * Initialize chat session for this visitor
     */
    public void initializeChatSession() {
        this.chatSession = new UserChatSession(this.userId);
    }

    /**
     * End chat session
     */
    public void endChatSession() {
        if (this.chatSession != null) {
            this.chatSession.endSession();
        }
    }

    /**
     * Visitor-specific methods
     */
    public boolean canParticipateInAuction() {
        return isRegistered;
    }

    public boolean canAccessPremiumContent() {
        return isRegistered;
    }

    @Override
    public String toString() {
        return "Visitor{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", isRegistered=" + isRegistered +
                '}';
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
    public java.util.List<String> getAvailableActions() {
        java.util.List<String> actions = new java.util.ArrayList<>();
        actions.add("Browse Artworks");
        actions.add("Place Bids");
        actions.add("View Auction Details");
        actions.add("Use Chat Assistant");
        actions.add("View My Bids");
        return actions;
    }

    @Override
    public boolean login(String username, String password) {
        System.out.println("Visitor login: " + username);
        this.updateLastLogin();
        return true;
    }

    @Override
    public void logout() {
        System.out.println("Visitor logged out: " + username);
    }

    @Override
    public void updateProfile(Object profile) {
        System.out.println("Visitor profile updated");
    }
}
