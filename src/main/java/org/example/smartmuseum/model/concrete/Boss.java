package org.example.smartmuseum.model.concrete;

import org.example.smartmuseum.model.abstracts.BaseUser;
import org.example.smartmuseum.model.enums.UserRole;

/**
 * Boss user class with full system privileges
 */
public class Boss extends BaseUser {
    private String department;
    private String title;

    public Boss() {
        super();
        this.role = UserRole.BOSS;
    }

    public Boss(int userId, String username) {
        super(userId, username, UserRole.BOSS);
        this.title = "Museum Director";
    }

    public Boss(int userId, String username, String department) {
        this(userId, username);
        this.department = department;
    }

    @Override
    public String getDisplayName() {
        return title != null ? title + " " + username : "Boss " + username;
    }

    @Override
    public boolean hasPermission(String permission) {
        // Boss has all permissions
        return true;
    }

    // Getters and Setters
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Boss-specific methods
     */
    public boolean canManageStaff() {
        return true;
    }

    public boolean canAccessReports() {
        return true;
    }

    public boolean canModifySystem() {
        return true;
    }

    @Override
    public void displayDashboard() {
        System.out.println("=== BOSS DASHBOARD ===");
        System.out.println("- Employee Management");
        System.out.println("- Auction Oversight");
        System.out.println("- System Analytics");
        System.out.println("- Attendance Monitoring");
    }

    @Override
    public java.util.List<String> getAvailableActions() {
        java.util.List<String> actions = new java.util.ArrayList<>();
        actions.add("Manage Employees");
        actions.add("View All Auctions");
        actions.add("Monitor Attendance");
        actions.add("System Analytics");
        actions.add("User Management");
        return actions;
    }

    @Override
    public boolean login(String username, String password) {
        System.out.println("Boss login: " + username);
        this.updateLastLogin();
        return true;
    }

    @Override
    public void logout() {
        System.out.println("Boss logged out: " + username);
    }

    @Override
    public void updateProfile(Object profile) {
        System.out.println("Boss profile updated");
    }

    @Override
    public String toString() {
        return "Boss{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", title='" + title + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
