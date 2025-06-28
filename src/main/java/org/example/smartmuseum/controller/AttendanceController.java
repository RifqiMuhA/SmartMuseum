package org.example.smartmuseum.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import org.example.smartmuseum.model.entity.Attendance;
import org.example.smartmuseum.util.CameraQRScanner;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AttendanceController implements Initializable {

    @FXML private ImageView cameraView;
    @FXML private Label lblCameraStatus;
    @FXML private Button btnStartCamera;
    @FXML private Button btnStopCamera;
    @FXML private TextField txtQRInput;
    @FXML private Button btnProcessQR;
    @FXML private Label lblScanResult;
    @FXML private TableView<AttendanceRecord> tableAttendance;
    @FXML private TableColumn<AttendanceRecord, String> colEmployeeName;
    @FXML private TableColumn<AttendanceRecord, String> colAction;
    @FXML private TableColumn<AttendanceRecord, String> colTimestamp;
    @FXML private TableColumn<AttendanceRecord, String> colStatus;

    private ObservableList<AttendanceRecord> attendanceData;
    private CameraQRScanner qrScanner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadSampleData();

        // Initialize QR scanner
        qrScanner = new CameraQRScanner();

        // Initial state
        btnStopCamera.setDisable(true);
        lblCameraStatus.setText("Camera not started");
        lblScanResult.setText("Ready to scan...");
    }

    private void setupTable() {
        attendanceData = FXCollections.observableArrayList();

        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableAttendance.setItems(attendanceData);
    }

    private void loadSampleData() {
        attendanceData.addAll(
                new AttendanceRecord("John Doe", "Check In", "2024-01-15 08:30:00", "Present"),
                new AttendanceRecord("Jane Smith", "Check Out", "2024-01-15 17:00:00", "Present"),
                new AttendanceRecord("Bob Johnson", "Check In", "2024-01-15 09:15:00", "Late"),
                new AttendanceRecord("Alice Brown", "Check In", "2024-01-15 08:45:00", "Present")
        );
    }

    @FXML
    private void handleStartCamera() {
        try {
            qrScanner.startScanning(new CameraQRScanner.QRScanCallback() {
                @Override
                public void onQRCodeDetected(String qrCode) {
                    processQRCode(qrCode);
                }

                @Override
                public void onImageUpdate(Image image) {
                    cameraView.setImage(image);
                }

                @Override
                public void onError(String error) {
                    lblCameraStatus.setText("Camera Error: " + error);
                    lblScanResult.setText("❌ Error: " + error);
                    handleStopCamera();
                }
            });

            btnStartCamera.setDisable(true);
            btnStopCamera.setDisable(false);
            lblCameraStatus.setText("Camera started - Scanning for QR codes...");
            lblScanResult.setText("🔍 Camera is active. Show QR code to camera for automatic detection...");

        } catch (Exception e) {
            lblCameraStatus.setText("Failed to start camera");
            lblScanResult.setText("❌ Error: Could not access camera. " + e.getMessage());
            System.err.println("Camera start error: " + e.getMessage());
        }
    }

    @FXML
    private void handleStopCamera() {
        qrScanner.stopScanning();
        btnStartCamera.setDisable(false);
        btnStopCamera.setDisable(true);
        lblCameraStatus.setText("Camera stopped");
        lblScanResult.setText("Camera stopped. Click 'Start Camera' to resume scanning.");
        cameraView.setImage(null);
    }

    @FXML
    private void handleProcessQR() {
        String qrCode = txtQRInput.getText().trim();

        if (qrCode.isEmpty()) {
            lblScanResult.setText("Please enter a QR code to process.");
            return;
        }

        // Process QR code
        processQRCode(qrCode);
        txtQRInput.clear();
    }

    private void processQRCode(String qrCode) {
        try {
            // Simulate QR code processing
            String employeeName = "Employee " + qrCode.substring(Math.max(0, qrCode.length() - 3));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String action = "Check In"; // Default action
            String status = "Present";

            // Add to table
            AttendanceRecord record = new AttendanceRecord(employeeName, action, timestamp, status);
            attendanceData.add(0, record); // Add to top

            lblScanResult.setText("✅ Attendance recorded for: " + employeeName + " at " + timestamp);

        } catch (Exception e) {
            lblScanResult.setText("❌ Error processing QR code: " + e.getMessage());
        }
    }

    public void refreshData() {
        // Refresh attendance data
        loadSampleData();
    }

    public void shutdown() {
        if (qrScanner != null) {
            qrScanner.shutdown();
        }
    }

    // Inner class for table data
    public static class AttendanceRecord {
        private String employeeName;
        private String action;
        private String timestamp;
        private String status;

        public AttendanceRecord(String employeeName, String action, String timestamp, String status) {
            this.employeeName = employeeName;
            this.action = action;
            this.timestamp = timestamp;
            this.status = status;
        }

        // Getters
        public String getEmployeeName() { return employeeName; }
        public String getAction() { return action; }
        public String getTimestamp() { return timestamp; }
        public String getStatus() { return status; }

        // Setters
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public void setAction(String action) { this.action = action; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public void setStatus(String status) { this.status = status; }
    }
}
