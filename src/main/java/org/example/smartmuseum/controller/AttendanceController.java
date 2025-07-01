package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import org.example.smartmuseum.database.EmployeeDAO;
import org.example.smartmuseum.model.entity.Attendance;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.service.EmployeeService;
import org.example.smartmuseum.util.CameraQRScanner;
import org.example.smartmuseum.util.QRCodeGenerator;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    @FXML private Button btnRefresh;
    @FXML private TextArea txtCustomData;
    @FXML private Label lblTodayDate;

    private ObservableList<AttendanceRecord> attendanceData;
    private CameraQRScanner qrScanner;
    private EmployeeService employeeService;
    private EmployeeDAO employeeDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        employeeService = new EmployeeService();
        employeeDAO = new EmployeeDAO();
        setupTable();
        setupTodayDate();
        loadAttendanceData();

        // Initialize QR scanner
        qrScanner = new CameraQRScanner();

        // Initial state
        btnStopCamera.setDisable(true);
        lblCameraStatus.setText("Camera not started");
        lblScanResult.setText("Ready to scan employee QR codes...");

        // Load and display available employees
        displayAvailableEmployees();
    }

    private void setupTable() {
        attendanceData = FXCollections.observableArrayList();

        // Set up table columns with proper cell value factories
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set preferred column widths
        colEmployeeName.setPrefWidth(150);
        colAction.setPrefWidth(100);
        colTimestamp.setPrefWidth(180);
        colStatus.setPrefWidth(100);

        tableAttendance.setItems(attendanceData);

        // Debug: Print column setup
        System.out.println("Table columns setup completed");
    }

    private void setupTodayDate() {
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        lblTodayDate.setText("📅 Today: " + formattedDate);
    }

    private void displayAvailableEmployees() {
        try {
            List<Employee> employees = employeeDAO.getAllStaffEmployees();
            System.out.println("=== AVAILABLE STAFF EMPLOYEES FOR ATTENDANCE ===");
            if (employees.isEmpty()) {
                System.out.println("No staff employees found in database!");
                lblScanResult.setText("⚠️ No staff employees found. Please add staff employees first.");
            } else {
                for (Employee emp : employees) {
                    System.out.println("- ID: " + emp.getEmployeeId() +
                            ", Name: " + emp.getName() +
                            ", Position: " + emp.getPosition() +
                            ", QR: " + (emp.getQrCode() != null ? emp.getQrCode() : "NOT SET"));
                }
                lblScanResult.setText("📋 " + employees.size() + " staff employees available for attendance");
            }
            System.out.println("=== END EMPLOYEE LIST ===");
        } catch (Exception e) {
            System.err.println("Error loading employees: " + e.getMessage());
            lblScanResult.setText("❌ Error loading employee data");
        }
    }

    private void loadAttendanceData() {
        attendanceData.clear();

        try {
            List<Attendance> todayAttendance = employeeService.getAllTodayAttendance();

            System.out.println("=== TODAY'S ATTENDANCE FROM DATABASE ===");
            System.out.println("Found " + todayAttendance.size() + " attendance records for today");

            for (Attendance attendance : todayAttendance) {
                Employee employee = employeeDAO.getEmployeeById(attendance.getEmployeeId());
                if (employee != null) {
                    String employeeName = employee.getName();
                    String status = attendance.getStatus().getValue();

                    System.out.println("Processing attendance for: " + employeeName);

                    if (attendance.getCheckIn() != null) {
                        String timestamp = attendance.getCheckIn().toLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                        AttendanceRecord checkInRecord = new AttendanceRecord(
                                employeeName, "Check In", timestamp, status);
                        attendanceData.add(checkInRecord);

                        System.out.println("  - Added Check In record: " + employeeName + " at " + timestamp);
                    }

                    if (attendance.getCheckOut() != null) {
                        String timestamp = attendance.getCheckOut().toLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                        AttendanceRecord checkOutRecord = new AttendanceRecord(
                                employeeName, "Check Out", timestamp, "Completed");
                        attendanceData.add(checkOutRecord);

                        System.out.println("  - Added Check Out record: " + employeeName + " at " + timestamp);
                    }
                } else {
                    System.out.println("Employee not found for ID: " + attendance.getEmployeeId());
                }
            }

            // Sort by timestamp (newest first)
            attendanceData.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

            System.out.println("Total attendance records in table: " + attendanceData.size());

            // Debug: Print all records
            for (AttendanceRecord record : attendanceData) {
                System.out.println("Table Record: " + record.getEmployeeName() + " | " +
                        record.getAction() + " | " + record.getTimestamp() + " | " + record.getStatus());
            }

            System.out.println("=== END ATTENDANCE DATA ===");

            // Force table refresh
            Platform.runLater(() -> {
                tableAttendance.refresh();
                System.out.println("Table refreshed with " + attendanceData.size() + " records");
            });

        } catch (Exception e) {
            System.err.println("Error loading attendance data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStartCamera() {
        try {
            qrScanner.startScanning(new CameraQRScanner.QRScanCallback() {
                @Override
                public void onQRCodeDetected(String qrCode) {
                    Platform.runLater(() -> processQRCode(qrCode));
                }

                @Override
                public void onImageUpdate(Image image) {
                    Platform.runLater(() -> cameraView.setImage(image));
                }

                @Override
                public void onError(String error) {
                    Platform.runLater(() -> {
                        lblCameraStatus.setText("Camera Error: " + error);
                        lblScanResult.setText("❌ Error: " + error);
                        handleStopCamera();
                    });
                }
            });

            btnStartCamera.setDisable(true);
            btnStopCamera.setDisable(false);
            lblCameraStatus.setText("📹 Camera active - Scanning for employee QR codes...");
            lblScanResult.setText("🔍 Show employee QR code to camera for automatic attendance tracking...");

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

        processQRCode(qrCode);
        txtQRInput.clear();
    }

    private void processQRCode(String qrCode) {
        try {
            System.out.println("=== PROCESSING QR CODE ===");
            System.out.println("QR Code: " + qrCode);

            // Validate QR code format
            if (!QRCodeGenerator.validateQRCode(qrCode)) {
                lblScanResult.setText("❌ Invalid QR code format");
                System.out.println("Invalid QR code format");
                return;
            }

            // Extract employee ID from QR code
            Integer employeeId = QRCodeGenerator.extractEmployeeId(qrCode);
            if (employeeId == null) {
                lblScanResult.setText("❌ Could not extract employee ID from QR code");
                System.out.println("Could not extract employee ID");
                return;
            }

            System.out.println("Extracted Employee ID: " + employeeId);

            // Process attendance for today only
            EmployeeService.AttendanceResult result = employeeService.processAttendance(employeeId, qrCode);

            System.out.println("Attendance Result: " + result.getMessage());
            System.out.println("Success: " + result.isSuccess());

            // Display result
            lblScanResult.setText(result.getMessage());

            if (result.isSuccess()) {
                // Refresh attendance table
                loadAttendanceData();
                System.out.println("Attendance processed successfully: " + result.getAction());
            } else {
                System.out.println("Attendance processing failed: " + result.getMessage());
            }

            System.out.println("=== END QR PROCESSING ===");

        } catch (Exception e) {
            lblScanResult.setText("❌ Error processing QR code: " + e.getMessage());
            System.err.println("QR processing error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadAttendanceData();
        displayAvailableEmployees();
        setupTodayDate();
        lblScanResult.setText("📊 Today's attendance data refreshed");
    }

    public void refreshData() {
        loadAttendanceData();
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

            // Debug: Print when creating record
            System.out.println("Created AttendanceRecord: " + employeeName + " | " + action + " | " + timestamp + " | " + status);
        }

        // Getters
        public String getEmployeeName() {
            return employeeName;
        }

        public String getAction() {
            return action;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getStatus() {
            return status;
        }

        // Setters
        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "AttendanceRecord{" +
                    "employeeName='" + employeeName + '\'' +
                    ", action='" + action + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }
}
