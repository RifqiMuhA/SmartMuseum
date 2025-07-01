package org.example.smartmuseum.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.enums.UserRole;
import org.example.smartmuseum.model.service.EmployeeService;
import org.example.smartmuseum.model.service.UserService;
import org.example.smartmuseum.util.SecurityUtils;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class EmployeeManagementController implements Initializable {

    @FXML private Label lblTotalEmployees;
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbPosition;
    @FXML private DatePicker dateHireDate;
    @FXML private TextField txtSalary;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private Label lblStatus;
    @FXML private TableView<EmployeeRecord> tableEmployees;
    @FXML private TableColumn<EmployeeRecord, Integer> colEmployeeId;
    @FXML private TableColumn<EmployeeRecord, String> colName;
    @FXML private TableColumn<EmployeeRecord, String> colPosition;
    @FXML private TableColumn<EmployeeRecord, String> colQRCode;
    @FXML private TableColumn<EmployeeRecord, String> colHireDate;
    @FXML private TableColumn<EmployeeRecord, Integer> colSalary;

    private EmployeeService employeeService;
    private UserService userService;
    private ObservableList<EmployeeRecord> employeeData;
    private EmployeeRecord selectedEmployee;

    // Minimum salary for each position
    private Map<String, Integer> minimumSalaries;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        employeeService = new EmployeeService();
        userService = new UserService();
        employeeData = FXCollections.observableArrayList();

        setupMinimumSalaries();
        setupPositionComboBox();
        setupTableColumns();
        setupTableSelection();
        setupSalaryValidation();
        loadEmployees();
        updateStatistics();

        // Initially disable update and delete buttons
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void setupMinimumSalaries() {
        minimumSalaries = new HashMap<>();
        minimumSalaries.put("Curator", 8000000);
        minimumSalaries.put("Guide", 7000000);
        minimumSalaries.put("Technician", 5000000);
        minimumSalaries.put("Security", 6500000);
    }

    private void setupPositionComboBox() {
        ObservableList<String> positions = FXCollections.observableArrayList(
                "Curator", "Guide", "Technician", "Security"
        );
        cmbPosition.setItems(positions);

        // Add listener to update salary field when position changes
        cmbPosition.setOnAction(e -> {
            String selectedPosition = cmbPosition.getValue();
            if (selectedPosition != null && minimumSalaries.containsKey(selectedPosition)) {
                int minSalary = minimumSalaries.get(selectedPosition);
                txtSalary.setPromptText("Minimum: Rp " + String.format("%,d", minSalary));
            }
        });
    }

    private void setupTableColumns() {
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colQRCode.setCellValueFactory(new PropertyValueFactory<>("qrCode"));
        colHireDate.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));

        tableEmployees.setItems(employeeData);
    }

    private void setupTableSelection() {
        tableEmployees.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedEmployee = newSelection;
            if (newSelection != null) {
                populateForm(newSelection);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
            } else {
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);
            }
        });
    }

    private void setupSalaryValidation() {
        txtSalary.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtSalary.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            return;
        }

        try {
            String name = txtName.getText().trim();
            String position = cmbPosition.getValue();
            int salary = Integer.parseInt(txtSalary.getText().trim());
            Date hireDate = Date.valueOf(dateHireDate.getValue());

            // First, create user account
            User newUser = new User();
            newUser.setUsername(generateUsername(name));
            newUser.setPasswordHash(SecurityUtils.simpleHash("password")); // Default password
            newUser.setRole(UserRole.STAFF);
            newUser.setEmail(generateEmail(name));
            newUser.setPhone("080000000000"); // Default phone

            // Add user to database
            int userId = userService.addUser(newUser);
            if (userId <= 0) {
                showError("Failed to create user account for employee");
                showErrorAlert("Add Employee Failed", "Failed to create user account for employee: " + name);
                return;
            }

            // Then create employee
            Employee employee = new Employee();
            employee.setUserId(userId);
            employee.setName(name);
            employee.setPosition(position);
            employee.setHireDate(hireDate);
            employee.setSalary(salary);
            employee.setQrCode(""); // Empty QR code initially

            if (employeeService.addEmployee(employee)) {
                showSuccess("Employee and user account created successfully: " + name);
                showSuccessAlert("Employee Added Successfully",
                        "Employee: " + name + "\n" +
                                "Position: " + position + "\n" +
                                "Salary: Rp " + String.format("%,d", salary) + "\n" +
                                "Username: " + generateUsername(name) + "\n" +
                                "Email: " + generateEmail(name) + "\n\n" +
                                "User account has been created successfully!");
                handleClear();
                loadEmployees();
                updateStatistics();
            } else {
                // If employee creation fails, we should ideally rollback user creation
                showError("Failed to create employee record");
                showErrorAlert("Add Employee Failed", "Failed to create employee record for: " + name);
            }

        } catch (Exception e) {
            showError("Error adding employee: " + e.getMessage());
            showErrorAlert("Add Employee Error", "An error occurred while adding employee:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedEmployee == null) {
            showError("Please select an employee to update");
            showErrorAlert("Update Failed", "Please select an employee from the table to update.");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            String name = txtName.getText().trim();
            String position = cmbPosition.getValue();
            int salary = Integer.parseInt(txtSalary.getText().trim());

            // Update selected employee
            Employee employee = new Employee();
            employee.setEmployeeId(selectedEmployee.getEmployeeId());
            employee.setUserId(selectedEmployee.getUserId()); // Keep existing user_id
            employee.setName(name);
            employee.setPosition(position);
            employee.setHireDate(Date.valueOf(dateHireDate.getValue()));
            employee.setSalary(salary);
            employee.setQrCode(selectedEmployee.getQrCode()); // Keep existing QR code

            if (employeeService.updateEmployee(employee)) {
                showSuccess("Employee updated successfully: " + employee.getName());
                showSuccessAlert("Employee Updated Successfully",
                        "Employee information has been updated:\n\n" +
                                "Name: " + name + "\n" +
                                "Position: " + position + "\n" +
                                "Salary: Rp " + String.format("%,d", salary) + "\n" +
                                "Hire Date: " + dateHireDate.getValue() + "\n\n" +
                                "Changes saved successfully!");
                handleClear();
                loadEmployees();
                updateStatistics();
            } else {
                showError("Failed to update employee in database");
                showErrorAlert("Update Failed", "Failed to update employee information in the database.");
            }

        } catch (Exception e) {
            showError("Error updating employee: " + e.getMessage());
            showErrorAlert("Update Error", "An error occurred while updating employee:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedEmployee == null) {
            showError("Please select an employee to delete");
            showErrorAlert("Delete Failed", "Please select an employee from the table to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Employee");
        confirmAlert.setContentText("Are you sure you want to delete employee: " + selectedEmployee.getName() + "?\n\n" +
                "Position: " + selectedEmployee.getPosition() + "\n" +
                "Salary: Rp " + String.format("%,d", selectedEmployee.getSalary()) + "\n\n" +
                "This action cannot be undone and will also delete the associated user account.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                String employeeName = selectedEmployee.getName();
                String employeePosition = selectedEmployee.getPosition();

                if (employeeService.deleteEmployee(selectedEmployee.getEmployeeId())) {
                    // Also delete the associated user account
                    if (selectedEmployee.getUserId() > 0) {
                        userService.deleteUser(selectedEmployee.getUserId());
                    }

                    showSuccess("Employee and user account deleted successfully: " + employeeName);
                    showSuccessAlert("Employee Deleted Successfully",
                            "The following employee has been deleted:\n\n" +
                                    "Name: " + employeeName + "\n" +
                                    "Position: " + employeePosition + "\n\n" +
                                    "User account has also been removed from the system.");
                    handleClear();
                    loadEmployees();
                    updateStatistics();
                } else {
                    showError("Failed to delete employee from database");
                    showErrorAlert("Delete Failed", "Failed to delete employee from the database.");
                }

            } catch (Exception e) {
                showError("Error deleting employee: " + e.getMessage());
                showErrorAlert("Delete Error", "An error occurred while deleting employee:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleClear() {
        txtName.clear();
        cmbPosition.setValue(null);
        dateHireDate.setValue(null);
        txtSalary.clear();
        txtSalary.setPromptText("Enter salary amount");

        tableEmployees.getSelectionModel().clearSelection();
        selectedEmployee = null;

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        lblStatus.setText("Form cleared. Ready to add new employee.");
        lblStatus.setStyle("-fx-text-fill: #666666;");
    }

    @FXML
    private void handleRefresh() {
        try {
            loadEmployees();
            updateStatistics();
            showSuccess("Employee data refreshed");
            showSuccessAlert("Data Refreshed",
                    "Employee data has been refreshed successfully!\n\n" +
                            "Total employees: " + employeeData.size());
        } catch (Exception e) {
            showError("Error refreshing data: " + e.getMessage());
            showErrorAlert("Refresh Error", "An error occurred while refreshing data:\n" + e.getMessage());
        }
    }

    private void loadEmployees() {
        employeeData.clear();

        try {
            List<Employee> employees = employeeService.getAllEmployees();

            for (Employee emp : employees) {
                EmployeeRecord record = new EmployeeRecord();
                record.setEmployeeId(emp.getEmployeeId());
                record.setUserId(emp.getUserId());
                record.setName(emp.getName());
                record.setPosition(emp.getPosition());
                record.setQrCode(emp.getQrCode() != null ? emp.getQrCode() : "");
                record.setHireDate(emp.getHireDate() != null ? emp.getHireDate().toString() : "");
                record.setSalary(emp.getSalary());

                employeeData.add(record);
            }
        } catch (Exception e) {
            System.err.println("Error loading employees: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading employee data: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        String name = txtName.getText().trim();
        String position = cmbPosition.getValue();
        String salaryText = txtSalary.getText().trim();

        if (name.isEmpty()) {
            showError("Employee name is required");
            return false;
        }

        if (position == null || position.isEmpty()) {
            showError("Position is required");
            return false;
        }

        if (dateHireDate.getValue() == null) {
            showError("Hire date is required");
            return false;
        }

        if (salaryText.isEmpty()) {
            showError("Salary is required");
            return false;
        }

        try {
            int salary = Integer.parseInt(salaryText);
            if (salary <= 0) {
                showError("Salary must be a positive number");
                return false;
            }

            // Validate minimum salary for position
            if (minimumSalaries.containsKey(position)) {
                int minSalary = minimumSalaries.get(position);
                if (salary < minSalary) {
                    showError("Salary for " + position + " must be at least Rp " + String.format("%,d", minSalary));
                    return false;
                }
            }

        } catch (NumberFormatException e) {
            showError("Salary must be a valid number");
            return false;
        }

        return true;
    }

    private void populateForm(EmployeeRecord employee) {
        txtName.setText(employee.getName());
        cmbPosition.setValue(employee.getPosition());
        txtSalary.setText(String.valueOf(employee.getSalary()));

        if (employee.getHireDate() != null && !employee.getHireDate().isEmpty()) {
            try {
                LocalDate hireDate = LocalDate.parse(employee.getHireDate());
                dateHireDate.setValue(hireDate);
            } catch (Exception e) {
                dateHireDate.setValue(null);
            }
        }
    }

    private String generateUsername(String name) {
        // Convert name to lowercase and replace spaces with underscores
        String username = name.toLowerCase().replaceAll("\\s+", "_");
        // Remove special characters
        username = username.replaceAll("[^a-z0-9_]", "");
        return username;
    }

    private String generateEmail(String name) {
        String username = generateUsername(name);
        return username + "@senimatic.com";
    }

    private void updateStatistics() {
        lblTotalEmployees.setText("Total Employees: " + employeeData.size());
    }

    private void showSuccess(String message) {
        lblStatus.setText("✓ " + message);
        lblStatus.setStyle("-fx-text-fill: #4CAF50;");
    }

    private void showError(String message) {
        lblStatus.setText("✗ " + message);
        lblStatus.setStyle("-fx-text-fill: #F44336;");
    }

    // Inner class for table data
    public static class EmployeeRecord {
        private int employeeId;
        private int userId;
        private String name;
        private String position;
        private String qrCode;
        private String hireDate;
        private int salary;

        // Getters and setters
        public int getEmployeeId() { return employeeId; }
        public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }

        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }

        public String getHireDate() { return hireDate; }
        public void setHireDate(String hireDate) { this.hireDate = hireDate; }

        public int getSalary() { return salary; }
        public void setSalary(int salary) { this.salary = salary; }
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
