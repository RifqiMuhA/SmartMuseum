package org.example.smartmuseum.model.entity;

import org.example.smartmuseum.model.enums.AttendanceStatus;
import java.sql.Date;
import java.sql.Timestamp;

public class Attendance {
    private int attendanceId;
    private int employeeId;
    private Timestamp checkIn;
    private Timestamp checkOut;
    private Date date;
    private AttendanceStatus status;

    // Constructors
    public Attendance() {}

    public Attendance(int attendanceId, int employeeId, Timestamp checkIn, Timestamp checkOut, Date date, AttendanceStatus status) {
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.date = date;
        this.status = status;
    }

    public void recordCheckIn() {
        this.checkIn = new Timestamp(System.currentTimeMillis());
        this.status = AttendanceStatus.PRESENT;
    }

    public void recordCheckOut() {
        this.checkOut = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public Timestamp getCheckIn() { return checkIn; }
    public void setCheckIn(Timestamp checkIn) { this.checkIn = checkIn; }

    public Timestamp getCheckOut() { return checkOut; }
    public void setCheckOut(Timestamp checkOut) { this.checkOut = checkOut; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
}