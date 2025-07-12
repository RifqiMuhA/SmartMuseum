package org.example.smartmuseum.database;

import org.example.smartmuseum.model.entity.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    public List<Employee> getAllStaffEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = """
            SELECT e.employee_id, e.user_id, e.name, e.position, e.qr_code, e.hire_date, e.salary, e.photo_path
            FROM employees e 
            JOIN users u ON e.user_id = u.user_id 
            WHERE u.role = 'staff'
            ORDER BY e.name
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Employee employee = new Employee(
                        rs.getInt("employee_id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("position"),
                        rs.getString("qr_code"),
                        rs.getDate("hire_date"),
                        rs.getInt("salary"),
                        rs.getString("photo_path") // KOLOM BARU
                );
                employees.add(employee);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching staff employees: " + e.getMessage());
            e.printStackTrace();
        }
        return employees;
    }

    public Employee getEmployeeById(int employeeId) {
        String sql = """
            SELECT e.employee_id, e.user_id, e.name, e.position, e.qr_code, e.hire_date, e.salary, e.photo_path
            FROM employees e 
            JOIN users u ON e.user_id = u.user_id 
            WHERE e.employee_id = ? AND u.role = 'staff'
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Employee(
                        rs.getInt("employee_id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("position"),
                        rs.getString("qr_code"),
                        rs.getDate("hire_date"),
                        rs.getInt("salary"),
                        rs.getString("photo_path") // KOLOM BARU
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching employee by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateEmployeeQRCode(int employeeId, String qrCode) {
        String sql = "UPDATE employees SET qr_code = ? WHERE employee_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, qrCode);
            stmt.setInt(2, employeeId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating employee QR code: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int getTotalEmployee() {
        String sql = """
        SELECT COUNT(*) as total 
        FROM employees e 
        JOIN users u ON e.user_id = u.user_id 
        WHERE u.role = 'staff'
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error getting total employees: " + e.getMessage());
            e.printStackTrace();
        }

        return 0; // Return 0 if error or no data
    }

    public boolean updateEmployeePhoto(int employeeId, String photoPath) {
        String sql = "UPDATE employees SET photo_path = ? WHERE employee_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, photoPath);
            stmt.setInt(2, employeeId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating employee photo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Employee getEmployeeByQRCode(String qrCode) {
        String sql = """
            SELECT e.employee_id, e.user_id, e.name, e.position, e.qr_code, e.hire_date, e.salary, e.photo_path
            FROM employees e 
            JOIN users u ON e.user_id = u.user_id 
            WHERE e.qr_code = ? AND u.role = 'staff'
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, qrCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Employee(
                        rs.getInt("employee_id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("position"),
                        rs.getString("qr_code"),
                        rs.getDate("hire_date"),
                        rs.getInt("salary"),
                        rs.getString("photo_path") // KOLOM BARU
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching employee by QR code: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean addEmployee(Employee employee) {
        String sql = "INSERT INTO employees (user_id, name, position, qr_code, hire_date, salary, photo_path) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, employee.getUserId());
            stmt.setString(2, employee.getName());
            stmt.setString(3, employee.getPosition());
            stmt.setString(4, employee.getQrCode());
            stmt.setDate(5, employee.getHireDate());
            stmt.setInt(6, employee.getSalary());
            stmt.setString(7, employee.getPhotoPath()); // KOLOM BARU

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated employee_id
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    employee.setEmployeeId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error adding employee: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteEmployee(int employeeId) {
        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET name = ?, position = ?, qr_code = ?, hire_date = ?, salary = ?, photo_path = ? WHERE employee_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getName());
            stmt.setString(2, employee.getPosition());
            stmt.setString(3, employee.getQrCode());
            stmt.setDate(4, employee.getHireDate());
            stmt.setInt(5, employee.getSalary());
            stmt.setString(6, employee.getPhotoPath()); // KOLOM BARU
            stmt.setInt(7, employee.getEmployeeId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}