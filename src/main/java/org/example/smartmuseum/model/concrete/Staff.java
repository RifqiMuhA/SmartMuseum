package org.example.smartmuseum.model.concrete;

import org.example.smartmuseum.model.abstracts.BaseUser;
import org.example.smartmuseum.model.enums.UserRole;

/**
 * Staff user class with limited system access
 */
public class Staff extends BaseUser {
    private String position;
    private String department;
    private double salary;
    private String supervisor;

    public Staff() {
        super();
        this.role = UserRole.STAFF;
    }

    public Staff(int userId, String username) {
        super(userId, username, UserRole.STAFF);
    }

    public Staff(int userId, String username, String position) {
        this(userId, username);
        this.position = position;
    }

    public Staff(int userId, String username, String position, String department) {
        this(userId, username, position);
        this.department = department;
    }

    @Override
    public String getDisplayName() {
        return position != null ? position + " " + username : "Staff " + username;
    }

    @Override
    public boolean hasPermission(String permission) {
        // Staff has limited permissions
        switch (permission.toLowerCase()) {
            case "view_artworks":
            case "manage_visitors":
            case "use_chatbot":
            case "view_gallery":
                return true;
            case "manage_auctions":
            case "view_reports":
            case "manage_staff":
                return false;
            default:
                return false;
        }
    }

    // Getters and Setters
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    /**
     * Staff-specific methods
     */
    public boolean canAssistVisitors() {
        return true;
    }

    public boolean canManageGallery() {
        return "Gallery Assistant".equals(position) || "Curator".equals(position);
    }

    @Override
    public void displayDashboard() {
        System.out.println("=== STAFF DASHBOARD ===");
        System.out.println("- Artwork Management");
        System.out.println("- Attendance System");
        System.out.println("- Visitor Assistance");
        System.out.println("- Auction Support");
    }

    @Override
    public java.util.List<String> getAvailableActions() {
        java.util.List<String> actions = new java.util.ArrayList<>();
        actions.add("Manage Artworks");
        actions.add("Check Attendance");
        actions.add("Assist Visitors");
        actions.add("Support Auctions");
        return actions;
    }

    @Override
    public boolean login(String username, String password) {
        System.out.println("Staff login: " + username);
        this.updateLastLogin();
        return true;
    }

    @Override
    public void logout() {
        System.out.println("Staff logged out: " + username);
    }

    @Override
    public void updateProfile(Object profile) {
        System.out.println("Staff profile updated");
    }

    @Override
    public String toString() {
        return "Staff{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", position='" + position + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
