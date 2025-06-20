package org.example.smartmuseum.model.concrete;

import org.example.smartmuseum.model.abstracts.BaseUser;
import org.example.smartmuseum.model.interfaces.SystemObserver;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.entity.Auction;
import org.example.smartmuseum.model.enums.UserRole;
import java.util.List;
import java.util.ArrayList;

/**
 * Boss user class with full system access
 */
public class Boss extends BaseUser implements SystemObserver {
    private List<Employee> managedEmployees;

    public Boss(int userId, String username) {
        super(userId, username, UserRole.BOSS);
        this.managedEmployees = new ArrayList<>();
    }

    @Override
    public boolean login(String username, String password) {
        // Implement authentication logic
        System.out.println("Boss login: " + username);
        return true; // Placeholder
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
    public void displayDashboard() {
        System.out.println("=== BOSS DASHBOARD ===");
        System.out.println("- Employee Management");
        System.out.println("- Auction Oversight");
        System.out.println("- System Analytics");
        System.out.println("- Attendance Monitoring");
    }

    @Override
    public List<String> getAvailableActions() {
        List<String> actions = new ArrayList<>();
        actions.add("Manage Employees");
        actions.add("View All Auctions");
        actions.add("Monitor Attendance");
        actions.add("System Analytics");
        actions.add("User Management");
        return actions;
    }

    public void manageEmployee(Employee employee, String action) {
        System.out.println("Managing employee: " + employee.getName() + " - Action: " + action);
    }

    public List<Auction> viewAllAuctions() {
        System.out.println("Viewing all auctions");
        return new ArrayList<>(); // Placeholder
    }

    public void monitorAttendance() {
        System.out.println("Monitoring attendance for all employees");
    }

    @Override
    public void onBidPlaced(Object event) {
        System.out.println("Boss notified: New bid placed - " + event);
    }

    @Override
    public void onAttendanceChanged(Object event) {
        System.out.println("Boss notified: Attendance changed - " + event);
    }

    // Getters and Setters
    public List<Employee> getManagedEmployees() { return managedEmployees; }
    public void setManagedEmployees(List<Employee> managedEmployees) { this.managedEmployees = managedEmployees; }
}