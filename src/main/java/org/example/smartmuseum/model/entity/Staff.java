package org.example.smartmuseum.model.entity;

import org.example.smartmuseum.model.enums.UserRole;
import java.util.Arrays;
import java.util.List;

public class Staff extends BaseUser {
    private String position;
    private Employee employeeDetails;
    private List<String> assignedDuties;

    public Staff() {
        super();
        this.role = UserRole.STAFF;
    }

    public Staff(int userId, String username, String email, String position) {
        super(userId, username, email, UserRole.STAFF);
        this.position = position;
    }

    @Override
    public void displayDashboard() {
        System.out.println("=== STAFF DASHBOARD ===");
        System.out.println("Welcome, " + username);
        System.out.println("Position: " + position);
        System.out.println("Available Actions: " + String.join(", ", getAvailableActions()));
    }

    @Override
    public List<String> getAvailableActions() {
        return Arrays.asList(
                "MANAGE_ARTWORKS",
                "ASSIST_VISITORS",
                "CHECK_ATTENDANCE",
                "VIEW_SCHEDULE",
                "SCAN_QR_CODES"
        );
    }

    @Override
    protected void initializeSession() {
        System.out.println("Staff session initialized with operational access");
    }

    // Staff-specific methods
    public void checkInAttendance(String qrCode) {
        System.out.println("Staff " + username + " checking in with QR: " + qrCode);
    }

    public void checkOutAttendance() {
        System.out.println("Staff " + username + " checking out");
    }

    public void assistVisitor(Visitor visitor, String assistanceType) {
        System.out.println("Staff " + username + " assisting visitor with: " + assistanceType);
    }

    public void manageArtwork(Object artwork, String action) {
        System.out.println("Staff " + username + " performing " + action + " on artwork");
    }

    public void updateArtworkInfo(Object artwork) {
        System.out.println("Staff " + username + " updating artwork information");
    }

    @Override
    public void onAttendanceChanged(Object event) {
        System.out.println("Staff notified of attendance change: " + event);
    }

    // Getters and Setters
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Employee getEmployeeDetails() { return employeeDetails; }
    public void setEmployeeDetails(Employee employeeDetails) { this.employeeDetails = employeeDetails; }

    public List<String> getAssignedDuties() { return assignedDuties; }
    public void setAssignedDuties(List<String> assignedDuties) { this.assignedDuties = assignedDuties; }
}