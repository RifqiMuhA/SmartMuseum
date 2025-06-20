package org.example.smartmuseum.model.concrete;

import org.example.smartmuseum.model.abstracts.BaseUser;
import org.example.smartmuseum.model.interfaces.SystemObserver;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.entity.Artwork;
import org.example.smartmuseum.model.enums.UserRole;
import java.util.List;
import java.util.ArrayList;

/**
 * Staff user class for gallery operations
 */
public class Staff extends BaseUser implements SystemObserver {
    private String position;
    private Employee employeeDetails;

    public Staff(int userId, String username, String position) {
        super(userId, username, UserRole.STAFF);
        this.position = position;
    }

    @Override
    public boolean login(String username, String password) {
        System.out.println("Staff login: " + username);
        return true; // Placeholder
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
    public void displayDashboard() {
        System.out.println("=== STAFF DASHBOARD ===");
        System.out.println("- Artwork Management");
        System.out.println("- Attendance System");
        System.out.println("- Visitor Assistance");
        System.out.println("- Auction Support");
    }

    @Override
    public List<String> getAvailableActions() {
        List<String> actions = new ArrayList<>();
        actions.add("Manage Artworks");
        actions.add("Check Attendance");
        actions.add("Assist Visitors");
        actions.add("Support Auctions");
        return actions;
    }

    public void checkInAttendance(String qrCode) {
        System.out.println("Staff check-in with QR: " + qrCode);
    }

    public void checkOutAttendance() {
        System.out.println("Staff check-out: " + username);
    }

    public void manageArtwork(Artwork artwork, String action) {
        System.out.println("Managing artwork: " + artwork.getTitle() + " - Action: " + action);
    }

    @Override
    public void onAttendanceChanged(Object event) {
        System.out.println("Staff notified: Attendance changed - " + event);
    }

    @Override
    public void onBidPlaced(Object event) {
        // Staff may not need bid notifications, but implementing for interface compliance
    }

    // Getters and Setters
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Employee getEmployeeDetails() { return employeeDetails; }
    public void setEmployeeDetails(Employee employeeDetails) { this.employeeDetails = employeeDetails; }
}