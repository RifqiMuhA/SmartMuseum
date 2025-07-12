package org.example.smartmuseum.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.entity.Auction;
import org.example.smartmuseum.model.entity.Artwork;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.entity.Attendance;
import org.example.smartmuseum.model.enums.UserRole;
import org.example.smartmuseum.util.SessionContext;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.time.Duration;

import org.example.smartmuseum.model.service.ArtworkService;
import org.example.smartmuseum.model.service.AuctionService;
import org.example.smartmuseum.model.service.UserService;
import org.example.smartmuseum.model.service.EmployeeService;
import org.example.smartmuseum.database.DatabaseManager;

public class DashboardController implements Initializable, SessionAwareController {

    // Header elements
    @FXML private Label lblPageTitle;
    @FXML private Label lblPageSubtitle;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblUserInfo;
    @FXML private Label lblPositionInfo;
    @FXML private ImageView imgLogo;

    // Navigation buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnAttendance;
    @FXML private Button btnEmployees;
    @FXML private Button btnUsers;
    @FXML private Button btnArtworks;
    @FXML private Button btnAuctions;
    @FXML private Button btnQRGenerator;
    @FXML private Button btnProfile;
    @FXML private Button btnVideoRoom;

    // Content area
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardContent;
    @FXML private VBox activityList;

    // Statistics labels
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalArtworks;
    @FXML private Label lblActiveAuctions;
    @FXML private Label lblTodayAttendance;

    // Charts
    @FXML private PieChart artworkTypeChart;
    @FXML private BarChart<String, Number> attendanceBarChart;
    @FXML private CategoryAxis attendanceXAxis;
    @FXML private NumberAxis attendanceYAxis;

    private Timer clockTimer;
    private Button currentActiveButton;

    // Services
    private UserService userService;
    private ArtworkService artworkService;
    private AuctionService auctionService;
    private EmployeeService employeeService;
    private DatabaseManager databaseManager;

    // Session Context
    private SessionContext sessionContext;

    // Class helper buat ambil activity terkini
    private static class ActivityItem {
        private final LocalDateTime timestamp;
        private final String icon;
        private final String text;
        private final String timeAgo;

        public ActivityItem(LocalDateTime timestamp, String icon, String text, String timeAgo) {
            this.timestamp = timestamp;
            this.icon = icon;
            this.text = text;
            this.timeAgo = timeAgo;
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public String getIcon() { return icon; }
        public String getText() { return text; }
        public String getTimeAgo() { return timeAgo; }
    }

    @Override
    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
        System.out.println("DashboardController received session context: " + sessionContext);
    }

    @Override
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize Services
        userService = new UserService();
        artworkService = new ArtworkService();
        auctionService = new AuctionService();
        employeeService = new EmployeeService();
        databaseManager = new DatabaseManager();

        loadLogo();
        setupClock();
        setupNavigation();
        setupCharts();

        // Set dashboard as active and use modern styling
        currentActiveButton = btnDashboard;
        if (btnDashboard != null) {
            btnDashboard.getStyleClass().add("nav-item-active-modern");
        }
        showDashboard();

