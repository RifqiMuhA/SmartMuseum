package org.example.smartmuseum.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.service.EmployeeService;
import org.example.smartmuseum.util.QRCodeGenerator;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EmployeeManagementController implements Initializable {

    @FXML private Label lblTotalEmployees;
    @FXML private TextField txtName;
    @FXML private TextField txtPosition;
    @FXML private TextField txtDepartment;
    @FXML private DatePicker dateHireDate;
    @FXML private TextField txtSalary;
    @FXML private CheckBox chkActive;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private ImageView imgQRCode;
    @FXML private Label lblQRCodeText;
    @FXML private Button btnGenerateQR;
    @FXML private Label lblStatus;
    @FXML private TableView<EmployeeRecord> tableEmployees;
    @FXML private TableColumn<EmployeeRecord, Integer> colEmployeeId;
    @FXML private TableColumn<EmployeeRecord, String> colName;
    @FXML private TableColumn<EmployeeRecord, String> colPosition;
    @FXML private TableColumn<EmployeeRecord, String> colQRCode;
    @FXML private TableColumn<EmployeeRecord, String> colHireDate;
    @FXML private TableColumn<EmployeeRecord, String> colActive;

    private EmployeeService employeeService;
    private ObservableList<EmployeeRecord> employeeData;
    private EmployeeRecord selectedEmployee;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        employeeService = new EmployeeService();
        employeeData = FXCollections.observableArrayList();

        setupTableColumns();
        setupTableSelection();
        loadEmployees();
        updateStatistics();

        // Initially disable update and delete buttons
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnGenerateQR.setDisable(true);
    }

    private void setupTableColumns() {
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colQRCode.setCellValueFactory(new PropertyValueFactory<>("qrCode"));
        colHireDate.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        tableEmployees.setItems(employeeData);
    }

    private void setupTableSelection() {
        tableEmployees.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedEmployee = newSelection;
            if (newSelection != null) {
                populateForm(newSelection);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
                btnGenerateQR.setDisable(false);

                // Display existing QR code if available
                if (newSelection.getQrCode() != null && !newSelection.getQrCode().isEmpty()) {
                    displayQRCode(newSelection.getQrCode());
                }
            } else {
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);
                btnGenerateQR.setDisable(true);
                clearQRCode();
            }
        });
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            return;
        }

        try {
            EmployeeRecord newEmployee = createEmployeeFromForm();
            newEmployee.setEmployeeId(employeeData.size() + 1); // Simple ID generation

            // Add to service
            Employee employee = new Employee();
            employee.setEmployeeId(newEmployee.getEmployeeId());
            employee.setName(newEmployee.getName());
            employee.setPosition(newEmployee.getPosition());

            if (employeeService.addEmployee(employee)) {
                employeeData.add(newEmployee);
                showSuccess("Employee added successfully: " + newEmployee.getName());
                handleClear();
                updateStatistics();
            } else {
                showError("Failed to add employee to database");
            }

        } catch (Exception e) {
            showError("Error adding employee: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedEmployee == null) {
            showError("Please select an employee to update");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            updateEmployeeFromForm(selectedEmployee);

            // Update in service
            Employee employee = new Employee();
            employee.setEmployeeId(selectedEmployee.getEmployeeId());
            employee.setName(selectedEmployee.getName());
            employee.setPosition(selectedEmployee.getPosition());

            if (employeeService.updateEmployee(employee)) {
                tableEmployees.refresh();
                showSuccess("Employee updated successfully: " + selectedEmployee.getName());
                handleClear();
            } else {
                showError("Failed to update employee in database");
            }

        } catch (Exception e) {
            showError("Error updating employee: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedEmployee == null) {
            showError("Please select an employee to delete");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Employee");
        confirmAlert.setContentText("Are you sure you want to delete employee: " + selectedEmployee.getName() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                if (employeeService.deleteEmployee(selectedEmployee.getEmployeeId())) {
                    employeeData.remove(selectedEmployee);
                    showSuccess("Employee deleted successfully: " + selectedEmployee.getName());
                    handleClear();
                    updateStatistics();
                } else {
                    showError("Failed to delete employee from database");
                }

            } catch (Exception e) {
                showError("Error deleting employee: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        txtName.clear();
        txtPosition.clear();
        txtDepartment.clear();
        dateHireDate.setValue(null);
        txtSalary.clear();
        chkActive.setSelected(true);

        tableEmployees.getSelectionModel().clearSelection();
        selectedEmployee = null;
        clearQRCode();

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnGenerateQR.setDisable(true);

        lblStatus.setText("Form cleared. Ready to add new employee.");
        lblStatus.setStyle("-fx-text-fill: #666666;");
    }

    @FXML
    private void handleGenerateQR() {
        if (selectedEmployee == null) {
            showError("Please select an employee to generate QR code");
            return;
        }

        try {
            String qrData = "EMP" + selectedEmployee.getEmployeeId() + "_" +
                    selectedEmployee.getName().replaceAll("\\s+", "_");
            String qrCode = QRCodeGenerator.generateCustomQRData(qrData);

            selectedEmployee.setQrCode(qrCode);
            tableEmployees.refresh();

            displayQRCode(qrCode);

            showSuccess("QR code generated for: " + selectedEmployee.getName());

        } catch (Exception e) {
            showError("Error generating QR code: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadEmployees();
        updateStatistics();
        showSuccess("Employee data refreshed");
    }

    private void loadEmployees() {
        employeeData.clear();

        try {
            List<Employee> employees = employeeService.getAllEmployees();

            for (Employee emp : employees) {
                EmployeeRecord record = new EmployeeRecord();
                record.setEmployeeId(emp.getEmployeeId());
                record.setName(emp.getName());
                record.setPosition(emp.getPosition());
                record.setQrCode(emp.getQRCode());
                record.setHireDate("2024-01-01"); // Default date, would need to add to Employee entity
                record.setActive("Active"); // Default status, would need to add to Employee entity

                employeeData.add(record);
            }
        } catch (Exception e) {
            System.err.println("Error loading employees: " + e.getMessage());
            createSampleEmployees(); // Fallback to sample data
        }
    }

    private void createSampleEmployees() {
        // Sample employee 1
        EmployeeRecord emp1 = new EmployeeRecord();
        emp1.setEmployeeId(1);
        emp1.setName("John Smith");
        emp1.setPosition("Gallery Manager");
        emp1.setQrCode("QR_EMP1_John_Smith_12345678");
        emp1.setHireDate("2023-01-15");
        emp1.setActive("Active");
        employeeData.add(emp1);

        // Sample employee 2
        EmployeeRecord emp2 = new EmployeeRecord();
        emp2.setEmployeeId(2);
        emp2.setName("Sarah Johnson");
        emp2.setPosition("Curator");
        emp2.setQrCode("QR_EMP2_Sarah_Johnson_87654321");
        emp2.setHireDate("2023-03-20");
        emp2.setActive("Active");
        employeeData.add(emp2);

        // Sample employee 3
        EmployeeRecord emp3 = new EmployeeRecord();
        emp3.setEmployeeId(3);
        emp3.setName("Mike Davis");
        emp3.setPosition("Security Guard");
        emp3.setQrCode("");
        emp3.setHireDate("2023-06-10");
        emp3.setActive("Active");
        employeeData.add(emp3);
    }

    private boolean validateForm() {
        String name = txtName.getText().trim();
        String position = txtPosition.getText().trim();

        if (name.isEmpty()) {
            showError("Employee name is required");
            return false;
        }

        if (position.isEmpty()) {
            showError("Position is required");
            return false;
        }

        return true;
    }

    private EmployeeRecord createEmployeeFromForm() {
        EmployeeRecord employee = new EmployeeRecord();
        employee.setName(txtName.getText().trim());
        employee.setPosition(txtPosition.getText().trim());
        employee.setHireDate(dateHireDate.getValue() != null ?
                dateHireDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        employee.setActive(chkActive.isSelected() ? "Active" : "Inactive");
        employee.setQrCode(""); // Will be generated separately

        return employee;
    }

    private void updateEmployeeFromForm(EmployeeRecord employee) {
        employee.setName(txtName.getText().trim());
        employee.setPosition(txtPosition.getText().trim());
        employee.setHireDate(dateHireDate.getValue() != null ?
                dateHireDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        employee.setActive(chkActive.isSelected() ? "Active" : "Inactive");
    }

    private void populateForm(EmployeeRecord employee) {
        txtName.setText(employee.getName());
        txtPosition.setText(employee.getPosition());

        if (employee.getHireDate() != null && !employee.getHireDate().isEmpty()) {
            try {
                LocalDate hireDate = LocalDate.parse(employee.getHireDate());
                dateHireDate.setValue(hireDate);
            } catch (Exception e) {
                dateHireDate.setValue(null);
            }
        }

        chkActive.setSelected("Active".equals(employee.getActive()));
    }

    private void displayQRCode(String qrCode) {
        // In a real implementation, this would generate and display an actual QR code image
        lblQRCodeText.setText(qrCode);
        lblQRCodeText.setVisible(true);
    }

    private void clearQRCode() {
        imgQRCode.setImage(null);
        lblQRCodeText.setText("");
        lblQRCodeText.setVisible(false);
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
        private String name;
        private String position;
        private String qrCode;
        private String hireDate;
        private String active;

        // Getters and setters
        public int getEmployeeId() { return employeeId; }
        public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }

        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }

        public String getHireDate() { return hireDate; }
        public void setHireDate(String hireDate) { this.hireDate = hireDate; }

        public String getActive() { return active; }
        public void setActive(String active) { this.active = active; }
    }
}
