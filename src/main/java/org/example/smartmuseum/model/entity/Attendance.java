package org.example.smartmuseum.model.entity;

import org.example.smartmuseum.model.enums.AttendanceStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

public class Attendance {
    private int attendanceId;
    private int employeeId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private double workHours;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Attendance() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Attendance(int employeeId, LocalDate attendanceDate) {
        this.employeeId = employeeId;
        this.attendanceDate = attendanceDate;
        this.status = AttendanceStatus.ABSENT; // Default to absent until check-in
        this.workHours = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business Methods
    public void checkIn() {
        this.checkIn = LocalDateTime.now();
        if (this.attendanceDate == null) {
            this.attendanceDate = LocalDate.now();
        }

        // Check if late (assuming work starts at 9 AM)
        if (checkIn.getHour() > 9 || (checkIn.getHour() == 9 && checkIn.getMinute() > 15)) {
            this.status = AttendanceStatus.LATE;
        } else {
            this.status = AttendanceStatus.PRESENT;
        }
        this.updatedAt = LocalDateTime.now();

        System.out.println("Employee " + employeeId + " checked in at: " + checkIn);
    }

    public void checkOut() {
        if (this.checkIn == null) {
            throw new IllegalStateException("Cannot check out without checking in first");
        }

        this.checkOut = LocalDateTime.now();
        calculateWorkHours();
        this.updatedAt = LocalDateTime.now();

        System.out.println("Employee " + employeeId + " checked out at: " + checkOut +
                " (Total hours: " + String.format("%.2f", workHours) + ")");
    }

    private void calculateWorkHours() {
        if (checkIn != null && checkOut != null) {
            Duration duration = Duration.between(checkIn, checkOut);
            this.workHours = duration.toMinutes() / 60.0;

            // Check overtime (more than 8 hours)
            if (workHours > 8.0) {
                this.status = AttendanceStatus.OVERTIME;
            }
        }
    }

    public boolean isCheckedIn() {
        return checkIn != null && checkOut == null;
    }

    public boolean isCheckedOut() {
        return checkIn != null && checkOut != null;
    }

    public boolean isComplete() {
        return checkIn != null && checkOut != null;
    }

    public double getOvertimeHours() {
        return workHours > 8.0 ? workHours - 8.0 : 0.0;
    }

    public String getStatusDescription() {
        if (isCheckedOut()) {
            return status.getDisplayName() + " (Complete)";
        } else if (isCheckedIn()) {
            return status.getDisplayName() + " (Working)";
        } else {
            return AttendanceStatus.ABSENT.getDisplayName();
        }
    }

    // Getters and Setters
    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public LocalDateTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }

    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public double getWorkHours() { return workHours; }
    public void setWorkHours(double workHours) { this.workHours = workHours; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("Attendance{id=%d, employeeId=%d, date=%s, status=%s, hours=%.2f}",
                attendanceId, employeeId, attendanceDate, status, workHours);
    }
}