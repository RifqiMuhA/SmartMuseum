package org.example.smartmuseum.model.service;

import org.example.smartmuseum.database.EmployeeDAO;
import org.example.smartmuseum.database.AttendanceDAO;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.entity.Attendance;
import org.example.smartmuseum.model.enums.AttendanceStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Business logic: Validate employee data before adding
     */
    public boolean addEmployee(Employee employee) {
        try {
            if (employee == null) {
                System.out.println("Cannot add null employee");
                return false;
            }

            // BUSINESS LOGIC: Validasi nama tidak boleh kosong
            if (employee.getName() == null || employee.getName().trim().isEmpty()) {
                System.out.println("Employee name cannot be empty");
                return false;
            }

            // BUSINESS LOGIC: Validasi posisi tidak boleh kosong
            if (employee.getPosition() == null || employee.getPosition().trim().isEmpty()) {
                System.out.println("Employee position cannot be empty");
                return false;
            }

            // BUSINESS LOGIC: Validasi gaji minimal
            if (employee.getSalary() <= 0) {
                System.out.println("Employee salary must be positive");
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
     * Business logic: Validate employee data before updating
     */
    public boolean updateEmployee(Employee employee) {
        try {
            if (employee == null) {
                System.out.println("Cannot update null employee");
                return false;
            }

            // BUSINESS LOGIC: Validasi employee ID harus valid
            if (employee.getEmployeeId() <= 0) {
                System.out.println("Invalid employee ID");
                return false;
            }

            // BUSINESS LOGIC: Cek apakah employee exists
            Employee existingEmployee = employeeDAO.getEmployeeById(employee.getEmployeeId());
            if (existingEmployee == null) {
                System.out.println("Employee not found for update: " + employee.getEmployeeId());
                return false;
            }

            // BUSINESS LOGIC: Validasi data
            if (employee.getName() == null || employee.getName().trim().isEmpty()) {
                System.out.println("Employee name cannot be empty");
                return false;
            }

            if (employee.getPosition() == null || employee.getPosition().trim().isEmpty()) {
                System.out.println("Employee position cannot be empty");
                return false;
            }

            if (employee.getSalary() <= 0) {
                System.out.println("Employee salary must be positive");
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
     * Business logic: Check if employee exists before deleting
     */
    public boolean deleteEmployee(int employeeId) {
        try {
            if (employeeId <= 0) {
                System.out.println("Invalid employee ID");
                return false;
            }

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

    // ========== PHOTO MANAGEMENT METHODS ==========

    /**
     * Update employee photo
     * Business logic: Validate photo path and employee existence
     */
    public boolean updateEmployeePhoto(int employeeId, String photoPath) {
        try {
            if (employeeId <= 0) {
                System.out.println("Invalid employee ID");
                return false;
            }

            // BUSINESS LOGIC: Cek apakah employee exists
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            if (employee == null) {
                System.out.println("Employee not found for photo update: " + employeeId);
                return false;
            }

            // BUSINESS LOGIC: Validasi path foto
            if (photoPath != null && !photoPath.trim().isEmpty()) {
                // Validate photo format
                String lowerPath = photoPath.toLowerCase();
                if (!lowerPath.endsWith(".jpg") && !lowerPath.endsWith(".jpeg") &&
                        !lowerPath.endsWith(".png") && !lowerPath.endsWith(".gif") &&
                        !lowerPath.endsWith(".bmp")) {
                    System.out.println("Invalid photo format. Only JPG, JPEG, PNG, GIF, BMP are allowed.");
                    return false;
                }
            }

            boolean updated = employeeDAO.updateEmployeePhoto(employeeId, photoPath);
            if (updated) {
                System.out.println("Employee photo updated successfully for: " + employee.getName());
                return true;
            } else {
                System.out.println("Failed to update employee photo in database");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error updating employee photo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate photo file path for business rules
     */
    public boolean isValidPhotoPath(String photoPath) {
        if (photoPath == null || photoPath.trim().isEmpty()) {
            return true; // Empty is allowed (no photo)
        }

        // BUSINESS LOGIC: Check file extension
        String lowerPath = photoPath.toLowerCase();
        return lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg") ||
                lowerPath.endsWith(".png") || lowerPath.endsWith(".gif") ||
                lowerPath.endsWith(".bmp");
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
        System.out.println("Employee QR Code: " + employee.getQrCode());
        System.out.println("Scanned QR Code: " + qrCode);

        if (employee.getQrCode() == null || !employee.getQrCode().equals(qrCode)) {
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

    public int getTodayAttendanceCount() {
        try {
            List<Attendance> todayAttendance = attendanceDAO.getAllTodayAttendance();
            return todayAttendance.size();
        } catch (Exception e) {
            System.err.println("Error getting today attendance count: " + e.getMessage());
            return 0;
        }
    }

    public Map<LocalDate, Integer> getMonthlyAttendanceData(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Integer> attendanceData = new HashMap<>();

        try {
            List<Employee> employees = employeeDAO.getAllStaffEmployees();

            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                int count = 0;

                for (Employee employee : employees) {
                    List<Attendance> employeeAttendance = attendanceDAO.getEmployeeAttendance(employee.getEmployeeId(), 30);

                    final LocalDate checkDate = currentDate;
                    boolean attended = employeeAttendance.stream()
                            .anyMatch(attendance -> attendance.getDate().toLocalDate().equals(checkDate));

                    if (attended) {
                        count++;
                    }
                }

                attendanceData.put(currentDate, count);
                currentDate = currentDate.plusDays(1);
            }

        } catch (Exception e) {
            System.err.println("Error getting monthly attendance data: " + e.getMessage());
        }

        return attendanceData;
    }

    public List<Attendance> getRecentCheckIns(int limit) {
        try {
            List<Attendance> allTodayAttendance = attendanceDAO.getAllTodayAttendance();

            // Sort by check-in time (newest first)
            allTodayAttendance.sort((a, b) -> {
                if (a.getCheckIn() == null) return 1;
                if (b.getCheckIn() == null) return -1;
                return b.getCheckIn().compareTo(a.getCheckIn());
            });

            // Return limited results
            return allTodayAttendance.stream()
                    .filter(attendance -> attendance.getCheckIn() != null)
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting recent check-ins: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Employee> getRecentEmployees(int limit) {
        try {
            List<Employee> allEmployees = employeeDAO.getAllStaffEmployees();

            // If employees have created_at, sort by it
            // For now, return first few employees
            return allEmployees.stream()
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting recent employees: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}