        // Delay untuk memastikan sessionContext sudah di-set
        Platform.runLater(() -> {
            loadUserInfo();

            // Apply role-based access control
            applyRoleBasedAccess();

            loadDashboardData();

            // Set dashboard to full screen after everything is loaded
            try {
                if (lblUserInfo != null && lblUserInfo.getScene() != null && lblUserInfo.getScene().getWindow() != null) {
                    Stage stage = (Stage) lblUserInfo.getScene().getWindow();
                    stage.setMaximized(true);

                    // Set stage di session context
                    if (sessionContext != null) {
                        sessionContext.setStage(stage);
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not set full screen: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void applyRoleBasedAccess() {
        if (sessionContext != null && sessionContext.getSessionManager() != null) {
            User currentUser = sessionContext.getSessionManager().getCurrentUser();

            if (currentUser != null) {
                UserRole userRole = currentUser.getRole();

                // Hide buttons that STAFF cannot access
                if (userRole == UserRole.STAFF) {
                    // Hide Employee Management button
                    if (btnEmployees != null) {
                        btnEmployees.setVisible(false);
                        btnEmployees.setManaged(false);
                    }

                    // Hide QR Generator button
                    if (btnQRGenerator != null) {
                        btnQRGenerator.setVisible(false);
                        btnQRGenerator.setManaged(false);
                    }

                    // Update page title and subtitle for staff
                    if (lblPageTitle != null) {
                        lblPageTitle.setText("Staff Dashboard");
                    }
                    if (lblPageSubtitle != null) {
                        lblPageSubtitle.setText("Sistem manajemen museum untuk staff");
                    }

                    System.out.println("✅ Staff access restrictions applied");
                }
                // For BOSS role, show all buttons (default behavior)
                else if (userRole == UserRole.BOSS) {
                    // Ensure all buttons are visible for boss
                    if (btnEmployees != null) {
                        btnEmployees.setVisible(true);
                        btnEmployees.setManaged(true);
                    }

                    if (btnQRGenerator != null) {
                        btnQRGenerator.setVisible(true);
                        btnQRGenerator.setManaged(true);
                    }

                    System.out.println("✅ Boss access - all features available");
                }
            }
        }
    }

    private void loadUserInfo() {
        if (sessionContext != null && sessionContext.getSessionManager() != null) {
            User currentUser = sessionContext.getSessionManager().getCurrentUser();
            if (currentUser != null && lblUserInfo != null) {

                // Load employee data untuk mendapatkan name dan position
                try {
                    // Find employee by user_id or username
                    Employee currentEmployee = findEmployeeForUser(currentUser);

                    if (currentEmployee != null) {
                        // Display employee name and position
                        lblUserInfo.setText(currentEmployee.getName());

                        // Update role label to show position
                        updateSidebarRoleLabel(currentEmployee.getPosition());

                        System.out.println("Loaded employee info: " + currentEmployee.getName() +
                                " - " + currentEmployee.getPosition());
                    } else {
                        // Fallback to username if no employee record
                        lblUserInfo.setText(currentUser.getUsername());
                        updateSidebarRoleLabel(currentUser.getRole().getValue());
                        System.out.println("No employee record found, using username: " + currentUser.getUsername());
                    }

                } catch (Exception e) {
                    System.err.println("Error loading employee info: " + e.getMessage());
                    lblUserInfo.setText(currentUser.getUsername());
                    updateSidebarRoleLabel(currentUser.getRole().getValue());
                }

                System.out.println("Loaded user info for session: " + sessionContext.getSessionManager().getSessionId());
            } else if (lblUserInfo != null) {
                lblUserInfo.setText("Guest User");
            }
        } else {
            System.err.println("No session context available in DashboardController");
            if (lblUserInfo != null) {
                lblUserInfo.setText("No Session");
            }
        }
    }

    private Employee findEmployeeForUser(User user) {
        try {
            List<Employee> allEmployees = employeeService.getAllEmployees();

            // First try to find by user_id
            for (Employee emp : allEmployees) {
                if (emp.getUserId() == user.getUserId()) {
                    return emp;
                }
            }

            // If not found by user_id, try to find by username similarity
            for (Employee emp : allEmployees) {
                if (emp.getName() != null &&
                        (emp.getName().toLowerCase().contains(user.getUsername().toLowerCase()) ||
                                user.getUsername().toLowerCase().contains(emp.getName().toLowerCase()))) {
                    return emp;
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error finding employee for user: " + e.getMessage());
            return null;
        }
    }

    private void updateSidebarRoleLabel(String position) {
        Platform.runLater(() -> {
            if (lblPositionInfo != null) {
                lblPositionInfo.setText(position != null ? position : "Staff Member");
            }
        });
    }

    private String getTimeAgo(LocalDateTime pastTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(pastTime, now);

        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + (days == 1 ? " hari yang lalu" : " hari yang lalu");
        } else if (hours > 0) {
            return hours + (hours == 1 ? " jam yang lalu" : " jam yang lalu");
        } else if (minutes > 0) {
            return minutes + (minutes == 1 ? " menit yang lalu" : " menit yang lalu");
        } else {
            return "Baru saja";
        }
    }

    private void setupCharts() {
        // Setup Pie Chart
        if (artworkTypeChart != null) {
            artworkTypeChart.setTitle("");
            artworkTypeChart.setLegendVisible(true);
            artworkTypeChart.setLabelsVisible(true);
        }

        // Setup Bar Chart dengan Y-axis bilangan bulat
        if (attendanceBarChart != null) {
            attendanceBarChart.setTitle("");
            attendanceBarChart.setLegendVisible(false);
            attendanceXAxis.setLabel("Tanggal");
            attendanceYAxis.setLabel("Jumlah Pegawai");

            // Set Y-axis untuk menampilkan hanya bilangan bulat
            attendanceYAxis.setTickUnit(1.0);  // Interval 1
            attendanceYAxis.setMinorTickVisible(false);  // Hilangkan tick minor
            attendanceYAxis.setAutoRanging(true);

            // Format angka menjadi bilangan bulat (tanpa desimal)
            attendanceYAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(attendanceYAxis) {
                @Override
                public String toString(Number value) {
                    return String.valueOf(value.intValue());
                }
            });
        }
    }

    private void loadArtworkTypeChart() {
        if (artworkTypeChart != null) {
            try {
                List<Artwork> artworks = artworkService.getAllArtworks();

                Map<String, Long> typeCount = artworks.stream()
                        .collect(Collectors.groupingBy(
                                artwork -> artwork.getArtworkType() != null ? artwork.getArtworkType() : "Unknown",
                                Collectors.counting()
                        ));

                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

                typeCount.forEach((type, count) -> {
                    pieChartData.add(new PieChart.Data(type + " (" + count + ")", count));
                });

                artworkTypeChart.setData(pieChartData);
                System.out.println("Artwork type chart loaded with " + pieChartData.size() + " categories");

            } catch (Exception e) {
                System.err.println("Error loading artwork type chart: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadAttendanceChart() {
        if (attendanceBarChart != null) {
            try {
                LocalDate now = LocalDate.now();
                LocalDate startOfMonth = now.withDayOfMonth(1);

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Presensi Harian");

                Map<LocalDate, Integer> attendanceData = employeeService.getMonthlyAttendanceData(startOfMonth, now);

                int daysToShow = Math.min(10, now.getDayOfMonth());
                for (int i = daysToShow; i >= 1; i--) {
                    LocalDate date = now.minusDays(i - 1);
                    int count = attendanceData.getOrDefault(date, 0);
                    String dateStr = date.format(DateTimeFormatter.ofPattern("dd"));
                    series.getData().add(new XYChart.Data<>(dateStr, count));
                }

                attendanceBarChart.getData().clear();
                attendanceBarChart.getData().add(series);

                System.out.println("Attendance chart loaded with " + series.getData().size() + " data points");

            } catch (Exception e) {
                System.err.println("Error loading attendance chart: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Map<LocalDate, Integer> getMonthlyAttendanceData(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Integer> attendanceData = new HashMap<>();

        try {
            // Get all employees
            List<Employee> employees = employeeService.getAllEmployees();

            // For each day in the range, count attendance
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                int count = 0;

                // For each employee, check if they attended on this date
                for (Employee employee : employees) {
                    List<Attendance> employeeAttendance = employeeService.getEmployeeAttendance(employee.getEmployeeId());

                    final LocalDate checkDate = currentDate;
                    boolean attended = employeeAttendance.stream()
                            .anyMatch(attendance -> attendance.getDate().toLocalDate().equals(checkDate));

                    if (attended) {
                        count++;
                    }
                }

                attendanceData.put(currentDate, count);
                currentDate = currentDate.plusDays(1);
            }

        } catch (Exception e) {
            System.err.println("Error getting monthly attendance data: " + e.getMessage());
        }

        return attendanceData;
    }

    private void loadLogo() {
        try {
            URL logoUrl = getClass().getResource("/img/logo-putih.png");
            Image logoImage = new Image(logoUrl.toExternalForm());

            if (logoImage != null && !logoImage.isError()) {
                imgLogo.setImage(logoImage);
            }
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
        }
    }

    private void setupClock() {
        if (lblCurrentTime != null) {
            clockTimer = new Timer(true);
            clockTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (lblCurrentTime != null) {
                            String currentTime = LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                            lblCurrentTime.setText(currentTime);
                        }
                    });
                }
            }, 0, 1000);
        }
    }

    private void setupNavigation() {
        Button[] navButtons = {btnDashboard, btnAttendance, btnEmployees, btnUsers,
                btnArtworks, btnAuctions, btnQRGenerator, btnProfile};

        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().removeAll("nav-item-active", "nav-item-active-modern");
            }
        }
    }

    private void setActiveNavButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().removeAll("nav-item-active", "nav-item-active-modern");
        }
        if (button != null) {
            button.getStyleClass().add("nav-item-active-modern");
            currentActiveButton = button;
        }
    }

    private void loadDashboardData() {
        Platform.runLater(() -> {
            try {
                // Load real data only - no fallbacks
                int totalUsers = userService.getTotalUserCount();
                List<Artwork> artworks = artworkService.getAllArtworks();
                int activeAuctions = auctionService.getActiveAuctionCount();
                int todayAttendance = employeeService.getTodayAttendanceCount();

                // Update labels
                if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(totalUsers));
                if (lblTotalArtworks != null) lblTotalArtworks.setText(String.valueOf(artworks.size()));
                if (lblActiveAuctions != null) lblActiveAuctions.setText(String.valueOf(activeAuctions));
                if (lblTodayAttendance != null) lblTodayAttendance.setText(String.valueOf(todayAttendance));

                // Load charts
                loadArtworkTypeChart();
                loadAttendanceChart();

                // Load recent activity
                loadRecentActivity();
            } catch (Exception e) {
                System.err.println("Error loading dashboard data: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void loadRecentActivity() {
        if (activityList != null) {
            activityList.getChildren().clear();

            try {
                List<ActivityItem> allActivities = new ArrayList<>();

                // Get recent artworks (limit 3)
                List<Artwork> recentArtworks = artworkService.getAllArtworks();
                int artworkCount = 0;
                for (Artwork artwork : recentArtworks) {
                    if (artworkCount >= 2) break; // Limit to 2 artworks

                    if (artwork.getCreatedAt() != null) {
                        LocalDateTime createdTime = artwork.getCreatedAt().toLocalDateTime();
                        String timeAgo = getTimeAgo(createdTime);

                        ActivityItem item = new ActivityItem(
                                createdTime,
                                "🎨",
                                "Karya seni ditambahkan: " + artwork.getTitle(),
                                timeAgo
                        );
                        allActivities.add(item);
                        artworkCount++;
                    }
                }

                // Get recent check-ins (limit 2)
                List<Attendance> recentCheckIns = employeeService.getRecentCheckIns(2);
                for (Attendance attendance : recentCheckIns) {
                    LocalDateTime checkInTime = attendance.getCheckIn().toLocalDateTime();
                    String timeAgo = getTimeAgo(checkInTime);

                    Employee employee = employeeService.getEmployee(attendance.getEmployeeId());
                    String employeeName = employee != null ? employee.getName() : "Pegawai #" + attendance.getEmployeeId();

                    ActivityItem item = new ActivityItem(
                            checkInTime,
                            "✅",
                            employeeName + " melakukan check-in",
                            timeAgo
                    );
                    allActivities.add(item);
                }

                // Get recent employee (limit 1)
                List<Employee> recentEmployees = employeeService.getRecentEmployees(1);
                for (Employee employee : recentEmployees) {
                    // Use current time minus some hours as placeholder for employee creation time
                    LocalDateTime employeeTime = LocalDateTime.now().minusHours(8);
                    String timeAgo = getTimeAgo(employeeTime);

                    ActivityItem item = new ActivityItem(
                            employeeTime,
                            "👥",
                            "Pegawai terdaftar: " + employee.getName(),
                            timeAgo
                    );
                    allActivities.add(item);
                }

                // Sort by timestamp (newest first)
                allActivities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

                // Add to UI
                for (ActivityItem activity : allActivities) {
                    HBox activityItem = createActivityItem(
                            activity.getIcon(),
                            activity.getText(),
                            activity.getTimeAgo()
                    );
                    activityList.getChildren().add(activityItem);
                }

            } catch (Exception e) {
                System.err.println("Error loading recent activity: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private HBox createActivityItem(String icon, String text, String time) {
        HBox item = new HBox();
        item.setSpacing(15.0);
        item.getStyleClass().add("activity-item");

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("activity-icon");

        VBox textBox = new VBox();
        textBox.setSpacing(2.0);

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("activity-text");

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("activity-time");

        textBox.getChildren().addAll(textLabel, timeLabel);
        item.getChildren().addAll(iconLabel, textBox);

        return item;
    }

    @FXML
    private void showDashboard() {
        setActiveNavButton(btnDashboard);
        if (lblPageTitle != null) lblPageTitle.setText("Dashboard");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Selamat datang di sistem manajemen museum digital");

        if (dashboardContent != null) {
            showContent(dashboardContent);
        }
        loadDashboardData();
    }

    @FXML
    private void showAttendance() {
        if (!hasPermission("view_attendance")) {
            showAccessDeniedAlert("Attendance Scanner", "Anda tidak memiliki akses untuk melihat attendance.");
            return;
        }

        setActiveNavButton(btnAttendance);
        if (lblPageTitle != null) lblPageTitle.setText("Attendance Scanner");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Scan QR codes for employee attendance tracking");

        loadAndShowContent("/org/example/smartmuseum/fxml/attendance-scanner.fxml");
    }

    @FXML
    private void showEmployees() {
        // Check if user has permission
        if (!hasPermission("manage_employees")) {
            showAccessDeniedAlert("Employee Management", "Anda tidak memiliki akses untuk mengelola pegawai.");
            return;
        }

        setActiveNavButton(btnEmployees);
        if (lblPageTitle != null) lblPageTitle.setText("Employee Management");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Manage employee information and QR codes");

        loadAndShowContent("/org/example/smartmuseum/fxml/employee-management.fxml");
    }

    @FXML
    private void showUsers() {
        if (!hasPermission("manage_users")) {
            showAccessDeniedAlert("User Management", "Anda tidak memiliki akses untuk mengelola user.");
            return;
        }

        setActiveNavButton(btnUsers);
        if (lblPageTitle != null) lblPageTitle.setText("User Management");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Manage system users and their roles");

        loadAndShowContent("/org/example/smartmuseum/fxml/user-management.fxml");
    }

    @FXML
    public void showArtworks() {
        if (!hasPermission("manage_artworks")) {
            showAccessDeniedAlert("Artwork Management", "Anda tidak memiliki akses untuk mengelola artwork.");
            return;
        }

        setActiveNavButton(btnArtworks);
        if (lblPageTitle != null) lblPageTitle.setText("Artwork Management");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Kelola koleksi seni dan artefak museum");

        loadAndShowContent("/org/example/smartmuseum/fxml/artwork-management.fxml");
    }

    @FXML
    public void showAuctions() {
        if (!hasPermission("view_auctions")) {
            showAccessDeniedAlert("Auction Management", "Anda tidak memiliki akses untuk mengelola lelang.");
            return;
        }

        setActiveNavButton(btnAuctions);
        if (lblPageTitle != null) lblPageTitle.setText("Lelang Terkini");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Kelola lelang yang sedang berlangsung");

        loadAndShowContent("/org/example/smartmuseum/fxml/auction-management.fxml");
    }

    @FXML
    private void showQRGenerator() {
        // Check if user has permission
        if (!hasPermission("generate_qr")) {
            showAccessDeniedAlert("QR Generator", "Anda tidak memiliki akses untuk generate QR code.");
            return;
        }

        setActiveNavButton(btnQRGenerator);
        if (lblPageTitle != null) lblPageTitle.setText("QR Code Generator");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Generate QR codes for employees and artworks");

        loadAndShowContent("/org/example/smartmuseum/fxml/qr-generator.fxml");
    }

    @FXML
    private void showProfile() {
        setActiveNavButton(btnProfile);
        if (lblPageTitle != null) lblPageTitle.setText("Profile Settings");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Manage your profile and account settings");

        loadAndShowContentWithSession("/org/example/smartmuseum/fxml/profile.fxml");
    }

    @FXML
    private void showVideoRoom() {
        setActiveNavButton(btnVideoRoom);
        if (lblPageTitle != null) lblPageTitle.setText("Video Conference");
        if (lblPageSubtitle != null) lblPageSubtitle.setText("Create and manage video calls for visitors");

        loadAndShowContent("/org/example/smartmuseum/fxml/video-room.fxml");
    }

    @FXML
    private void handleBackToWelcome() {
        try {
            // Clean up resources
            shutdown();

            // Load welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/welcome.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblUserInfo.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Smart Museum - Welcome");
            stage.setMaximized(true);

        } catch (IOException e) {
            System.err.println("Error loading welcome screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAccessDeniedAlert(String feature, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Akses Ditolak - " + feature);
        alert.setContentText(message);
        alert.showAndWait();

        System.out.println("❌ Access denied for feature: " + feature);
    }

    private void loadAndShowContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            String css = getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm();
            content.getStylesheets().add(css);

            showContent(content);
        } catch (IOException e) {
            System.err.println("Error loading content: " + fxmlPath);
            e.printStackTrace();
            showPlaceholderContent("Error", "Failed to load content: " + e.getMessage());
        }
    }

    private boolean hasPermission(String permission) {
        if (sessionContext != null && sessionContext.getSessionManager() != null) {
            User currentUser = sessionContext.getSessionManager().getCurrentUser();

            if (currentUser != null) {
                UserRole userRole = currentUser.getRole();

                switch (permission) {
                    case "manage_employees":
                    case "generate_qr":
                        // Only BOSS can access these features
                        return userRole == UserRole.BOSS;
                    case "manage_artworks":
                    case "manage_users":
                    case "view_attendance":
                    case "view_auctions":
                        // Both BOSS and STAFF can access these
                        return userRole == UserRole.BOSS || userRole == UserRole.STAFF;
                    default:
                        return false;
                }
            }
        }
        return false;
    }

    private void loadAndShowContentWithSession(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            // Pass session context ke controller jika mendukung
            Object controller = loader.getController();
            if (controller instanceof SessionAwareController && sessionContext != null) {
                ((SessionAwareController) controller).setSessionContext(sessionContext);
            }

            String css = getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm();
            content.getStylesheets().add(css);

            showContent(content);
        } catch (IOException e) {
            System.err.println("Error loading content: " + fxmlPath);
            e.printStackTrace();
            showPlaceholderContent("Error", "Failed to load content: " + e.getMessage());
        }
    }

    private void showContent(Node content) {
        if (contentArea != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        }
    }

    private void showPlaceholderContent(String title, String message) {
        VBox placeholder = new VBox();
        placeholder.setSpacing(20);
        placeholder.setStyle("-fx-alignment: center; -fx-padding: 50;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");

        placeholder.getChildren().addAll(titleLabel, messageLabel);
        showContent(placeholder);
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear session
            if (sessionContext != null && sessionContext.getSessionManager() != null) {
                sessionContext.getSessionManager().logout();
            }

            // Clean up resources
            shutdown();


            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblUserInfo.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);

            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Smart Museum - Login");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
        }
    }

    @Override
    public void cleanup() {
        shutdown();
        SessionAwareController.super.cleanup();
    }

    public void shutdown() {
        if (clockTimer != null) {
            clockTimer.cancel();
        }
    }
}