package org.example.smartmuseum.model.service;

import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.entity.Attendance;
import org.example.smartmuseum.model.enums.AttendanceStatus;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.List;
import java.util.ArrayList;
import java.sql.Date;
import java.sql.Timestamp;

public class EmployeeService {
    private ConcurrentMap<Integer, Employee> employees;
    private ConcurrentMap<Integer, List<Attendance>> attendanceRecords;

    public EmployeeService() {
        this.employees = new ConcurrentHashMap<>();
        this.attendanceRecords = new ConcurrentHashMap<>();
    }

    public void processAttendance(int employeeId, String action) {
        Employee employee = employees.get(employeeId);
        if (employee == null) {
            System.out.println("Employee not found: " + employeeId);
            return;
        }

        Date today = new Date(System.currentTimeMillis());
        List<Attendance> records = attendanceRecords.computeIfAbsent(employeeId, k -> new ArrayList<>());

        // Find today's attendance record
        Attendance todayAttendance = records.stream()
                .filter(att -> att.getDate().equals(today))
                .findFirst()
                .orElse(null);

        if (todayAttendance == null) {
            todayAttendance = new Attendance();
            todayAttendance.setEmployeeId(employeeId);
            todayAttendance.setDate(today);
            records.add(todayAttendance);
        }

        switch (action.toLowerCase()) {
            case "checkin":
                todayAttendance.recordCheckIn();
                System.out.println("Employee " + employee.getName() + " checked in at " + todayAttendance.getCheckIn());
                break;
            case "checkout":
                todayAttendance.recordCheckOut();
                System.out.println("Employee " + employee.getName() + " checked out at " + todayAttendance.getCheckOut());
                break;
            default:
                System.out.println("Invalid attendance action: " + action);
        }
    }

    public Employee getEmployee(int employeeId) {
        return employees.get(employeeId);
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees.values());
    }

    // CHANGED: Now returns boolean for success/failure
    public boolean addEmployee(Employee employee) {
        try {
            if (employee == null) {
                System.out.println("Cannot add null employee");
                return false;
            }

            if (employees.containsKey(employee.getEmployeeId())) {
                System.out.println("Employee with ID " + employee.getEmployeeId() + " already exists");
                return false;
            }

            employees.put(employee.getEmployeeId(), employee);
            System.out.println("Employee added successfully: " + employee.getName());
            return true;

        } catch (Exception e) {
            System.err.println("Error adding employee: " + e.getMessage());
            return false;
        }
    }

    // ADDED: Update employee method with boolean return
    public boolean updateEmployee(Employee employee) {
        try {
            if (employee == null) {
                System.out.println("Cannot update null employee");
                return false;
            }

            if (!employees.containsKey(employee.getEmployeeId())) {
                System.out.println("Employee with ID " + employee.getEmployeeId() + " not found");
                return false;
            }

            employees.put(employee.getEmployeeId(), employee);
            System.out.println("Employee updated successfully: " + employee.getName());
            return true;

        } catch (Exception e) {
            System.err.println("Error updating employee: " + e.getMessage());
            return false;
        }
    }

    // ADDED: Delete employee method with boolean return
    public boolean deleteEmployee(int employeeId) {
        try {
            Employee removedEmployee = employees.remove(employeeId);
            if (removedEmployee != null) {
                // Also remove attendance records for this employee
                attendanceRecords.remove(employeeId);
                System.out.println("Employee deleted successfully: " + removedEmployee.getName());
                return true;
            } else {
                System.out.println("Employee with ID " + employeeId + " not found");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            return false;
        }
    }

    public List<Attendance> getEmployeeAttendance(int employeeId) {
        return attendanceRecords.getOrDefault(employeeId, new ArrayList<>());
    }

    public AttendanceStatus getEmployeeStatus(int employeeId) {
        Date today = new Date(System.currentTimeMillis());
        List<Attendance> records = attendanceRecords.get(employeeId);

        if (records != null) {
            Attendance todayRecord = records.stream()
                    .filter(att -> att.getDate().equals(today))
                    .findFirst()
                    .orElse(null);

            if (todayRecord != null) {
                return todayRecord.getStatus();
            }
        }

        return AttendanceStatus.ABSENT;
    }
}