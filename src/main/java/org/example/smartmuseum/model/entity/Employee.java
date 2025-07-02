package org.example.smartmuseum.model.entity;

import com.google.zxing.WriterException;
import org.example.smartmuseum.util.QRCodeGenerator;
import java.sql.Date;

public class Employee {
    private int employeeId;
    private int userId;
    private String name;
    private String position;
    private String qrCode;
    private Date hireDate;
    private int salary;
    private String photoPath; // FIELD BARU UNTUK FOTO

    // Constructors
    public Employee() {}

    public Employee(int employeeId, int userId, String name, String position, String qrCode) {
        this.employeeId = employeeId;
        this.userId = userId;
        this.name = name;
        this.position = position;
        this.qrCode = qrCode;
    }

    public Employee(int employeeId, int userId, String name, String position, String qrCode, Date hireDate, int salary) {
        this.employeeId = employeeId;
        this.userId = userId;
        this.name = name;
        this.position = position;
        this.qrCode = qrCode;
        this.hireDate = hireDate;
        this.salary = salary;
    }

    // Constructor dengan photo_path
    public Employee(int employeeId, int userId, String name, String position, String qrCode, Date hireDate, int salary, String photoPath) {
        this.employeeId = employeeId;
        this.userId = userId;
        this.name = name;
        this.position = position;
        this.qrCode = qrCode;
        this.hireDate = hireDate;
        this.salary = salary;
        this.photoPath = photoPath;
    }

    public void generateQRCode() throws WriterException {
        this.qrCode = QRCodeGenerator.generateEmployeeQRData(employeeId, "EMP" + employeeId + "_" + name);
    }

    // Getters and Setters
    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    // GETTER SETTER BARU UNTUK FOTO
    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}