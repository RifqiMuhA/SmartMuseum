package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class QRAttendanceScannerController implements CameraQRScanner.QRScanCallback {

    // UI Components
    private Button btnCheckIn;
    private Button btnCheckOut;
    private Button btnStartCamera;
    private Button btnStopCamera;
    private Button btnScanQR;
    private Button btnRefresh;

    private TextField txtQRCode;
    private Label lblScanResult;
    private Label lblCurrentTime;
    private Label lblTotalEmployees;
    private Label lblPresentToday;
    private Label lblCameraStatus;

    private ImageView imgCamera;
    private TableView<AttendanceTableData> tableAttendance;
    private TableColumn<AttendanceTableData, String> colEmployeeName;
    private TableColumn<AttendanceTableData, String> colPosition;
    private TableColumn<AttendanceTableData, String> colCheckIn;
    private TableColumn<AttendanceTableData, String> colCheckOut;
    private TableColumn<AttendanceTableData, String> colStatus;
    private TableColumn<AttendanceTableData, String> colWorkHours;

    // Services and Data
    private AttendanceService attendanceService;
    private UserService userService;
    private ObservableList<AttendanceTableData> attendanceData;
    private CameraQRScanner qrScanner;
    private Timer timer;
    private QRCodeLog.ScanType currentScanType = QRCodeLog.ScanType.CHECK_IN;

    public QRAttendanceScannerController() {
        try {
            attendanceService = AttendanceService.getInstance();
            userService = UserService.getInstance();
            attendanceData = FXCollections.observableArrayList();
            qrScanner = new CameraQRScanner();

            System.out.println("✅ QR Attendance Scanner Controller initialized");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error initializing QR Scanner: " + e.getMessage());
        }
    }

    public VBox createScannerUI() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f8f9fa;");

        // Header Section
        HBox header = createHeader();

        // Main Content
        HBox content = new HBox(20);
        VBox scannerSection = createScannerSection();
        VBox tableSection = createTableSection();

        content.getChildren().addAll(scannerSection, tableSection);
        HBox.setHgrow(tableSection, Priority.ALWAYS);

        mainContainer.getChildren().addAll(header, content);

        // Initialize data and timer
        initializeData();
        startTimer();

        return mainContainer;
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #34495e); " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);");

        Label title = new Label("📷 QR Code Attendance Scanner");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblTotalEmployees = new Label("Total Employees: 0");
        lblTotalEmployees.setStyle("-fx-text-fill: #ecf0f1; -fx-font-weight: bold;");

        lblPresentToday = new Label("Present Today: 0");
        lblPresentToday.setStyle("-fx-text-fill: #ecf0f1; -fx-font-weight: bold;");

        lblCurrentTime = new Label("Loading...");
        lblCurrentTime.setStyle("-fx-text-fill: #ecf0f1; -fx-font-weight: bold;");

        header.getChildren().addAll(title, spacer, lblTotalEmployees,
                new Separator(), lblPresentToday,
                new Separator(), lblCurrentTime);

        return header;
    }

    private VBox createScannerSection() {
        VBox scanner = new VBox(15);
        scanner.setPrefWidth(400);
        scanner.setPadding(new Insets(20));
        scanner.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #bdc3c7; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        Label scannerTitle = new Label("QR Code Scanner");
        scannerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Scan mode buttons
        HBox modeButtons = new HBox(10);
        modeButtons.setAlignment(Pos.CENTER);

        btnCheckIn = new Button("Check In Mode");
        btnCheckIn.setPrefSize(120, 40);
        btnCheckIn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; " +
                "-fx-border-color: #1e8449; -fx-border-width: 3;");
        btnCheckIn.setOnAction(e -> handleCheckIn());

        btnCheckOut = new Button("Check Out Mode");
        btnCheckOut.setPrefSize(120, 40);
        btnCheckOut.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6;");
        btnCheckOut.setOnAction(e -> handleCheckOut());

        modeButtons.getChildren().addAll(btnCheckIn, btnCheckOut);

        // Camera view
        StackPane cameraContainer = new StackPane();
        cameraContainer.setPrefSize(350, 260);
        cameraContainer.setStyle("-fx-border-color: #3498db; -fx-border-width: 2; " +
                "-fx-border-radius: 10; -fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 10;");

        imgCamera = new ImageView();
        imgCamera.setFitWidth(330);
        imgCamera.setFitHeight(240);
        imgCamera.setPreserveRatio(true);

        lblCameraStatus = new Label("Click 'Start Camera' to begin scanning");
        lblCameraStatus.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic; " +
                "-fx-background-color: rgba(255,255,255,0.9); " +
                "-fx-background-radius: 5; -fx-padding: 10;");

        cameraContainer.getChildren().addAll(imgCamera, lblCameraStatus);

        // Camera controls
        HBox cameraControls = new HBox(10);
        cameraControls.setAlignment(Pos.CENTER);

        btnStartCamera = new Button("Start Camera");
        btnStartCamera.setPrefSize(100, 40);
        btnStartCamera.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6;");
        btnStartCamera.setOnAction(e -> handleStartCamera());

        btnStopCamera = new Button("Stop Camera");
        btnStopCamera.setPrefSize(100, 40);
        btnStopCamera.setDisable(true);
        btnStopCamera.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6;");
        btnStopCamera.setOnAction(e -> handleStopCamera());

        cameraControls.getChildren().addAll(btnStartCamera, btnStopCamera);

        // Manual QR input
        Separator separator = new Separator();

        Label manualLabel = new Label("Or enter QR Code manually:");
        manualLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        txtQRCode = new TextField();
        txtQRCode.setPromptText("Enter QR Code here...");
        txtQRCode.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        txtQRCode.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                handleScanQR();
            }
        });

        btnScanQR = new Button("Process QR Code");
        btnScanQR.setPrefHeight(35);
        btnScanQR.setMaxWidth(Double.MAX_VALUE);
        btnScanQR.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6;");
        btnScanQR.setOnAction(e -> handleScanQR());

        // Scan result
        lblScanResult = new Label("Select scan mode and start camera or enter QR code");
        lblScanResult.setAlignment(Pos.CENTER);
        lblScanResult.setPrefHeight(60);
        lblScanResult.setMaxWidth(Double.MAX_VALUE);
        lblScanResult.setWrapText(true);
        lblScanResult.setStyle("-fx-background-color: #ecf0f1; " +
                "-fx-border-color: #bdc3c7; -fx-border-width: 1; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-padding: 10; -fx-text-fill: #2c3e50; " +
                "-fx-font-weight: bold;");

        scanner.getChildren().addAll(scannerTitle, modeButtons, cameraContainer,
                cameraControls, separator, manualLabel,
                txtQRCode, btnScanQR, lblScanResult);

        return scanner;
    }

    private VBox createTableSection() {
        VBox tableSection = new VBox(15);
        tableSection.setPadding(new Insets(20));
        tableSection.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #bdc3c7; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        HBox tableHeader = new HBox(15);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("Today's Attendance");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 5;");
        btnRefresh.setOnAction(e -> handleRefresh());

        tableHeader.getChildren().addAll(tableTitle, spacer, btnRefresh);

        // Create table
        tableAttendance = new TableView<>();
        tableAttendance.setItems(attendanceData);

        colEmployeeName = new TableColumn<>("Employee Name");
        colEmployeeName.setPrefWidth(150);
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));

        colPosition = new TableColumn<>("Position");
        colPosition.setPrefWidth(120);
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));

        colCheckIn = new TableColumn<>("Check In");
        colCheckIn.setPrefWidth(100);
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));

        colCheckOut = new TableColumn<>("Check Out");
        colCheckOut.setPrefWidth(100);
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));

        colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(90);
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colWorkHours = new TableColumn<>("Work Hours");
        colWorkHours.setPrefWidth(100);
        colWorkHours.setCellValueFactory(new PropertyValueFactory<>("workHours"));

        // Style status column
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

        tableAttendance.getColumns().addAll(colEmployeeName, colPosition, colCheckIn,
                colCheckOut, colStatus, colWorkHours);

        VBox.setVgrow(tableAttendance, Priority.ALWAYS);

        tableSection.getChildren().addAll(tableHeader, tableAttendance);

        return tableSection;
    }

    // Event Handlers
    private void handleCheckIn() {
        currentScanType = QRCodeLog.ScanType.CHECK_IN;
        updateScanTypeButtons();
        showMessage("Mode: CHECK IN - Scan QR Code untuk masuk", "info");
        txtQRCode.requestFocus();
    }

    private void handleCheckOut() {
        currentScanType = QRCodeLog.ScanType.CHECK_OUT;
        updateScanTypeButtons();
        showMessage("Mode: CHECK OUT - Scan QR Code untuk pulang", "info");
        txtQRCode.requestFocus();
    }

    private void updateScanTypeButtons() {
        String baseStyle = "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6;";

        if (currentScanType == QRCodeLog.ScanType.CHECK_IN) {
            btnCheckIn.setStyle(baseStyle + "-fx-background-color: #27ae60; -fx-text-fill: white; " +
                    "-fx-border-color: #1e8449; -fx-border-width: 3;");
            btnCheckOut.setStyle(baseStyle + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
        } else {
            btnCheckIn.setStyle(baseStyle + "-fx-background-color: #27ae60; -fx-text-fill: white;");
            btnCheckOut.setStyle(baseStyle + "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                    "-fx-border-color: #c0392b; -fx-border-width: 3;");
        }
    }

    private void handleStartCamera() {
        try {
            showMessage("Starting camera...", "info");
            qrScanner.startScanning(this);

            btnStartCamera.setDisable(true);
            btnStopCamera.setDisable(false);
            lblCameraStatus.setText("Camera is running - Point QR code to camera");

            System.out.println("✅ Camera started successfully");

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error starting camera: " + e.getMessage(), "error");
        }
    }

    private void handleStopCamera() {
        try {
            qrScanner.stopScanning();

            btnStartCamera.setDisable(false);
            btnStopCamera.setDisable(true);
            imgCamera.setImage(null);
            lblCameraStatus.setText("Camera stopped - Click 'Start Camera' to begin scanning");

            showMessage("Camera stopped", "info");
            System.out.println("🛑 Camera stopped");

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error stopping camera: " + e.getMessage(), "error");
        }
    }

    private void handleScanQR() {
        String qrCode = txtQRCode.getText().trim();
        if (qrCode.isEmpty()) {
            showMessage("QR Code tidak boleh kosong!", "error");
            return;
        }

        btnScanQR.setDisable(true);

        try {
            showMessage("Processing QR Code...", "info");

            AttendanceService.AttendanceResult result = attendanceService.scanQRCode(qrCode, currentScanType);

            if (result.isSuccess()) {
                showMessage(result.getMessage(), "success");
                loadTodayAttendance();
                updateStatusLabels();
                txtQRCode.clear();
            } else {
                showMessage(result.getMessage(), "error");
            }

        } catch (Exception e) {
            showMessage("System error: " + e.getMessage(), "error");
            e.printStackTrace();
        } finally {
            Platform.runLater(() -> {
                btnScanQR.setDisable(false);
                txtQRCode.requestFocus();
            });
        }
    }

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

    // Camera QR Scanner Callback Implementation
    @Override
    public void onQRCodeDetected(String qrCode) {
        Platform.runLater(() -> {
            txtQRCode.setText(qrCode);
            handleScanQR();
        });
    }

    @Override
    public void onImageUpdate(Image image) {
        Platform.runLater(() -> {
            imgCamera.setImage(image);
            lblCameraStatus.setText("Scanning for QR codes...");
        });
    }

    @Override
    public void onError(String error) {
        Platform.runLater(() -> {
            showMessage("Camera error: " + error, "error");
            handleStopCamera();
        });
    }

    // Data Management
    private void initializeData() {
        loadTodayAttendance();
        updateStatusLabels();
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

    private void startTimer() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    lblCurrentTime.setText(LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                });
            }
        }, 0, 1000);
    }

    private void showMessage(String message, String type) {
        Platform.runLater(() -> {
            lblScanResult.setText(message);

            switch (type.toLowerCase()) {
                case "success":
                    lblScanResult.setStyle("-fx-background-color: #d5f4e6; " +
                            "-fx-border-color: #27ae60; -fx-border-width: 1; " +
                            "-fx-border-radius: 6; -fx-background-radius: 6; " +
                            "-fx-padding: 10; -fx-text-fill: #27ae60; " +
                            "-fx-font-weight: bold;");
                    break;
                case "error":
                    lblScanResult.setStyle("-fx-background-color: #ffcccc; " +
                            "-fx-border-color: #e74c3c; -fx-border-width: 1; " +
                            "-fx-border-radius: 6; -fx-background-radius: 6; " +
                            "-fx-padding: 10; -fx-text-fill: #e74c3c; " +
                            "-fx-font-weight: bold;");
                    break;
                case "info":
                    lblScanResult.setStyle("-fx-background-color: #d6eaf8; " +
                            "-fx-border-color: #3498db; -fx-border-width: 1; " +
                            "-fx-border-radius: 6; -fx-background-radius: 6; " +
                            "-fx-padding: 10; -fx-text-fill: #3498db; " +
                            "-fx-font-weight: bold;");
                    break;
                default:
                    lblScanResult.setStyle("-fx-background-color: #ecf0f1; " +
                            "-fx-border-color: #bdc3c7; -fx-border-width: 1; " +
                            "-fx-border-radius: 6; -fx-background-radius: 6; " +
                            "-fx-padding: 10; -fx-text-fill: #2c3e50;");
            }
        });
    }

    public void shutdown() {
        if (timer != null) {
            timer.cancel();
        }
        if (qrScanner != null) {
            qrScanner.shutdown();
        }
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