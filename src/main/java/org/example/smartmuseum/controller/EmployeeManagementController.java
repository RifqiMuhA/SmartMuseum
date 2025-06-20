package org.example.smartmuseum.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EmployeeManagementController implements Initializable {

    // Table and data
    @FXML private TableView<Employee> tableEmployees;
    @FXML private TableColumn<Employee, Integer> colEmployeeId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colPosition;
    @FXML private TableColumn<Employee, String> colQRCode;
    @FXML private TableColumn<Employee, LocalDate> colHireDate;
    @FXML private TableColumn<Employee, Boolean> colActive;

    // Form fields
    @FXML private TextField txtName;
    @FXML private TextField txtPosition;
    @FXML private TextField txtDepartment;
    @FXML private DatePicker dateHireDate;
    @FXML private TextField txtSalary;
    @FXML private CheckBox chkActive;

    // Buttons
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private Button btnGenerateQR;

    // QR Code display
    @FXML private ImageView imgQRCode;
    @FXML private Label lblQRCodeText;

    // Status
    @FXML private Label lblStatus;
    @FXML private Label lblTotalEmployees;

    private UserService userService;
    private ObservableList<Employee> employeeData;
    private Employee selectedEmployee;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            userService = UserService.getInstance();
            employeeData = FXCollections.observableArrayList();

            initializeTable();
            loadEmployees();
            setupFormValidation();

            System.out.println("✅ Employee Management initialized");

        } catch (Exception e) {
            showStatus("Error initializing: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private void initializeTable() {
        // Configure columns
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colQRCode.setCellValueFactory(new PropertyValueFactory<>("qrCode"));
        colHireDate.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        // Style active column
        colActive.setCellFactory(column -> new TableCell<Employee, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Active" : "Inactive");
                    setStyle(item ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                            : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            }
        });

        // Set table data
        tableEmployees.setItems(employeeData);

        // Handle selection
        tableEmployees.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectEmployee(newSelection);
                    }
                });
    }

    private void loadEmployees() {
        try {
            List<Employee> employees = userService.getAllEmployees();
            employeeData.clear();
            employeeData.addAll(employees);

            lblTotalEmployees.setText("Total Employees: " + employees.size());
            showStatus("Loaded " + employees.size() + " employees", "success");

        } catch (Exception e) {
            showStatus("Error loading employees: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private void selectEmployee(Employee employee) {
        selectedEmployee = employee;

        // Populate form
        txtName.setText(employee.getName());
        txtPosition.setText(employee.getPosition());
        dateHireDate.setValue(employee.getHireDate());
        chkActive.setSelected(employee.isActive());

        // Generate and show QR code
        generateQRCodeForEmployee(employee);

        // Enable update/delete buttons
        btnUpdate.setDisable(false);
        btnDelete.setDisable(false);
        btnGenerateQR.setDisable(false);
    }

    private void generateQRCodeForEmployee(Employee employee) {
        try {
            if (employee.getQRCode() == null || employee.getQRCode().isEmpty()) {
                employee.generateQRCode();
            }

            String qrCodeText = employee.getQRCode();
            lblQRCodeText.setText(qrCodeText);

            // Generate QR code image
            Image qrImage = QRCodeGenerator.generateQRCodeImage(qrCodeText, 200, 200);
            imgQRCode.setImage(qrImage);

        } catch (Exception e) {
            showStatus("Error generating QR code: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            return;
        }

        try {
            // Create new employee
            Employee employee = new Employee();
            employee.setName(txtName.getText().trim());
            employee.setPosition(txtPosition.getText().trim());
            employee.setHireDate(dateHireDate.getValue());
            employee.setActive(chkActive.isSelected());

            // Add to database
            Employee savedEmployee = userService.addEmployee(employee);
            if (savedEmployee != null) {
                // Auto-generate QR code
                savedEmployee.generateQRCode();

                // Add to table
                employeeData.add(savedEmployee);

                // Show QR code
                generateQRCodeForEmployee(savedEmployee);

                showStatus("Employee added successfully: " + savedEmployee.getName(), "success");
                clearForm();
                lblTotalEmployees.setText("Total Employees: " + employeeData.size());

            } else {
                showStatus("Failed to add employee", "error");
            }

        } catch (Exception e) {
            showStatus("Error adding employee: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedEmployee == null) {
            showStatus("Please select an employee to update", "warning");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            // Update employee data
            selectedEmployee.setName(txtName.getText().trim());
            selectedEmployee.setPosition(txtPosition.getText().trim());
            selectedEmployee.setHireDate(dateHireDate.getValue());
            selectedEmployee.setActive(chkActive.isSelected());

            // TODO: Update in database
            // userService.updateEmployee(selectedEmployee);

            // Refresh table
            tableEmployees.refresh();

            showStatus("Employee updated successfully: " + selectedEmployee.getName(), "success");

        } catch (Exception e) {
            showStatus("Error updating employee: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedEmployee == null) {
            showStatus("Please select an employee to delete", "warning");
            return;
        }

        // Confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Employee");
        alert.setContentText("Are you sure you want to delete employee: " + selectedEmployee.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // TODO: Delete from database
                // userService.deleteEmployee(selectedEmployee.getEmployeeId());

                // Remove from table
                employeeData.remove(selectedEmployee);

                showStatus("Employee deleted successfully", "success");
                clearForm();
                lblTotalEmployees.setText("Total Employees: " + employeeData.size());

            } catch (Exception e) {
                showStatus("Error deleting employee: " + e.getMessage(), "error");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    @FXML
    private void handleGenerateQR() {
        if (selectedEmployee == null) {
            showStatus("Please select an employee first", "warning");
            return;
        }

        try {
            // Regenerate QR code
            selectedEmployee.generateQRCode();
            generateQRCodeForEmployee(selectedEmployee);

            // Refresh table
            tableEmployees.refresh();

            showStatus("QR Code regenerated for: " + selectedEmployee.getName(), "success");

        } catch (Exception e) {
            showStatus("Error generating QR code: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadEmployees();
        clearForm();
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            showStatus("Name is required", "warning");
            txtName.requestFocus();
            return false;
        }

        if (txtPosition.getText().trim().isEmpty()) {
            showStatus("Position is required", "warning");
            txtPosition.requestFocus();
            return false;
        }

        if (dateHireDate.getValue() == null) {
            showStatus("Hire date is required", "warning");
            dateHireDate.requestFocus();
            return false;
        }

        if (dateHireDate.getValue().isAfter(LocalDate.now())) {
            showStatus("Hire date cannot be in the future", "warning");
            dateHireDate.requestFocus();
            return false;
        }

        return true;
    }

    private void clearForm() {
        txtName.clear();
        txtPosition.clear();
        txtDepartment.clear();
        txtSalary.clear();
        dateHireDate.setValue(null);
        chkActive.setSelected(true);

        selectedEmployee = null;
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnGenerateQR.setDisable(true);

        imgQRCode.setImage(null);
        lblQRCodeText.setText("");

        tableEmployees.getSelectionModel().clearSelection();
    }

    private void setupFormValidation() {
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnGenerateQR.setDisable(true);
        chkActive.setSelected(true);
        dateHireDate.setValue(LocalDate.now());
    }

    private void showStatus(String message, String type) {
        lblStatus.setText(message);

        switch (type.toLowerCase()) {
            case "success":
                lblStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                break;
            case "error":
                lblStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                break;
            case "warning":
                lblStatus.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                break;
            default:
                lblStatus.setStyle("-fx-text-fill: #2c3e50;");
        }

        System.out.println("[" + type.toUpperCase() + "] " + message);
    }
}