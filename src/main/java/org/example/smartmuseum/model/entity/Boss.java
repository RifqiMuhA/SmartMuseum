package org.example.smartmuseum.model.entity;

import org.example.smartmuseum.model.enums.UserRole;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Boss extends BaseUser {
    private List<Employee> managedEmployees;

    public Boss() {
        super();
        this.role = UserRole.BOSS;
        this.managedEmployees = new ArrayList<>();
    }

    public Boss(int userId, String username, String email) {
        super(userId, username, email, UserRole.BOSS);
        this.managedEmployees = new ArrayList<>();
    }

    @Override
    public void displayDashboard() {
        System.out.println("=== BOSS DASHBOARD ===");
        System.out.println("Welcome, " + username);
        System.out.println("Total Managed Employees: " + managedEmployees.size());
        System.out.println("Available Actions: " + String.join(", ", getAvailableActions()));
    }

    @Override
    public List<String> getAvailableActions() {
        return Arrays.asList(
                "VIEW_REPORTS",
                "MANAGE_EMPLOYEES",
                "VIEW_ALL_AUCTIONS",
                "SYSTEM_SETTINGS",
                "MONITOR_ATTENDANCE",
                "GENERATE_QR_CODES"
        );
    }

    @Override
    protected void initializeSession() {
        System.out.println("Boss session initialized with full system access");
    }

    // Boss-specific methods
    public void manageEmployee(Employee employee, String action) {
        switch (action.toUpperCase()) {
            case "ADD":
                if (!managedEmployees.contains(employee)) {
                    managedEmployees.add(employee);
                    System.out.println("Employee added: " + employee.getName());
                }
                break;
            case "REMOVE":
                managedEmployees.remove(employee);
                System.out.println("Employee removed: " + employee.getName());
                break;
            case "UPDATE":
                System.out.println("Employee updated: " + employee.getName());
                break;
        }
    }

    public void viewAllAuctions() {
        System.out.println("Viewing all auctions (Boss access)");
    }

    public void monitorAttendance() {
        System.out.println("Monitoring attendance for all employees");
    }

    public void configureSystem(Object settings) {
        System.out.println("System configuration updated by: " + username);
    }

    @Override
    public void onAttendanceChanged(Object event) {
        System.out.println("Boss notified of attendance change: " + event);
    }

    // Getters and Setters
    public List<Employee> getManagedEmployees() { return managedEmployees; }
    public void setManagedEmployees(List<Employee> managedEmployees) {
        this.managedEmployees = managedEmployees;
    }
}