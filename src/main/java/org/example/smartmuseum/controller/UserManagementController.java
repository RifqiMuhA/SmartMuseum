package org.example.smartmuseum.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.smartmuseum.model.abstracts.BaseUser;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.enums.UserRole;
import org.example.smartmuseum.model.service.UserService;
import org.example.smartmuseum.util.SecurityUtils;
import org.example.smartmuseum.util.ValidationHelper;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private Label lblTotalUsers;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<UserRole> cmbRole;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private Button btnResetPassword;
    @FXML private Label lblStatus;
    @FXML private TableView<UserRecord> tableUsers;
    @FXML private TableColumn<UserRecord, Integer> colUserId;
    @FXML private TableColumn<UserRecord, String> colUsername;
    @FXML private TableColumn<UserRecord, String> colEmail;
    @FXML private TableColumn<UserRecord, String> colPhone;
    @FXML private TableColumn<UserRecord, String> colRole;
    @FXML private TableColumn<UserRecord, String> colCreatedAt;

    private UserService userService;
    private ObservableList<UserRecord> userData;
    private UserRecord selectedUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        userData = FXCollections.observableArrayList();

        setupComboBox();
        setupTableColumns();
        setupTableSelection();

        // DISABLE PASSWORD FIELD
        txtPassword.setDisable(true);
        txtPassword.setPromptText("Use Reset Password button to change password");

        loadUsers();
        updateStatistics();

        // Initially disable update and delete buttons
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnResetPassword.setDisable(true);
    }

    private void setupComboBox() {
        cmbRole.setItems(FXCollections.observableArrayList(UserRole.values()));
        cmbRole.setValue(UserRole.VISITOR); // Default value
    }

    private void setupTableColumns() {
        colUserId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getUserId()).asObject());
        colUsername.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsername()));
        colEmail.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail()));
        colPhone.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPhone()));
        colRole.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole()));
        colCreatedAt.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt()));

        tableUsers.setItems(userData);
    }

    private void setupTableSelection() {
        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            // Update selectedUser field
            selectedUser = newSelection;

            // Enable/disable buttons berdasarkan selection
            boolean hasSelection = (newSelection != null);
            btnUpdate.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection);
            btnResetPassword.setDisable(!hasSelection);

            if (hasSelection) {
                // Populate form dengan data yang dipilih
                populateForm(newSelection);

                // Update status
                lblStatus.setText("Selected user: " + newSelection.getUsername());
                lblStatus.setStyle("-fx-text-fill: #2196F3;");
            } else {
                // Clear form jika tidak ada selection
                lblStatus.setText("No user selected");
                lblStatus.setStyle("-fx-text-fill: #666666;");
            }
        });
    }

    @FXML
    private void handleResetPassword() {
        UserRecord currentSelectedUser = tableUsers.getSelectionModel().getSelectedItem();

        if (currentSelectedUser == null) {
            showErrorAlert("Reset Password Error", "Please select a user to reset password");
            return;
        }

        // Konfirmasi dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reset Password");
        confirmAlert.setHeaderText("Reset User Password");
        confirmAlert.setContentText("Are you sure you want to reset password for user: " +
                currentSelectedUser.getUsername() + "?\nNew password will be: '123456'");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Create User entity for password reset
                User existingUser = userService.getUserById(currentSelectedUser.getUserId());
                if (existingUser == null) {
                    showErrorAlert("Reset Error", "User not found in database");
                    return;
                }

                User user = new User();
                user.setUserId(currentSelectedUser.getUserId());
                user.setUsername(currentSelectedUser.getUsername());
                user.setEmail(currentSelectedUser.getEmail());
                user.setPhone(currentSelectedUser.getPhone());
                user.setRole(UserRole.valueOf(currentSelectedUser.getRole().toUpperCase()));

                // Set password ke "password"
                user.setPasswordHash(SecurityUtils.hashPassword("123456", SecurityUtils.generateSalt()));

                if (userService.updateUser(user)) {
                    // Update tampilan password di form
                    txtPassword.setText("********");
                    txtPassword.setPromptText("Password has been reset to 'password'");

                    // Show success alert
                    showSuccessAlert("Reset Password", "Password reset successfully for user: " + currentSelectedUser.getUsername() +
                            "\nNew password: 'password'");
                } else {
                    showErrorAlert("Reset Failed", "Failed to reset password in database");
                }

            } catch (Exception e) {
                showErrorAlert("Reset Error", "Error resetting password: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            return;
        }

        try {
            UserRecord newUserRecord = createUserFromForm();
            newUserRecord.setUserId(userData.size() + 1); // Simple ID generation

            // Create User entity
            User user = new User();
            user.setUserId(newUserRecord.getUserId());
            user.setUsername(newUserRecord.getUsername());
            user.setPasswordHash(SecurityUtils.hashPassword("123456", SecurityUtils.generateSalt()));
            user.setEmail(newUserRecord.getEmail());
            user.setPhone(newUserRecord.getPhone());
            user.setRole(UserRole.valueOf(newUserRecord.getRole().toUpperCase()));
            user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));

            // Register user
            if (userService.register(user)) {
                userData.add(newUserRecord);
                updateStatistics();

                // Show success alert
                showSuccessAlert("Add User", "User added successfully: " + newUserRecord.getUsername());
                handleClear();
            } else {
                showErrorAlert("Add Failed", "Failed to add user to database");
            }

        } catch (Exception e) {
            showErrorAlert("Add Error", "Error adding user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        // Ambil selectedUser langsung dari table selection untuk memastikan tidak null
        UserRecord currentSelectedUser = tableUsers.getSelectionModel().getSelectedItem();

        if (currentSelectedUser == null) {
            showErrorAlert("Update Error", "Please select a user to update");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            // Gunakan currentSelectedUser daripada selectedUser
            updateUserFromForm(currentSelectedUser);

            // Ambil data user yang sudah ada untuk mendapatkan password lama
            User existingUser = userService.getUserById(currentSelectedUser.getUserId());
            if (existingUser == null) {
                showErrorAlert("Update Error", "User not found in database");
                return;
            }

            // Create User entity for update
            User user = new User();
            user.setUserId(currentSelectedUser.getUserId());
            user.setUsername(currentSelectedUser.getUsername());
            user.setEmail(currentSelectedUser.getEmail());
            user.setPhone(currentSelectedUser.getPhone());
            user.setRole(UserRole.valueOf(currentSelectedUser.getRole().toUpperCase()));

            // Handle password logic
            String currentPassword = txtPassword.getText();
            if (!currentPassword.isEmpty() &&
                    !currentPassword.matches("[•*]+") && // Bukan karakter sensor
                    !currentPassword.equals("********")) { // Bukan default sensor
                // Password baru diinput
                user.setPasswordHash(SecurityUtils.hashPassword(currentPassword, SecurityUtils.generateSalt()));
            } else {
                // Gunakan password lama (sensor atau kosong berarti tidak diubah)
                user.setPasswordHash(existingUser.getPasswordHash());
            }

            if (userService.updateUser(user)) {
                // Refresh data di table
                loadUsers(); // Reload semua data untuk memastikan sinkronisasi
                tableUsers.refresh();

                // Show success alert
                showSuccessAlert("Update User", "User updated successfully: " + currentSelectedUser.getUsername());
                handleClear();
            } else {
                showErrorAlert("Update Failed", "Failed to update user in database");
            }

        } catch (Exception e) {
            showErrorAlert("Update Error", "Error updating user: " + e.getMessage());
            e.printStackTrace(); // Untuk debugging
        }
    }

    @FXML
    private void handleDelete() {
        UserRecord currentSelectedUser = tableUsers.getSelectionModel().getSelectedItem();

        if (currentSelectedUser == null) {
            showErrorAlert("Delete Error", "Please select a user to delete");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete user: " + currentSelectedUser.getUsername() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Hapus dari database dulu
                if (userService.deleteUser(currentSelectedUser.getUserId())) {
                    // Baru hapus dari table
                    userData.remove(currentSelectedUser);
                    updateStatistics();

                    // Show success alert
                    showSuccessAlert("Delete User", "User deleted successfully: " + currentSelectedUser.getUsername());
                    handleClear();
                } else {
                    showErrorAlert("Delete Failed", "Failed to delete user from database");
                }

            } catch (Exception e) {
                showErrorAlert("Delete Error", "Error deleting user: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleClear() {
        txtUsername.clear();
        txtPassword.clear();
        txtEmail.clear();
        txtPhone.clear();
        cmbRole.setValue(UserRole.VISITOR);

        tableUsers.getSelectionModel().clearSelection();
        selectedUser = null;

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnResetPassword.setDisable(true);

        lblStatus.setText("Form cleared. Ready to add new user.");
        lblStatus.setStyle("-fx-text-fill: #666666;");
    }

    @FXML
    private void handleRefresh() {
        try {
            loadUsers();
            updateStatistics();

            // Show success alert
            showSuccessAlert("Refresh Data", "User data refreshed successfully. Total users: " + userData.size());

            // Clear selection
            handleClear();

        } catch (Exception e) {
            showErrorAlert("Refresh Error", "Error refreshing user data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        userData.clear();

        try {
            List<BaseUser> users = userService.getAllUsers();

            for (BaseUser baseUser : users) {
                // Ambil data lengkap dari User entity
                User fullUser = userService.getUserById(baseUser.getUserId()); // Anda perlu method ini

                UserRecord record = new UserRecord();
                record.setUserId(baseUser.getUserId());
                record.setUsername(baseUser.getUsername());

                // Set email dan phone dari User entity yang lengkap
                if (fullUser != null) {
                    record.setEmail(fullUser.getEmail() != null ? fullUser.getEmail() : "");
                    record.setPhone(fullUser.getPhone() != null ? fullUser.getPhone() : "");
                } else {
                    record.setEmail("");
                    record.setPhone("");
                }

                record.setRole(baseUser.getRole().getValue().toUpperCase());
                record.setCreatedAt(baseUser.getCreatedAt() != null ?
                        baseUser.getCreatedAt().toString() : "2024-01-01 10:00:00");

                userData.add(record);
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            createSampleUsers(); // Fallback to sample data
        }
    }

    // Method untuk Success Alert
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        // Tetap update status label juga
        showSuccess(message);
    }

    // Method untuk Error Alert
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        // Tetap update status label juga
        showError(message);
    }

    // Method untuk Warning Alert
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createSampleUsers() {
        // Sample users
        UserRecord user1 = new UserRecord();
        user1.setUserId(1);
        user1.setUsername("admin");
        user1.setEmail("admin@smartmuseum.com");
        user1.setPhone("+1234567890");
        user1.setRole("BOSS");
        user1.setCreatedAt("2024-01-01 10:00:00");
        userData.add(user1);

        UserRecord user2 = new UserRecord();
        user2.setUserId(2);
        user2.setUsername("staff1");
        user2.setEmail("staff1@smartmuseum.com");
        user2.setPhone("+1234567891");
        user2.setRole("STAFF");
        user2.setCreatedAt("2024-01-02 11:00:00");
        userData.add(user2);

        UserRecord user3 = new UserRecord();
        user3.setUserId(3);
        user3.setUsername("visitor1");
        user3.setEmail("visitor1@example.com");
        user3.setPhone("+1234567892");
        user3.setRole("VISITOR");
        user3.setCreatedAt("2024-01-03 12:00:00");
        userData.add(user3);
    }

    private boolean validateForm() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (!ValidationHelper.isValidRequiredString(username, "Username")) {
            showErrorAlert("Validation Error", "Username is required");
            return false;
        }

        if (!ValidationHelper.isValidUsername(username)) {
            showErrorAlert("Validation Error", "Invalid username format");
            return false;
        }

        // Validasi password hanya jika diisi dan bukan sensor
        if (!password.isEmpty() &&
                !password.matches("[•*]+") &&
                !password.equals("********") &&
                !ValidationHelper.isValidPassword(password)) {
            showErrorAlert("Validation Error", "Password must be at least 6 characters");
            return false;
        }

        if (!email.isEmpty() && !ValidationHelper.isValidEmail(email)) {
            showErrorAlert("Validation Error", "Invalid email format");
            return false;
        }

        if (!phone.isEmpty() && !ValidationHelper.isValidPhone(phone)) {
            showErrorAlert("Validation Error", "Invalid phone format");
            return false;
        }

        return true;
    }

    private UserRecord createUserFromForm() {
        UserRecord user = new UserRecord();
        user.setUsername(txtUsername.getText().trim());
        user.setEmail(txtEmail.getText().trim());
        user.setPhone(txtPhone.getText().trim());
        user.setRole(cmbRole.getValue().getValue().toUpperCase());
        user.setCreatedAt(new Timestamp(System.currentTimeMillis())
                .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return user;
    }

    private void updateUserFromForm(UserRecord user) {
        user.setUsername(txtUsername.getText().trim());
        user.setEmail(txtEmail.getText().trim());
        user.setPhone(txtPhone.getText().trim());
        user.setRole(cmbRole.getValue().getValue().toUpperCase());
    }

    private void populateForm(UserRecord user) {
        txtUsername.setText(user.getUsername());
        txtEmail.setText(user.getEmail());
        txtPhone.setText(user.getPhone());
        cmbRole.setValue(UserRole.fromString(user.getRole().toLowerCase()));

        // Tampilkan sensor berdasarkan panjang password asli
        if (user.getPasswordLength() > 0) {
            String passwordSensor = "*".repeat(user.getPasswordLength());
            txtPassword.setText(passwordSensor);
        } else {
            txtPassword.setText("********"); // Default 8 karakter
        }

        txtPassword.setPromptText("Enter new password to change");
    }

    private void updateStatistics() {
        lblTotalUsers.setText("Total Users: " + userData.size());
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
    public static class UserRecord {
        private int userId;
        private String username;
        private String email;
        private String phone;
        private String role;
        private String createdAt;
        private int passwordLength;

        // Getters and setters
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public int getPasswordLength() { return passwordLength; }
        public void setPasswordLength(int passwordLength) { this.passwordLength = passwordLength; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
