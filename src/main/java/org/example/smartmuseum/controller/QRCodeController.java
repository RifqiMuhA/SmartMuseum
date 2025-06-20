package org.example.smartmuseum.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class QRCodeController implements Initializable {

    @FXML private ComboBox<Employee> cmbEmployees;
    @FXML private Label lblEmployeeId;
    @FXML private Label lblEmployeeName;
    @FXML private Label lblEmployeePosition;
    @FXML private Label lblQRCode;
    @FXML private Button btnGenerateQR;
    @FXML private Button btnSaveQR;
    @FXML private Button btnPrintQR;
    @FXML private ImageView imgQRCode;
    @FXML private Label lblNoQRCode;
    @FXML private Label lblQRCodeText;

    private UserService userService;
    private Employee selectedEmployee;
    private String currentQRCode;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            userService = UserService.getInstance();

            setupEmployeeComboBox();
            loadEmployees();

            // Initially hide QR code image
            imgQRCode.setVisible(false);
            lblNoQRCode.setVisible(true);

            System.out.println("✅ QRCodeController initialized successfully");

        } catch (Exception e) {
            showAlert("Initialization Error", "Failed to initialize QR Code Controller: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupEmployeeComboBox() {
        // Custom cell factory for dropdown items
        cmbEmployees.setCellFactory(listView -> new ListCell<Employee>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                if (empty || employee == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s (ID: %d)",
                            employee.getName(), employee.getPosition(), employee.getEmployeeId()));
                }
            }
        });

        // Custom button cell for selected item display
        cmbEmployees.setButtonCell(new ListCell<Employee>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                if (empty || employee == null) {
                    setText("Pilih pegawai...");
                } else {
                    setText(String.format("%s - %s", employee.getName(), employee.getPosition()));
                }
            }
        });
    }

    private void loadEmployees() {
        try {
            List<Employee> employees = userService.getAllEmployees();
            cmbEmployees.setItems(FXCollections.observableArrayList(employees));
            System.out.println("✅ Loaded " + employees.size() + " employees");
        } catch (Exception e) {
            showAlert("Error", "Failed to load employees: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEmployeeSelection() {
        selectedEmployee = cmbEmployees.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            updateEmployeeDetails();
            btnGenerateQR.setDisable(false);
            System.out.println("Selected employee: " + selectedEmployee.getName());
        } else {
            clearEmployeeDetails();
            btnGenerateQR.setDisable(true);
        }

        // Clear previous QR code
        clearQRCode();
    }

    private void updateEmployeeDetails() {
        lblEmployeeId.setText(String.valueOf(selectedEmployee.getEmployeeId()));
        lblEmployeeName.setText(selectedEmployee.getName());
        lblEmployeePosition.setText(selectedEmployee.getPosition());

        String qrCodeText = selectedEmployee.getQRCode();
        if (qrCodeText != null && !qrCodeText.isEmpty()) {
            lblQRCode.setText(qrCodeText);
            lblQRCode.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else {
            lblQRCode.setText("Not Generated");
            lblQRCode.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private void clearEmployeeDetails() {
        lblEmployeeId.setText("-");
        lblEmployeeName.setText("-");
        lblEmployeePosition.setText("-");
        lblQRCode.setText("-");
        lblQRCode.setStyle("-fx-text-fill: #2c3e50;");
    }

    private void clearQRCode() {
        imgQRCode.setVisible(false);
        lblNoQRCode.setVisible(true);
        lblQRCodeText.setText("");
        btnSaveQR.setDisable(true);
        btnPrintQR.setDisable(true);
        currentQRCode = null;
    }

    @FXML
    private void handleGenerateQR() {
        if (selectedEmployee == null) {
            showAlert("Error", "Silakan pilih pegawai terlebih dahulu", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Generate new QR code content
            currentQRCode = QRCodeGenerator.generateEmployeeQRCode(
                    selectedEmployee.getEmployeeId(),
                    selectedEmployee.getName()
            );

            // Generate QR code image
            Image qrImage = QRCodeGenerator.generateQRCodeImage(currentQRCode, 300, 300);

            // Display QR code
            imgQRCode.setImage(qrImage);
            imgQRCode.setVisible(true);
            lblNoQRCode.setVisible(false);
            lblQRCodeText.setText(currentQRCode);

            // Enable save and print buttons
            btnSaveQR.setDisable(false);
            btnPrintQR.setDisable(false);

            // Update employee QR code if it's new
            if (selectedEmployee.getQRCode() == null || selectedEmployee.getQRCode().isEmpty()) {
                selectedEmployee.setQrCode(currentQRCode);
                lblQRCode.setText(currentQRCode);
                lblQRCode.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                // TODO: Update in database
            }

            showAlert("Success",
                    String.format("QR Code berhasil dibuat untuk %s!\n\nQR Code: %s",
                            selectedEmployee.getName(), currentQRCode),
                    Alert.AlertType.INFORMATION);

            System.out.println("✅ QR Code generated successfully: " + currentQRCode);

        } catch (Exception e) {
            showAlert("Error", "Failed to generate QR code: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveQR() {
        if (currentQRCode == null || selectedEmployee == null) {
            showAlert("Error", "Belum ada QR Code yang dibuat", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save QR Code");

        // Set default filename
        String defaultFileName = String.format("QRCode_%s_%s.png",
                selectedEmployee.getName().replaceAll("\\s+", "_"),
                selectedEmployee.getEmployeeId());
        fileChooser.setInitialFileName(defaultFileName);

        // Set file extension filter
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPG Files", "*.jpg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showSaveDialog(btnSaveQR.getScene().getWindow());
        if (file != null) {
            try {
                QRCodeGenerator.saveQRCodeImage(currentQRCode, file.getAbsolutePath(), 300, 300);

                showAlert("Success",
                        String.format("QR Code berhasil disimpan!\n\nLocation: %s\nEmployee: %s\nQR Code: %s",
                                file.getAbsolutePath(), selectedEmployee.getName(), currentQRCode),
                        Alert.AlertType.INFORMATION);

                System.out.println("✅ QR Code saved to: " + file.getAbsolutePath());

            } catch (Exception e) {
                showAlert("Error", "Failed to save QR code: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handlePrintQR() {
        if (currentQRCode == null) {
            showAlert("Error", "Belum ada QR Code yang dibuat", Alert.AlertType.WARNING);
            return;
        }

        // TODO: Implement printing functionality
        showAlert("Info",
                "Fitur print akan segera tersedia.\n\nSementara ini, Anda dapat menyimpan QR Code " +
                        "dan mencetaknya menggunakan aplikasi lain.",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleRefreshEmployees() {
        try {
            loadEmployees();
            clearEmployeeDetails();
            clearQRCode();
            cmbEmployees.getSelectionModel().clearSelection();
            showAlert("Success", "Data pegawai berhasil direfresh", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to refresh employees: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert based on type
        DialogPane dialogPane = alert.getDialogPane();
        switch (type) {
            case ERROR:
                dialogPane.setStyle("-fx-background-color: #ffebee;");
                break;
            case WARNING:
                dialogPane.setStyle("-fx-background-color: #fff3e0;");
                break;
            case INFORMATION:
                dialogPane.setStyle("-fx-background-color: #e8f5e8;");
                break;
        }

        alert.showAndWait();
    }
}