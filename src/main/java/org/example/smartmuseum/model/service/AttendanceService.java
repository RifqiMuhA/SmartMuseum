package org.example.smartmuseum.model.service;

import org.example.smartmuseum.model.entity.*;
import org.example.smartmuseum.model.enums.AttendanceStatus;
import org.example.smartmuseum.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;

public class AttendanceService {
    private static AttendanceService instance;
    private final ConcurrentHashMap<Integer, Attendance> todayAttendance;
    private final BlockingQueue<QRCodeLog> scanQueue;
    private final ExecutorService scanProcessorPool;
    private final DatabaseConnection dbConnection;
    private final UserService userService;

    private AttendanceService() {
        this.todayAttendance = new ConcurrentHashMap<>();
        this.scanQueue = new LinkedBlockingQueue<>();
        this.scanProcessorPool = Executors.newFixedThreadPool(3);
        this.userService = UserService.getInstance();

        try {
            this.dbConnection = DatabaseConnection.getInstance();
            startScanProcessor();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AttendanceService", e);
        }
    }

    public static synchronized AttendanceService getInstance() {
        if (instance == null) {
            instance = new AttendanceService();
        }
        return instance;
    }

    // QR Code Scanning
    public AttendanceResult scanQRCode(String qrCode, QRCodeLog.ScanType scanType) {
        try {
            // Find employee by QR code
            Employee employee = userService.findEmployeeByQRCode(qrCode);
            if (employee == null) {
                logQRScan(0, qrCode, scanType, QRCodeLog.ScanStatus.FAILED);
                return new AttendanceResult(false, "QR Code tidak ditemukan", null);
            }

            // Get or create today's attendance
            LocalDate today = LocalDate.now();
            Attendance attendance = getTodayAttendance(employee.getEmployeeId(), today);

            if (attendance == null) {
                attendance = new Attendance(employee.getEmployeeId(), today);
            }

            // Process scan based on type
            String message;
            QRCodeLog.ScanStatus logStatus = QRCodeLog.ScanStatus.SUCCESS;

            if (scanType == QRCodeLog.ScanType.CHECK_IN) {
                if (attendance.isCheckedIn()) {
                    logStatus = QRCodeLog.ScanStatus.DUPLICATE;
                    return new AttendanceResult(false, employee.getName() + " sudah check-in hari ini", attendance);
                }
                attendance.checkIn();
                message = employee.getName() + " berhasil check-in";
            } else {
                if (!attendance.isCheckedIn()) {
                    logStatus = QRCodeLog.ScanStatus.FAILED;
                    return new AttendanceResult(false, employee.getName() + " belum check-in", attendance);
                }
                if (attendance.isCheckedOut()) {
                    logStatus = QRCodeLog.ScanStatus.DUPLICATE;
                    return new AttendanceResult(false, employee.getName() + " sudah check-out hari ini", attendance);
                }
                attendance.checkOut();
                message = employee.getName() + " berhasil check-out (Jam kerja: " +
                        String.format("%.2f", attendance.getWorkHours()) + " jam)";
            }

            // Save attendance
            saveAttendance(attendance);
            todayAttendance.put(employee.getEmployeeId(), attendance);

            // Log scan
            logQRScan(employee.getEmployeeId(), qrCode, scanType, logStatus);

            return new AttendanceResult(true, message, attendance, employee);

        } catch (Exception e) {
            e.printStackTrace();
            return new AttendanceResult(false, "System error: " + e.getMessage(), null);
        }
    }

    // Attendance Management
    public Attendance getTodayAttendance(int employeeId, LocalDate date) {
        // Check cache first
        if (date.equals(LocalDate.now()) && todayAttendance.containsKey(employeeId)) {
            return todayAttendance.get(employeeId);
        }

        // Load from database
        try {
            String sql = "SELECT * FROM attendance WHERE employee_id = ? AND attendance_date = ?";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
                stmt.setInt(1, employeeId);
                stmt.setDate(2, Date.valueOf(date));
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Attendance attendance = createAttendanceFromResultSet(rs);
                    if (date.equals(LocalDate.now())) {
                        todayAttendance.put(employeeId, attendance);
                    }
                    return attendance;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveAttendance(Attendance attendance) throws SQLException {
        if (attendance.getAttendanceId() == 0) {
            insertAttendance(attendance);
        } else {
            updateAttendance(attendance);
        }
    }

    private void insertAttendance(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO attendance (employee_id, check_in, check_out, attendance_date, " +
                "status, work_hours, notes) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE check_in = VALUES(check_in), check_out = VALUES(check_out), " +
                "status = VALUES(status), work_hours = VALUES(work_hours), notes = VALUES(notes)";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, attendance.getEmployeeId());
            stmt.setTimestamp(2, attendance.getCheckIn() != null ?
                    Timestamp.valueOf(attendance.getCheckIn()) : null);
            stmt.setTimestamp(3, attendance.getCheckOut() != null ?
                    Timestamp.valueOf(attendance.getCheckOut()) : null);
            stmt.setDate(4, Date.valueOf(attendance.getAttendanceDate()));
            stmt.setString(5, attendance.getStatus().name().toLowerCase());
            stmt.setDouble(6, attendance.getWorkHours());
            stmt.setString(7, attendance.getNotes());

            stmt.executeUpdate();

            if (attendance.getAttendanceId() == 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    attendance.setAttendanceId(generatedKeys.getInt(1));
                }
            }
        }
    }

