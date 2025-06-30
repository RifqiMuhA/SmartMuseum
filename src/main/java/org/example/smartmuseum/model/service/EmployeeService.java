package org.example.smartmuseum.model.service;

import org.example.smartmuseum.database.EmployeeDAO;
import org.example.smartmuseum.database.AttendanceDAO;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.entity.Attendance;
import org.example.smartmuseum.model.enums.AttendanceStatus;
import java.util.List;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class EmployeeService {
    private EmployeeDAO employeeDAO;
    private AttendanceDAO attendanceDAO;
    private static final long MIN_CHECKOUT_DELAY_MINUTES = 1; // 1 minute minimum

    public EmployeeService() {
        this.employeeDAO = new EmployeeDAO();
        this.attendanceDAO = new AttendanceDAO();
    }

    // ========== EMPLOYEE CRUD METHODS ==========

    /**
     * Add new employee to database
     */
    public boolean addEmployee(Employee employee) {
        try {
            if (employee == null) {
                System.out.println("Cannot add null employee");
                return false;
            }

            boolean added = employeeDAO.addEmployee(employee);
            if (added) {
                System.out.println("Employee added successfully: " + employee.getName());
                return true;
            } else {
                System.out.println("Failed to add employee to database");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error adding employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update existing employee in database
     */
    public boolean updateEmployee(Employee employee) {
        try {
            if (employee == null) {
                System.out.println("Cannot update null employee");
                return false;
            }

            boolean updated = employeeDAO.updateEmployee(employee);
            if (updated) {
                System.out.println("Employee updated successfully: " + employee.getName());
                return true;
            } else {
                System.out.println("Failed to update employee in database");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error updating employee: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete employee from database
     */
    public boolean deleteEmployee(int employeeId) {
        try {
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            if (employee == null) {
                System.out.println("Employee with ID " + employeeId + " not found");
                return false;
            }

            boolean deleted = employeeDAO.deleteEmployee(employeeId);
            if (deleted) {
                System.out.println("Employee deleted successfully: " + employee.getName());
                return true;
            } else {
                System.out.println("Failed to delete employee from database");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            return false;
        }
    }

    // ========== ATTENDANCE PROCESSING METHODS ==========

    /**
     * Process attendance for today only
     */
    public AttendanceResult processAttendance(int employeeId, String qrCode) {
        LocalDate today = LocalDate.now();

        System.out.println("=== PROCESSING ATTENDANCE ===");
        System.out.println("Employee ID: " + employeeId);
        System.out.println("QR Code: " + qrCode);
        System.out.println("Date: " + today + " (Today)");

        // Get employee from database using DAO
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        System.out.println("Employee found: " + (employee != null ? employee.getName() : "NULL"));

        if (employee == null) {
            System.out.println("Employee not found in database for ID: " + employeeId);
            return new AttendanceResult(false, "Employee not found: " + employeeId, null);
        }

        // Verify QR code belongs to this employee
        System.out.println("Employee QR Code: " + employee.getQRCode());
        System.out.println("Scanned QR Code: " + qrCode);

        if (employee.getQRCode() == null || !employee.getQRCode().equals(qrCode)) {
            System.out.println("QR code mismatch!");
            return new AttendanceResult(false, "Invalid QR code for employee: " + employee.getName(), null);
        }

        // Get today's attendance record from database
        Attendance todayAttendance = attendanceDAO.getTodayAttendance(employeeId);
        System.out.println("Existing attendance for today: " + (todayAttendance != null ? "Found" : "Not found"));

        if (todayAttendance == null) {
            // First scan of the day - CHECK IN
            System.out.println("Creating new attendance record - CHECK IN");
            todayAttendance = new Attendance();
            todayAttendance.setEmployeeId(employeeId);
            todayAttendance.setDate(Date.valueOf(today));
            todayAttendance.recordCheckIn();

            boolean inserted = attendanceDAO.insertAttendance(todayAttendance);
            if (!inserted) {
                System.out.println("Failed to insert attendance record");
                return new AttendanceResult(false, "Failed to record check-in in database", null);
            }

            String message = "✅ " + employee.getName() + " checked in at " +
                    todayAttendance.getCheckIn().toLocalDateTime().format(
                            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

            System.out.println("Check-in successful: " + message);
            return new AttendanceResult(true, message, "CHECK_IN");

        } else if (todayAttendance.getCheckIn() != null && todayAttendance.getCheckOut() == null) {
            // Already checked in, try to check out
            System.out.println("Attempting CHECK OUT");
            LocalDateTime checkInTime = todayAttendance.getCheckIn().toLocalDateTime();
            LocalDateTime now = LocalDateTime.now();

            long minutesSinceCheckIn = ChronoUnit.MINUTES.between(checkInTime, now);
            System.out.println("Minutes since check-in: " + minutesSinceCheckIn);

            if (minutesSinceCheckIn < MIN_CHECKOUT_DELAY_MINUTES) {
                long remainingSeconds = (MIN_CHECKOUT_DELAY_MINUTES * 60) - ChronoUnit.SECONDS.between(checkInTime, now);
                String message = "⏱️ Please wait " + remainingSeconds + " more seconds before checking out";
                System.out.println(message);
                return new AttendanceResult(false, message, null);
            }

            // CHECK OUT
            todayAttendance.recordCheckOut();

            boolean updated = attendanceDAO.updateAttendance(todayAttendance);
            if (!updated) {
                System.out.println("Failed to update attendance record");
                return new AttendanceResult(false, "Failed to record check-out in database", null);
            }

            String message = "✅ " + employee.getName() + " checked out at " +
                    todayAttendance.getCheckOut().toLocalDateTime().format(
                            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

            System.out.println("Check-out successful: " + message);
            return new AttendanceResult(true, message, "CHECK_OUT");

        } else {
            // Already completed check-in and check-out for today
            String message = "❌ " + employee.getName() + " has already completed attendance for today";
            System.out.println(message);
            return new AttendanceResult(false, message, null);
        }
    }

    // ========== GETTER METHODS ==========

    public Employee getEmployee(int employeeId) {
        return employeeDAO.getEmployeeById(employeeId);
    }

    public Employee getEmployeeByQRCode(String qrCode) {
        return employeeDAO.getEmployeeByQRCode(qrCode);
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllStaffEmployees();
    }

    public List<Attendance> getEmployeeAttendance(int employeeId) {
        return attendanceDAO.getEmployeeAttendance(employeeId, 30); // Last 30 days
    }

    // Get all attendance for today
    public List<Attendance> getAllTodayAttendance() {
        return attendanceDAO.getAllTodayAttendance();
    }

    public AttendanceStatus getEmployeeStatus(int employeeId) {
        Attendance todayRecord = attendanceDAO.getTodayAttendance(employeeId);

        if (todayRecord != null) {
            return todayRecord.getStatus();
        }

        return AttendanceStatus.ABSENT;
    }

    // Result class for attendance processing
    public static class AttendanceResult {
        private final boolean success;
        private final String message;
        private final String action;

        public AttendanceResult(boolean success, String message, String action) {
            this.success = success;
            this.message = message;
            this.action = action;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getAction() { return action; }
    }
}
