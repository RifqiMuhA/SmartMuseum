package org.example.smartmuseum.database;

import org.example.smartmuseum.model.entity.Attendance;
import org.example.smartmuseum.model.enums.AttendanceStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public boolean insertAttendance(Attendance attendance) {
        String sql = "INSERT INTO attendance (employee_id, check_in, date, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, attendance.getEmployeeId());
            pstmt.setTimestamp(2, attendance.getCheckIn());
            pstmt.setDate(3, attendance.getDate());
            pstmt.setString(4, attendance.getStatus().getValue());

            int result = pstmt.executeUpdate();
            System.out.println("Attendance insert result: " + result);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting attendance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAttendance(Attendance attendance) {
        String sql = "UPDATE attendance SET check_out = ?, status = ? WHERE employee_id = ? AND date = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, attendance.getCheckOut());
            pstmt.setString(2, attendance.getStatus().getValue());
            pstmt.setInt(3, attendance.getEmployeeId());
            pstmt.setDate(4, attendance.getDate());

            int result = pstmt.executeUpdate();
            System.out.println("Attendance update result: " + result);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Error updating attendance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Attendance getTodayAttendance(int employeeId) {
        String sql = "SELECT * FROM attendance WHERE employee_id = ? AND date = CURDATE()";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setAttendanceId(rs.getInt("attendance_id"));
                attendance.setEmployeeId(rs.getInt("employee_id"));
                attendance.setCheckIn(rs.getTimestamp("check_in"));
                attendance.setCheckOut(rs.getTimestamp("check_out"));
                attendance.setDate(rs.getDate("date"));
                attendance.setStatus(AttendanceStatus.fromValue(rs.getString("status")));

                System.out.println("Found today's attendance for employee " + employeeId);
                return attendance;
            }

        } catch (SQLException e) {
            System.err.println("Error getting today's attendance: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("No attendance found for employee " + employeeId + " today");
        return null;
    }

    public List<Attendance> getAllTodayAttendance() {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE date = CURDATE() ORDER BY check_in DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setAttendanceId(rs.getInt("attendance_id"));
                attendance.setEmployeeId(rs.getInt("employee_id"));
                attendance.setCheckIn(rs.getTimestamp("check_in"));
                attendance.setCheckOut(rs.getTimestamp("check_out"));
                attendance.setDate(rs.getDate("date"));
                attendance.setStatus(AttendanceStatus.fromValue(rs.getString("status")));

                attendanceList.add(attendance);
                System.out.println("Loaded attendance record for employee ID: " + attendance.getEmployeeId());
            }

            System.out.println("Total today's attendance records loaded: " + attendanceList.size());

        } catch (SQLException e) {
            System.err.println("Error getting all today's attendance: " + e.getMessage());
            e.printStackTrace();
        }

        return attendanceList;
    }

    public Attendance getAttendanceByDate(int employeeId, LocalDate date) {
        String sql = "SELECT * FROM attendance WHERE employee_id = ? AND date = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            pstmt.setDate(2, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setAttendanceId(rs.getInt("attendance_id"));
                attendance.setEmployeeId(rs.getInt("employee_id"));
                attendance.setCheckIn(rs.getTimestamp("check_in"));
                attendance.setCheckOut(rs.getTimestamp("check_out"));
                attendance.setDate(rs.getDate("date"));
                attendance.setStatus(AttendanceStatus.fromValue(rs.getString("status")));

                return attendance;
            }

        } catch (SQLException e) {
            System.err.println("Error getting attendance by date: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<Attendance> getEmployeeAttendance(int employeeId, int days) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE employee_id = ? AND date >= DATE_SUB(CURDATE(), INTERVAL ? DAY) ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            pstmt.setInt(2, days);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setAttendanceId(rs.getInt("attendance_id"));
                attendance.setEmployeeId(rs.getInt("employee_id"));
                attendance.setCheckIn(rs.getTimestamp("check_in"));
                attendance.setCheckOut(rs.getTimestamp("check_out"));
                attendance.setDate(rs.getDate("date"));
                attendance.setStatus(AttendanceStatus.fromValue(rs.getString("status")));

                attendanceList.add(attendance);
            }

        } catch (SQLException e) {
            System.err.println("Error getting employee attendance: " + e.getMessage());
            e.printStackTrace();
        }

        return attendanceList;
    }
}