    private void updateAttendance(Attendance attendance) throws SQLException {
        String sql = "UPDATE attendance SET check_in = ?, check_out = ?, status = ?, " +
                "work_hours = ?, notes = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE attendance_id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setTimestamp(1, attendance.getCheckIn() != null ?
                    Timestamp.valueOf(attendance.getCheckIn()) : null);
            stmt.setTimestamp(2, attendance.getCheckOut() != null ?
                    Timestamp.valueOf(attendance.getCheckOut()) : null);
            stmt.setString(3, attendance.getStatus().name().toLowerCase());
            stmt.setDouble(4, attendance.getWorkHours());
            stmt.setString(5, attendance.getNotes());
            stmt.setInt(6, attendance.getAttendanceId());

            stmt.executeUpdate();
        }
    }

    public List<Attendance> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = new ArrayList<>();
        try {
            String sql = "SELECT a.*, e.name as employee_name FROM attendance a " +
                    "JOIN employees e ON a.employee_id = e.employee_id " +
                    "WHERE a.attendance_date BETWEEN ? AND ? " +
                    "ORDER BY a.attendance_date DESC, e.name";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(startDate));
                stmt.setDate(2, Date.valueOf(endDate));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Attendance attendance = createAttendanceFromResultSet(rs);
                    attendances.add(attendance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendances;
    }

    // QR Code Logging
    private void logQRScan(int employeeId, String qrCode, QRCodeLog.ScanType scanType,
                           QRCodeLog.ScanStatus status) {
        QRCodeLog log = new QRCodeLog(employeeId, qrCode, scanType);
        log.setStatus(status);
        scanQueue.offer(log);
    }

    private void startScanProcessor() {
        scanProcessorPool.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    QRCodeLog log = scanQueue.take();
                    saveScanLog(log);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveScanLog(QRCodeLog log) {
        try {
            String sql = "INSERT INTO qr_code_logs (employee_id, qr_code, scan_type, status) " +
                    "VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
                stmt.setInt(1, log.getEmployeeId());
                stmt.setString(2, log.getQrCode());
                stmt.setString(3, log.getScanType().name().toLowerCase());
                stmt.setString(4, log.getStatus().name().toLowerCase());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper methods
    private Attendance createAttendanceFromResultSet(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setEmployeeId(rs.getInt("employee_id"));

        Timestamp checkIn = rs.getTimestamp("check_in");
        if (checkIn != null) {
            attendance.setCheckIn(checkIn.toLocalDateTime());
        }

        Timestamp checkOut = rs.getTimestamp("check_out");
        if (checkOut != null) {
            attendance.setCheckOut(checkOut.toLocalDateTime());
        }

        Date attendanceDate = rs.getDate("attendance_date");
        if (attendanceDate != null) {
            attendance.setAttendanceDate(attendanceDate.toLocalDate());
        }

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            attendance.setStatus(AttendanceStatus.valueOf(statusStr.toUpperCase()));
        }

        attendance.setWorkHours(rs.getDouble("work_hours"));
        attendance.setNotes(rs.getString("notes"));

        return attendance;
    }

    // Shutdown method
    public void shutdown() {
        scanProcessorPool.shutdown();
    }

    // Result class for scan operations
    public static class AttendanceResult {
        private final boolean success;
        private final String message;
        private final Attendance attendance;
        private final Employee employee;

        public AttendanceResult(boolean success, String message, Attendance attendance) {
            this(success, message, attendance, null);
        }

        public AttendanceResult(boolean success, String message, Attendance attendance, Employee employee) {
            this.success = success;
            this.message = message;
            this.attendance = attendance;
            this.employee = employee;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Attendance getAttendance() { return attendance; }
        public Employee getEmployee() { return employee; }
    }
}