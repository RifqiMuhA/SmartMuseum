package org.example.smartmuseum.model.entity;

import org.example.smartmuseum.model.interfaces.QRCodeEnabled;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Employee implements QRCodeEnabled {
    private int employeeId;
    private int userId;
    private String name;
    private String position;
    private String qrCode;
    private LocalDate hireDate;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Constructors
    public Employee() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public Employee(int employeeId, int userId, String name, String position, LocalDate hireDate) {
        this.employeeId = employeeId;
        this.userId = userId;
        this.name = name;
        this.position = position;
        this.hireDate = hireDate;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        generateQRCode(); // Auto-generate QR code
    }

    // Implementation of QRCodeEnabled interface
    @Override
    public String getQRCode() {
        return qrCode;
    }

    @Override
    public void generateQRCode() {
        if (employeeId > 0 && name != null) {
            this.qrCode = "SMARTMUSEUM_EMP_" + String.format("%03d", employeeId) +
                    "_" + name.replaceAll("\\s+", "_").toUpperCase() +
                    "_" + System.currentTimeMillis() / 1000;
        }
    }

    @Override
    public String getQRInfo() {
        return String.format("Employee: %s (ID: %d), Position: %s, QR: %s",
                name, employeeId, position, qrCode);
    }

    // Business methods
    public boolean isValidForAttendance() {
        return isActive && qrCode != null && !qrCode.isEmpty();
    }

    public void updateQRCode() {
        generateQRCode();
        System.out.println("QR Code updated for employee: " + name);
    }

    // Getters and Setters
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
        generateQRCode(); // Regenerate QR when ID changes
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        generateQRCode(); // Regenerate QR when name changes
    }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s', position='%s', active=%s}",
                employeeId, name, position, isActive);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Employee employee = (Employee) obj;
        return employeeId == employee.employeeId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(employeeId);
    }
}