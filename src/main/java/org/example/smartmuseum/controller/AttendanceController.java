package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.smartmuseum.model.entity.*;
import org.example.smartmuseum.model.service.AttendanceService;
import org.example.smartmuseum.model.service.UserService;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class AttendanceController implements Initializable {

    @FXML private Button btnCheckIn;
    @FXML private Button btnCheckOut;
    @FXML private Button btnRefresh;
    @FXML private Button btnViewReports;
    @FXML private Button btnScanQR;

    @FXML private TextField txtQRCode;
    @FXML private Label lblScanResult;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblTotalEmployees;
    @FXML private Label lblPresentToday;

    @FXML private TableView<AttendanceTableData> tableAttendance;
    @FXML private TableColumn<AttendanceTableData, String> colEmployeeName;
    @FXML private TableColumn<AttendanceTableData, String> colPosition;
    @FXML private TableColumn<AttendanceTableData, String> colCheckIn;
    @FXML private TableColumn<AttendanceTableData, String> colCheckOut;
    @FXML private TableColumn<AttendanceTableData, String> colStatus;
    @FXML private TableColumn<AttendanceTableData, String> colWorkHours;

    private AttendanceService attendanceService;
    private UserService userService;
    private ObservableList<AttendanceTableData> attendanceData;
    private Timer timer;
    private QRCodeLog.ScanType currentScanType = QRCodeLog.ScanType.CHECK_IN;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize services
            attendanceService = AttendanceService.getInstance();
            userService = UserService.getInstance();
            attendanceData = FXCollections.observableArrayList();

            // Initialize UI components
            initializeTable();
            initializeTimer();
            loadTodayAttendance();
            updateStatusLabels();
            updateScanTypeButtons();

            // Set initial messages
            lblScanResult.setText("Pilih mode Check In/Out, lalu scan QR Code");

            System.out.println("✅ AttendanceController initialized successfully");

        } catch (Exception e) {
            showMessage("Error initializing application: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private void initializeTable() {
        // Configure table columns
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colWorkHours.setCellValueFactory(new PropertyValueFactory<>("workHours"));

        // Style status column with colors
        colStatus.setCellFactory(column -> new TableCell<AttendanceTableData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "present":
                            setStyle("-fx-background-color: #d5f4e6; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "late":
                            setStyle("-fx-background-color: #ffeaa7; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "overtime":
                            setStyle("-fx-background-color: #ddd6fe; -fx-text-fill: #9b59b6; -fx-font-weight: bold;");
                            break;
                        case "absent":
                            setStyle("-fx-background-color: #ffcccc; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #2c3e50;");
                    }
                }
            }
        });

        // Set table data
        tableAttendance.setItems(attendanceData);

        // Table styling
        tableAttendance.setRowFactory(tv -> {
            TableRow<AttendanceTableData> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else if (newItem.getStatus().equals("Absent")) {
                    row.setStyle("-fx-background-color: #ffebee;");
                } else if (newItem.getCheckOutTime().equals("-")) {
                    row.setStyle("-fx-background-color: #e8f5e8;");
                }
            });
            return row;
        });

        System.out.println("✅ Table initialized");
    }

    private void initializeTimer() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    lblCurrentTime.setText("Current Time: " +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                });
            }
        }, 0, 1000); // Update every second

        System.out.println("✅ Timer initialized");
    }

    @FXML
    private void handleCheckIn() {
        currentScanType = QRCodeLog.ScanType.CHECK_IN;
        updateScanTypeButtons();
        showMessage("Mode: CHECK IN - Scan QR Code untuk masuk", "info");
        txtQRCode.requestFocus();
    }

    @FXML
    private void handleCheckOut() {
        currentScanType = QRCodeLog.ScanType.CHECK_OUT;
        updateScanTypeButtons();
        showMessage("Mode: CHECK OUT - Scan QR Code untuk pulang", "info");
        txtQRCode.requestFocus();
    }

    private void updateScanTypeButtons() {
        // Reset styles
        String baseStyle = "-fx-font-weight: bold; -fx-padding: 8 16;";

        if (currentScanType == QRCodeLog.ScanType.CHECK_IN) {
            btnCheckIn.setStyle(baseStyle + "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-color: #1e8449; -fx-border-width: 3;");
            btnCheckOut.setStyle(baseStyle + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
        } else {
            btnCheckIn.setStyle(baseStyle + "-fx-background-color: #27ae60; -fx-text-fill: white;");
            btnCheckOut.setStyle(baseStyle + "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-color: #c0392b; -fx-border-width: 3;");
        }
    }

    @FXML
    private void handleScanQR() {
        String qrCode = txtQRCode.getText().trim();
        if (qrCode.isEmpty()) {
            showMessage("QR Code tidak boleh kosong!", "error");
            return;
        }

        // Disable button to prevent multiple clicks
        btnScanQR.setDisable(true);

        try {
            showMessage("Processing QR Code...", "info");

            AttendanceService.AttendanceResult result = attendanceService.scanQRCode(qrCode, currentScanType);

            if (result.isSuccess()) {
                showMessage(result.getMessage(), "success");
                loadTodayAttendance();
                updateStatusLabels();
                txtQRCode.clear();

                // Auto-refresh table after successful scan
                Platform.runLater(() -> {
                    try {
                        Thread.sleep(500); // Small delay for UI update
                        loadTodayAttendance();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

            } else {
                showMessage(result.getMessage(), "error");
            }

        } catch (Exception e) {
            showMessage("System error: " + e.getMessage(), "error");
            e.printStackTrace();
        } finally {
            // Re-enable button
            Platform.runLater(() -> {
                btnScanQR.setDisable(false);
                txtQRCode.requestFocus();
            });
        }
    }

    @FXML
    private void handleRefresh() {
        try {
            showMessage("Refreshing data...", "info");
            loadTodayAttendance();
            updateStatusLabels();
            showMessage("Data berhasil direfresh", "success");
        } catch (Exception e) {
            showMessage("Error refreshing data: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewReports() {
        showMessage("Fitur laporan akan segera tersedia", "info");
        // TODO: Implement reports view
    }

    private void loadTodayAttendance() {
        try {
            List<Employee> employees = userService.getAllEmployees();
            attendanceData.clear();

            LocalDate today = LocalDate.now();

            for (Employee employee : employees) {
                Attendance attendance = attendanceService.getTodayAttendance(employee.getEmployeeId(), today);

                AttendanceTableData data = new AttendanceTableData();
                data.setEmployeeName(employee.getName());
                data.setPosition(employee.getPosition());

                if (attendance != null) {
                    data.setCheckInTime(attendance.getCheckIn() != null ?
                            attendance.getCheckIn().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "-");
                    data.setCheckOutTime(attendance.getCheckOut() != null ?
                            attendance.getCheckOut().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "-");
                    data.setStatus(attendance.getStatus().getDisplayName());
                    data.setWorkHours(attendance.isComplete() ?
                            String.format("%.2f jam", attendance.getWorkHours()) :
                            (attendance.isCheckedIn() ? "Working..." : "-"));
                } else {
                    data.setCheckInTime("-");
                    data.setCheckOutTime("-");
                    data.setStatus("Absent");
                    data.setWorkHours("-");
                }

                attendanceData.add(data);
            }

            System.out.println("✅ Loaded attendance for " + employees.size() + " employees");

        } catch (Exception e) {
            showMessage("Error loading attendance data: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private void updateStatusLabels() {
        try {
            List<Employee> employees = userService.getAllEmployees();
            lblTotalEmployees.setText("Total Employees: " + employees.size());

            long presentCount = attendanceData.stream()
                    .filter(data -> !data.getStatus().equals("Absent"))
                    .count();

            long workingCount = attendanceData.stream()
                    .filter(data -> data.getWorkHours().equals("Working..."))
                    .count();

            lblPresentToday.setText(String.format("Present: %d | Working: %d", presentCount, workingCount));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String message, String type) {
        Platform.runLater(() -> {
            lblScanResult.setText(message);

            switch (type.toLowerCase()) {
                case "success":
                    lblScanResult.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    break;
                case "error":
                    lblScanResult.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    break;
                case "info":
                    lblScanResult.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                    break;
                default:
                    lblScanResult.setStyle("-fx-text-fill: #2c3e50;");
            }
        });
    }

    // Handle Enter key press in QR code field
    @FXML
    private void handleQRCodeKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            handleScanQR();
        }
    }

    public void shutdown() {
        if (timer != null) {
            timer.cancel();
        }
        if (attendanceService != null) {
            attendanceService.shutdown();
        }
        System.out.println("✅ AttendanceController shutdown complete");
    }

    // Inner class for table data
    public static class AttendanceTableData {
        private String employeeName;
        private String position;
        private String checkInTime;
        private String checkOutTime;
        private String status;
        private String workHours;

        // Getters and setters
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }

        public String getCheckInTime() { return checkInTime; }
        public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

        public String getCheckOutTime() { return checkOutTime; }
        public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getWorkHours() { return workHours; }
        public void setWorkHours(String workHours) { this.workHours = workHours; }
    }
}