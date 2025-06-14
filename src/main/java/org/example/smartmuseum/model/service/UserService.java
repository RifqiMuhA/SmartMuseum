package org.example.smartmuseum.model.service;

import org.example.smartmuseum.model.entity.*;
import org.example.smartmuseum.model.enums.UserRole;
import org.example.smartmuseum.database.DatabaseConnection;

import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

public class UserService {
    private static UserService instance;
    private final ConcurrentHashMap<Integer, BaseUser> activeUsers;
    private final ConcurrentHashMap<Integer, Employee> employees;
    private final DatabaseConnection dbConnection;

    private UserService() {
        this.activeUsers = new ConcurrentHashMap<>();
        this.employees = new ConcurrentHashMap<>();
        try {
            this.dbConnection = DatabaseConnection.getInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize UserService", e);
        }
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    // User Authentication
    public BaseUser authenticate(String username, String password) {
        try {
            String sql = "SELECT u.*, e.employee_id, e.name as emp_name, e.position " +
                    "FROM users u LEFT JOIN employees e ON u.user_id = e.user_id " +
                    "WHERE u.username = ? AND u.password_hash = ?";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, hashPassword(password)); // In real app, use proper hashing

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    BaseUser user = createUserFromResultSet(rs);
                    activeUsers.put(user.getUserId(), user);
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(BaseUser user, String password) {
        try {
            String sql = "INSERT INTO users (username, password_hash, role, email, phone) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, hashPassword(password));
                stmt.setString(3, user.getRole().getRoleName());
                stmt.setString(4, user.getEmail());
                stmt.setString(5, user.getPhone());

                int result = stmt.executeUpdate();
                if (result > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void logout(int userId) {
        BaseUser user = activeUsers.remove(userId);
        if (user != null) {
            user.logout();
        }
    }

    // Employee Management
    public Employee addEmployee(Employee employee) {
        try {
            String sql = "INSERT INTO employees (user_id, name, position, qr_code, hire_date) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, employee.getUserId());
                stmt.setString(2, employee.getName());
                stmt.setString(3, employee.getPosition());
                stmt.setString(4, employee.getQRCode());
                stmt.setDate(5, Date.valueOf(employee.getHireDate()));

                int result = stmt.executeUpdate();
                if (result > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        employee.setEmployeeId(generatedKeys.getInt(1));
                        employees.put(employee.getEmployeeId(), employee);
                        return employee;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Employee getEmployee(int employeeId) {
        // Check cache first
        if (employees.containsKey(employeeId)) {
            return employees.get(employeeId);
        }

        // Load from database
        try {
            String sql = "SELECT * FROM employees WHERE employee_id = ? AND is_active = true";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
                stmt.setInt(1, employeeId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Employee employee = createEmployeeFromResultSet(rs);
                    employees.put(employeeId, employee);
                    return employee;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employeeList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM employees WHERE is_active = true ORDER BY name";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Employee employee = createEmployeeFromResultSet(rs);
                    employeeList.add(employee);
                    employees.put(employee.getEmployeeId(), employee);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeeList;
    }

    public Employee findEmployeeByQRCode(String qrCode) {
        try {
            String sql = "SELECT * FROM employees WHERE qr_code = ? AND is_active = true";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
                stmt.setString(1, qrCode);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Employee employee = createEmployeeFromResultSet(rs);
                    employees.put(employee.getEmployeeId(), employee);
                    return employee;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper methods
    private BaseUser createUserFromResultSet(ResultSet rs) throws SQLException {
        UserRole role = UserRole.valueOf(rs.getString("role").toUpperCase());
        BaseUser user;

        switch (role) {
            case BOSS:
                user = new Boss();
                break;
            case STAFF:
                user = new Staff();
                break;
            case VISITOR:
                user = new Visitor();
                break;
            default:
                throw new IllegalArgumentException("Unknown user role: " + role);
        }

        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(role);

        return user;
    }

    private Employee createEmployeeFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeId(rs.getInt("employee_id"));
        employee.setUserId(rs.getInt("user_id"));
        employee.setName(rs.getString("name"));
        employee.setPosition(rs.getString("position"));
        employee.setQrCode(rs.getString("qr_code"));

        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            employee.setHireDate(hireDate.toLocalDate());
        }

        employee.setActive(rs.getBoolean("is_active"));
        return employee;
    }

    private String hashPassword(String password) {
        // In a real application, use BCrypt or similar
        return "hashed_" + password; // Simplified for demo
    }

    // Getters for active collections
    public ConcurrentHashMap<Integer, BaseUser> getActiveUsers() {
        return activeUsers;
    }

    public ConcurrentHashMap<Integer, Employee> getEmployees() {
        return employees;
    }
}