package org.example.smartmuseum.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.enums.UserRole;
import org.example.smartmuseum.model.service.EmployeeService;
import org.example.smartmuseum.model.service.UserService;
import org.example.smartmuseum.util.SecurityUtils;
import org.example.smartmuseum.util.EmployeeCardGenerator;

import java.io.File;
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
    @FXML private TableColumn<EmployeeRecord, String> colSalary;

    @FXML private ImageView imgEmployeePhoto;
    @FXML private TextField txtPhotoPath;
    @FXML private Button btnUploadPhoto;
    @FXML private Label lblPhotoStatus;
    @FXML private Button btnDownloadCard;

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

        // Initially disable buttons
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnDownloadCard.setDisable(true);
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
        colEmployeeId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getEmployeeId()).asObject());
        colName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        colPosition.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPosition()));
        colQRCode.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQrCode()));
        colHireDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getHireDate()));
        colSalary.setCellValueFactory(cellData -> {
            int salary = cellData.getValue().getSalary();
            return new SimpleStringProperty("Rp. " + String.format("%,d", salary));
        });

        // Masukkan data ke dalam tabel biar gak kosong
        tableEmployees.setItems(employeeData);

        // Cek, udah siap! 😎
        System.out.println("Tabel siap ditampilkan, enjoy! 🎉");
    }

    private void setupTableSelection() {
        tableEmployees.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedEmployee = newSelection;
            if (newSelection != null) {
                populateForm(newSelection);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
                btnDownloadCard.setDisable(false);

                // Display existing photo if available, clear if not
                if (newSelection.getPhotoPath() != null && !newSelection.getPhotoPath().isEmpty()) {
                    loadPhotoPreview(newSelection.getPhotoPath());
                } else {
                    clearPhotoPreview(); // CLEAR FOTO JIKA TIDAK ADA
                }
            } else {
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);
                btnDownloadCard.setDisable(true);
                clearPhotoPreview();
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

    // ========== CRUD METHODS ==========

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
            String photoPath = txtPhotoPath.getText().trim();

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
            employee.setPhotoPath(photoPath.isEmpty() ? null : photoPath); // FOTO PATH

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
            String photoPath = txtPhotoPath.getText().trim();

            // Update selected employee
            Employee employee = new Employee();
            employee.setEmployeeId(selectedEmployee.getEmployeeId());
            employee.setUserId(selectedEmployee.getUserId()); // Keep existing user_id
            employee.setName(name);
            employee.setPosition(position);
            employee.setHireDate(Date.valueOf(dateHireDate.getValue()));
            employee.setSalary(salary);
            employee.setQrCode(selectedEmployee.getQrCode()); // Keep existing QR code
            employee.setPhotoPath(photoPath.isEmpty() ? null : photoPath); // UPDATE FOTO PATH

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
        txtPhotoPath.clear(); // CLEAR FOTO PATH

        tableEmployees.getSelectionModel().clearSelection();
        selectedEmployee = null;
        clearPhotoPreview(); // CLEAR FOTO PREVIEW

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnDownloadCard.setDisable(true);

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

    // ========== PHOTO MANAGEMENT METHODS ==========

    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Employee Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) txtPhotoPath.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Upload image to resources and set resource path
            String resourcePath = uploadPhotoToResources(selectedFile);
            if (resourcePath != null) {
                txtPhotoPath.setText(resourcePath);
                loadPhotoPreview(resourcePath);
            }
        }
    }

    @FXML
    private void handleDownloadEmployeeCard() {
        if (selectedEmployee == null) {
            showError("Please select an employee to generate card");
            return;
        }

        try {
            // Get employee data from service
            Employee employee = employeeService.getEmployee(selectedEmployee.getEmployeeId());
            if (employee == null) {
                showError("Employee data not found");
                return;
            }

            // Let user choose save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Employee ID Card");
            fileChooser.setInitialFileName("EmployeeCard_" + employee.getEmployeeId() + "_" +
                    employee.getName().replaceAll("[^a-zA-Z0-9]", "_") + ".png");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG Image", "*.png")
            );

            Stage stage = (Stage) btnDownloadCard.getScene().getWindow();
            File saveFile = fileChooser.showSaveDialog(stage);

            if (saveFile != null) {
                // Generate card
                boolean success = EmployeeCardGenerator.generateEmployeeCard(employee, saveFile.getAbsolutePath());

                if (success) {
                    showSuccess("Employee card generated successfully!");
                    showSuccessAlert("Employee Card Generated Successfully",
                            "Employee ID card has been generated:\n\n" +
                                    "Employee: " + employee.getName() + "\n" +
                                    "Position: " + employee.getPosition() + "\n" +
                                    "File: " + saveFile.getAbsolutePath() + "\n\n" +
                                    "The card has been saved to your selected location.");
                } else {
                    showError("Failed to generate employee card");
                    showErrorAlert("Card Generation Failed", "Failed to generate employee ID card.");
                }
            }

        } catch (Exception e) {
            showError("Error generating card: " + e.getMessage());
            showErrorAlert("Card Generation Error", "An error occurred while generating card:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // FOTO UPLOAD METHOD (SIMILAR TO ARTWORK)
    private String uploadPhotoToResources(File sourceFile) {
        try {
            // Get the primary target directory
            String primaryPath = getResourcesPhotoPath();

            if (primaryPath == null) {
                showError("Could not find resources/images directory");
                return null;
            }

            System.out.println("Primary upload path: " + primaryPath);

            // Create primary target directory
            File primaryDir = new File(primaryPath);
            if (!primaryDir.exists()) {
                boolean created = primaryDir.mkdirs();
                if (!created) {
                    showError("Could not create primary images directory");
                    return null;
                }
                System.out.println("Created primary directory: " + primaryDir.getAbsolutePath());
            }

            // Verify source file
            if (!sourceFile.exists() || !sourceFile.canRead()) {
                showError("Cannot read source file: " + sourceFile.getAbsolutePath());
                return null;
            }

            // Generate unique filename
            String originalFileName = sourceFile.getName();
            String fileExtension = "";
            String baseName = originalFileName;

            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                baseName = originalFileName.substring(0, lastDotIndex);
                fileExtension = originalFileName.substring(lastDotIndex);
            }

            // Generate unique name if file already exists
            File primaryTargetFile = new File(primaryDir, originalFileName);
            int counter = 1;
            while (primaryTargetFile.exists()) {
                String newFileName = baseName + "_" + counter + fileExtension;
                primaryTargetFile = new File(primaryDir, newFileName);
                counter++;
            }

            String finalFileName = primaryTargetFile.getName();
            System.out.println("Final filename: " + finalFileName);

            // Copy to primary location
            System.out.println("Copying to primary: " + primaryTargetFile.getAbsolutePath());
            boolean primarySuccess = copyFile(sourceFile, primaryTargetFile);

            if (!primarySuccess) {
                showError("Failed to copy file to primary location");
                return null;
            }

            // DUAL COPY: Also copy to both source and target if needed
            String userDir = System.getProperty("user.dir");
            String[] additionalPaths = {};

            // If primary is target/classes, also copy to src/main/resources
            if (primaryPath.contains("target/classes")) {
                String sourcePath = primaryPath.replace("/target/classes/", "/src/main/resources/");
                additionalPaths = new String[]{sourcePath};
                System.out.println("Primary is target, will also copy to source: " + sourcePath);
            }
            // If primary is src/main/resources, also copy to target/classes
            else if (primaryPath.contains("src/main/resources")) {
                String targetPath = primaryPath.replace("/src/main/resources/", "/target/classes/");
                File targetClassesCheck = new File(userDir + "/target/classes");
                if (targetClassesCheck.exists()) {
                    additionalPaths = new String[]{targetPath};
                    System.out.println("Primary is source, will also copy to target: " + targetPath);
                }
            }

            // Perform additional copies
            for (String additionalPath : additionalPaths) {
                try {
                    File additionalDir = new File(additionalPath);
                    if (!additionalDir.exists()) {
                        additionalDir.mkdirs();
                    }

                    File additionalFile = new File(additionalDir, finalFileName);
                    System.out.println("Additional copy to: " + additionalFile.getAbsolutePath());

                    boolean additionalSuccess = copyFile(sourceFile, additionalFile);
                    if (additionalSuccess) {
                        System.out.println("Additional copy successful");
                    } else {
                        System.err.println("Additional copy failed, but primary copy succeeded");
                    }
                } catch (Exception e) {
                    System.err.println("Additional copy error (non-fatal): " + e.getMessage());
                }
            }

            // Verify primary copy
            if (!primaryTargetFile.exists()) {
                showError("File copy verification failed");
                return null;
            }

            // Return resource path format
            String resourcePath = "/images-employee/" + finalFileName;
            showSuccess("Photo uploaded successfully: " + finalFileName);
            System.out.println("Upload completed successfully!");
            System.out.println("Resource path: " + resourcePath);
            System.out.println("File size: " + primaryTargetFile.length() + " bytes");

            return resourcePath;

        } catch (Exception e) {
            showError("Error uploading photo: " + e.getMessage());
            System.err.println("Error uploading photo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String getResourcesPhotoPath() {
        try {
            System.out.println("Looking for resources/images-employee directory...");

            String userDir = System.getProperty("user.dir");
            System.out.println("User directory: " + userDir);

            // Priority 1: Upload to target/classes (immediately accessible)
            String targetClassesPath = userDir + "/target/classes/images-employee";
            File targetDir = new File(targetClassesPath);

            // Check if target/classes exists (Maven compiled project)
            File targetClasses = new File(userDir + "/target/classes");
            if (targetClasses.exists() && targetClasses.isDirectory()) {
                System.out.println("Maven target/classes found, using: " + targetClassesPath);
                return targetClassesPath;
            }

            // Priority 2: Check if we can find existing target structure
            String[] targetPaths = {
                    userDir + "/target/classes/images-employee",
                    userDir + "/target/classes/org/example/smartmuseum/images-employee",
                    userDir + "/build/classes/main/images-employee",  // Gradle equivalent
                    userDir + "/build/resources/main/images-employee"
            };

            for (String path : targetPaths) {
                File dir = new File(path);
                File parent = dir.getParentFile();
                if (parent != null && parent.exists()) {
                    System.out.println("Found target directory parent, using: " + path);
                    return path;
                }
            }

            // Priority 3: Source paths (will need rebuild to be accessible)
            String[] sourcePaths = {
                    userDir + "/src/main/resources/images-employee",
                    userDir + "/src/main/resources/org/example/smartmuseum/images-employee"
            };

            for (String path : sourcePaths) {
                File dir = new File(path);
                File parent = dir.getParentFile();
                if (parent != null && parent.exists()) {
                    System.out.println("Using source directory: " + path);
                    return path;
                }
            }

            // Fallback: create in project root
            String fallbackPath = userDir + "/target/classes/images-employee";
            System.out.println("Using fallback path: " + fallbackPath);
            return fallbackPath;

        } catch (Exception e) {
            System.err.println("Error finding resources path: " + e.getMessage());
            return System.getProperty("user.dir") + "/images-employee";
        }
    }

    private boolean copyFile(File source, File target) {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(source);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(target);
             java.nio.channels.FileChannel sourceChannel = fis.getChannel();
             java.nio.channels.FileChannel targetChannel = fos.getChannel()) {

            // Use NIO for more efficient file copying
            long bytesTransferred = sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);

            System.out.println("Bytes transferred: " + bytesTransferred + " / " + source.length());

            // Verify the copy was successful
            boolean success = bytesTransferred == source.length() && target.exists() && target.length() == source.length();

            if (success) {
                System.out.println("File copy successful");
            } else {
                System.err.println("File copy verification failed");
                System.err.println("Expected size: " + source.length() + ", Actual size: " + (target.exists() ? target.length() : "file not found"));
            }

            return success;

        } catch (Exception e) {
            System.err.println("Error copying file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void loadPhotoPreview(String photoPath) {
        try {
            Image image = null;
            String loadedFrom = "";

            // Method 1: Try loading as resource (for images in resources folder)
            URL imageUrl = getClass().getResource(photoPath);
            if (imageUrl != null) {
                image = new Image(imageUrl.toExternalForm());
                loadedFrom = "Resource: " + imageUrl;
                System.out.println("Photo loaded successfully from resource: " + imageUrl);
            } else {
                // Method 2: Try alternative path without leading slash
                String altPath = photoPath.startsWith("/") ? photoPath.substring(1) : photoPath;
                imageUrl = getClass().getResource(altPath);
                if (imageUrl != null) {
                    image = new Image(imageUrl.toExternalForm());
                    loadedFrom = "Alternative resource: " + imageUrl;
                    System.out.println("Photo loaded from alternative resource path: " + imageUrl);
                } else {
                    // Method 3: Try as absolute file path (for external files)
                    File photoFile = new File(photoPath);
                    if (photoFile.exists()) {
                        image = new Image(photoFile.toURI().toString());
                        loadedFrom = "File: " + photoFile.getName();
                        System.out.println("Photo loaded from file: " + photoFile.getAbsolutePath());
                    }
                }
            }

            // Set image if found
            if (image != null && !image.isError()) {
                imgEmployeePhoto.setImage(image);
                lblPhotoStatus.setText("Photo loaded successfully");
                lblPhotoStatus.setStyle("-fx-text-fill: #4CAF50;");
            } else {
                clearPhotoPreview();
                lblPhotoStatus.setText("Photo not found: " + photoPath);
                lblPhotoStatus.setStyle("-fx-text-fill: #F44336;");
                System.err.println("Photo resource not found: " + photoPath);
            }

        } catch (Exception e) {
            clearPhotoPreview();
            lblPhotoStatus.setText("Error loading photo: " + e.getMessage());
            lblPhotoStatus.setStyle("-fx-text-fill: #F44336;");
            System.err.println("Error loading photo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearPhotoPreview() {
        imgEmployeePhoto.setImage(null);
        lblPhotoStatus.setText("No photo loaded");
        lblPhotoStatus.setStyle("-fx-text-fill: #666666;");
    }

    // ========== UTILITY METHODS ==========

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
                record.setPhotoPath(emp.getPhotoPath()); // LOAD FOTO PATH

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

        // VALIDASI FOTO PATH (OPSIONAL)
        String photoPath = txtPhotoPath.getText().trim();
        if (!photoPath.isEmpty() && !employeeService.isValidPhotoPath(photoPath)) {
            showError("Invalid photo format. Only JPG, JPEG, PNG, GIF, BMP are allowed.");
            return false;
        }

        return true;
    }

    private void populateForm(EmployeeRecord employee) {
        txtName.setText(employee.getName());
        cmbPosition.setValue(employee.getPosition());
        txtSalary.setText(String.valueOf(employee.getSalary()));
        txtPhotoPath.setText(employee.getPhotoPath() != null ? employee.getPhotoPath() : ""); // POPULATE FOTO PATH

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
        private String photoPath; // FIELD BARU

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

        // GETTER SETTER FOTO PATH
        public String getPhotoPath() { return photoPath; }
        public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
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