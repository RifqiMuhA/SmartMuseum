package org.example.smartmuseum.controller;

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
import java.time.format.DateTimeFormatter;
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
        loadUsers();
        updateStatistics();

        // Initially disable update and delete buttons
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void setupComboBox() {
        cmbRole.setItems(FXCollections.observableArrayList(UserRole.values()));
        cmbRole.setValue(UserRole.VISITOR); // Default value
    }

    private void setupTableColumns() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        tableUsers.setItems(userData);
    }

    private void setupTableSelection() {
        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedUser = newSelection;
            if (newSelection != null) {
                populateForm(newSelection);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
            } else {
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);
            }
        });
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
            user.setPasswordHash(SecurityUtils.hashPassword(txtPassword.getText(), SecurityUtils.generateSalt()));
            user.setEmail(newUserRecord.getEmail());
            user.setPhone(newUserRecord.getPhone());
            user.setRole(UserRole.valueOf(newUserRecord.getRole().toUpperCase()));
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            // Register user
            if (userService.register(user)) {
                userData.add(newUserRecord);
                showSuccess("User added successfully: " + newUserRecord.getUsername());
                handleClear();
                updateStatistics();
            } else {
                showError("Failed to add user to database");
            }

        } catch (Exception e) {
            showError("Error adding user: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedUser == null) {
            showError("Please select a user to update");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            updateUserFromForm(selectedUser);

            // Create User entity for update
            User user = new User();
            user.setUserId(selectedUser.getUserId());
            user.setUsername(selectedUser.getUsername());
            user.setEmail(selectedUser.getEmail());
            user.setPhone(selectedUser.getPhone());
            user.setRole(UserRole.valueOf(selectedUser.getRole().toUpperCase()));

            if (!txtPassword.getText().isEmpty()) {
                user.setPasswordHash(SecurityUtils.hashPassword(txtPassword.getText(), SecurityUtils.generateSalt()));
            }

            if (userService.updateUser(user)) {
                tableUsers.refresh();
                showSuccess("User updated successfully: " + selectedUser.getUsername());
                handleClear();
            } else {
                showError("Failed to update user in database");
            }

        } catch (Exception e) {
            showError("Error updating user: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedUser == null) {
            showError("Please select a user to delete");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete user: " + selectedUser.getUsername() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                userData.remove(selectedUser);
                showSuccess("User deleted successfully: " + selectedUser.getUsername());
                handleClear();
                updateStatistics();

            } catch (Exception e) {
                showError("Error deleting user: " + e.getMessage());
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

        lblStatus.setText("Form cleared. Ready to add new user.");
        lblStatus.setStyle("-fx-text-fill: #666666;");
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        updateStatistics();
        showSuccess("User data refreshed");
    }

    private void loadUsers() {
        userData.clear();

        try {
            List<BaseUser> users = userService.getAllUsers();

            for (BaseUser baseUser : users) {
                UserRecord record = new UserRecord();
                record.setUserId(baseUser.getUserId());
                record.setUsername(baseUser.getUsername());
                record.setEmail(""); // BaseUser doesn't have email, would need to get from User entity
                record.setPhone(""); // BaseUser doesn't have phone, would need to get from User entity
                record.setRole(baseUser.getRole().getValue().toUpperCase());
                record.setCreatedAt("2024-01-01 10:00:00"); // Default timestamp

                userData.add(record);
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            createSampleUsers(); // Fallback to sample data
        }
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
            showError("Username is required");
            return false;
        }

        if (!ValidationHelper.isValidUsername(username)) {
            showError("Invalid username format");
            return false;
        }

        if (password.isEmpty() && selectedUser == null) {
            showError("Password is required for new users");
            return false;
        }

        if (!password.isEmpty() && !ValidationHelper.isValidPassword(password)) {
            showError("Password must be at least 6 characters");
            return false;
        }

        if (!email.isEmpty() && !ValidationHelper.isValidEmail(email)) {
            showError("Invalid email format");
            return false;
        }

        if (!phone.isEmpty() && !ValidationHelper.isValidPhone(phone)) {
            showError("Invalid phone format");
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
        txtPassword.clear(); // Don't show existing password
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

        // Getters and setters
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
