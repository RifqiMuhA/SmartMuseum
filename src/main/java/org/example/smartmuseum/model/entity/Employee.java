package org.example.smartmuseum.model.entity;

import org.example.smartmuseum.util.QRCodeGenerator;

public class Employee {
    private int employeeId;
    private int userId;
    private String name;
    private String position;
    private String qrCode;

    // Constructors
    public Employee() {}

    public Employee(int employeeId, int userId, String name, String position, String qrCode) {
        this.employeeId = employeeId;
        this.userId = userId;
        this.name = name;
        this.position = position;
        this.qrCode = qrCode;
    }

    public void generateQRCode() {
        this.qrCode = QRCodeGenerator.generateQRCode("EMP" + employeeId + "_" + name);
    }

    public String getQRCode() { return qrCode; }
    public String getName() { return name; }

    // Getters and Setters
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public void setName(String name) { this.name = name; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}