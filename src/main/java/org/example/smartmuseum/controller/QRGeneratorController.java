package org.example.smartmuseum.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.example.smartmuseum.database.EmployeeDAO;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.util.QRCodeGenerator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class QRGeneratorController implements Initializable {

    @FXML private ComboBox<String> cmbQRType;
    @FXML private ComboBox<String> cmbEmployee;
    @FXML private TextArea txtCustomData;
    @FXML private Button btnGenerate;
    @FXML private Button btnSave;
    @FXML private ImageView imgQRCode;
    @FXML private Label lblQRData;
    @FXML private Label lblStatus;

    private BufferedImage currentQRImage;
    private String currentQRData;
    private EmployeeDAO employeeDAO;
    private ObservableList<Employee> employees;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        employeeDAO = new EmployeeDAO();
        employees = FXCollections.observableArrayList();

        setupQRTypeComboBox();
        loadEmployees();
        setupEventHandlers();

        // Initial state
        btnSave.setDisable(true);
        lblStatus.setText("Ready to generate QR codes");
    }

    private void setupQRTypeComboBox() {
        cmbQRType.setItems(FXCollections.observableArrayList(
                "Employee QR Code",
                "Custom Data QR Code"
        ));

        cmbQRType.setValue("Employee QR Code");

        // Show/hide controls based on selection
        cmbQRType.setOnAction(e -> {
            String selectedType = cmbQRType.getValue();
            boolean isEmployee = "Employee QR Code".equals(selectedType);

            cmbEmployee.setVisible(isEmployee);
            cmbEmployee.setManaged(isEmployee);
            txtCustomData.setVisible(!isEmployee);
            txtCustomData.setManaged(!isEmployee);

            // Clear previous data
            clearQRCode();
        });
    }

    private void loadEmployees() {
        try {
            employees.clear();
            List<Employee> staffEmployees = employeeDAO.getAllStaffEmployees();

            System.out.println("=== LOADING EMPLOYEES FOR QR GENERATOR ===");

            if (staffEmployees.isEmpty()) {
                System.out.println("No staff employees found in database!");
                lblStatus.setText("⚠️ No staff employees found in database");
                cmbEmployee.setItems(FXCollections.observableArrayList("No employees found"));
                cmbEmployee.setDisable(true);
            } else {
                employees.addAll(staffEmployees);

                ObservableList<String> employeeNames = FXCollections.observableArrayList();
                for (Employee emp : staffEmployees) {
                    String displayText = emp.getEmployeeId() + " - " + emp.getName() + " (" + emp.getPosition() + ")";
                    employeeNames.add(displayText);
                    System.out.println("Loaded employee: " + displayText);
                }

                cmbEmployee.setItems(employeeNames);
                cmbEmployee.setDisable(false);
                lblStatus.setText("✅ " + staffEmployees.size() + " staff employees loaded");
            }

            System.out.println("=== END EMPLOYEE LOADING ===");

        } catch (Exception e) {
            System.err.println("Error loading employees: " + e.getMessage());
            e.printStackTrace();
            lblStatus.setText("❌ Error loading employees: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        btnGenerate.setOnAction(e -> generateQRCode());
        btnSave.setOnAction(e -> saveQRCode());
    }

    @FXML
    private void generateQRCode() {
        try {
            String qrType = cmbQRType.getValue();
            String qrData = null;

            if ("Employee QR Code".equals(qrType)) {
                String selectedEmployee = cmbEmployee.getValue();
                if (selectedEmployee == null || selectedEmployee.equals("No employees found")) {
                    lblStatus.setText("❌ Please select an employee");
                    return;
                }

                // Extract employee ID from selection
                int employeeId = Integer.parseInt(selectedEmployee.split(" - ")[0]);
                Employee employee = employees.stream()
                        .filter(emp -> emp.getEmployeeId() == employeeId)
                        .findFirst()
                        .orElse(null);

                if (employee == null) {
                    lblStatus.setText("❌ Employee not found");
                    return;
                }

                // Generate QR data for employee
                qrData = QRCodeGenerator.generateEmployeeQRData(employee.getEmployeeId(), employee.getName());

                // Update employee QR code in database
                boolean updated = employeeDAO.updateEmployeeQRCode(employee.getEmployeeId(), qrData);
                if (!updated) {
                    lblStatus.setText("⚠️ QR generated but failed to update database");
                } else {
                    System.out.println("Updated employee QR code in database: " + employee.getName() + " -> " + qrData);
                }

            } else if ("Custom Data QR Code".equals(qrType)) {
                String customData = txtCustomData.getText().trim();
                if (customData.isEmpty()) {
                    lblStatus.setText("❌ Please enter custom data");
                    return;
                }

                qrData = QRCodeGenerator.generateCustomQRData(customData);
            }

            if (qrData == null) {
                lblStatus.setText("❌ Failed to generate QR data");
                return;
            }

            // Generate QR code image
            currentQRImage = QRCodeGenerator.generateQRCodeImage(qrData, 300, 300);
            currentQRData = qrData;

            // Display QR code
            Image fxImage = SwingFXUtils.toFXImage(currentQRImage, null);
            imgQRCode.setImage(fxImage);

            // Show QR data
            lblQRData.setText("QR Data: " + qrData);

            // Enable save button
            btnSave.setDisable(false);

            lblStatus.setText("✅ QR Code generated successfully!");

            System.out.println("Generated QR Code: " + qrData);

        } catch (Exception e) {
            lblStatus.setText("❌ Error generating QR code: " + e.getMessage());
            System.err.println("QR generation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void saveQRCode() {
        if (currentQRImage == null) {
            lblStatus.setText("❌ No QR code to save");
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save QR Code");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG Images", "*.png")
            );

            // Set default filename
            String defaultName = "emp_qr_" + System.currentTimeMillis() + ".png";
            fileChooser.setInitialFileName(defaultName);

            File file = fileChooser.showSaveDialog(btnSave.getScene().getWindow());

            if (file != null) {
                boolean saved = QRCodeGenerator.saveQRCodeImage(currentQRImage, file.getAbsolutePath());
                if (saved) {
                    lblStatus.setText("✅ QR Code saved to: " + file.getName());
                } else {
                    lblStatus.setText("❌ Failed to save QR code");
                }
            }

        } catch (Exception e) {
            lblStatus.setText("❌ Error saving QR code: " + e.getMessage());
            System.err.println("QR save error: " + e.getMessage());
        }
    }

    @FXML
    private void refreshEmployees() {
        loadEmployees();
        clearQRCode();
    }

    private void clearQRCode() {
        imgQRCode.setImage(null);
        lblQRData.setText("QR Data: -");
        btnSave.setDisable(true);
        currentQRImage = null;
        currentQRData = null;
    }
}
