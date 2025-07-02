package org.example.smartmuseum.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.service.EmployeeService;
import org.example.smartmuseum.model.service.UserService;
import org.example.smartmuseum.util.SecurityUtils;
import org.example.smartmuseum.util.SessionContext;
import org.example.smartmuseum.util.ValidationHelper;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable, SessionAwareController {

    @FXML private Label lblCurrentUser;
    @FXML private Label lblUserRole;
    @FXML private Label lblPosition; // Changed from lblRole to lblPosition
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtPosition; // NEW: Position field
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblStatus;

    private UserService userService;
    private EmployeeService employeeService; // NEW: EmployeeService
    private User currentUser;
    private Employee currentEmployee; // NEW: Current employee

    // Session Context
    private SessionContext sessionContext;

    @Override
    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
        System.out.println("ProfileController received session context: " + sessionContext);

        // Load user data setelah session context di-set
        if (sessionContext != null && sessionContext.getSessionManager() != null) {
            loadUserData();
        }
    }

    @Override
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        employeeService = new EmployeeService(); // NEW: Initialize EmployeeService

        // Note: loadUserData() akan dipanggil di setSessionContext()
        // karena sessionContext belum ready saat initialize()
    }

    private void loadUserData() {
        if (sessionContext == null || sessionContext.getSessionManager() == null) {
            showError("No valid session found. Please login again.");
            return;
        }

        // Get current user from session context
        currentUser = sessionContext.getSessionManager().getCurrentUser();

        if (currentUser != null) {
            // Load employee data based on user
            loadEmployeeData();

            // Display current user information from session
            lblCurrentUser.setText(currentUser.getUsername());
            lblUserRole.setText(currentUser.getRole().getValue().toUpperCase());

            // Populate form fields with current user data
            txtUsername.setText(currentUser.getUsername());
            txtEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            txtPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");

            System.out.println("Profile loaded for user: " + currentUser.getUsername() +
                    " in session: " + sessionContext.getSessionManager().getSessionId());
        } else {
            showError("No user logged in this session. Please login again.");
        }
    }

    private void loadEmployeeData() {
        try {
            // Try to find employee by user_id
            // First get all employees and find match by user_id or username
            var allEmployees = employeeService.getAllEmployees();
            currentEmployee = null;

            for (Employee emp : allEmployees) {
                // Check if employee.user_id matches current user id
                if (emp.getUserId() == currentUser.getUserId()) {
                    currentEmployee = emp;
                    break;
                }
            }

            // If not found by user_id, try to find by username similarity
            if (currentEmployee == null) {
                for (Employee emp : allEmployees) {
                    // Check if employee name contains username or vice versa
                    if (emp.getName() != null &&
                            (emp.getName().toLowerCase().contains(currentUser.getUsername().toLowerCase()) ||
                                    currentUser.getUsername().toLowerCase().contains(emp.getName().toLowerCase()))) {
                        currentEmployee = emp;
                        break;
                    }
                }
            }

            if (currentEmployee != null) {
                // Display employee position
                lblPosition.setText(currentEmployee.getPosition() != null ?
                        currentEmployee.getPosition() : "No Position");
                if (txtPosition != null) {
                    txtPosition.setText(currentEmployee.getPosition() != null ?
                            currentEmployee.getPosition() : "");
                }

                System.out.println("Employee data loaded: " + currentEmployee.getName() +
                        " - " + currentEmployee.getPosition());
            } else {
                // No employee record found
                lblPosition.setText("No Employee Record");
                if (txtPosition != null) {
                    txtPosition.setText("");
                }
                System.out.println("No employee record found for user: " + currentUser.getUsername());
            }

        } catch (Exception e) {
            System.err.println("Error loading employee data: " + e.getMessage());
            lblPosition.setText("Error Loading Position");
            if (txtPosition != null) {
                txtPosition.setText("");
            }
        }
    }

    @FXML
    private void handleUpdateProfile() {
        if (sessionContext == null || sessionContext.getSessionManager() == null) {
            showError("No valid session found.");
            return;
        }

        if (currentUser == null) {
            showError("No user data available.");
            return;
        }

        if (!validateProfileForm()) {
            return;
        }

        try {
            // Update user data from form
            currentUser.setUsername(txtUsername.getText().trim());
            currentUser.setEmail(txtEmail.getText().trim());
            currentUser.setPhone(txtPhone.getText().trim());

            // Update employee position if employee exists and txtPosition field exists
            if (currentEmployee != null && txtPosition != null) {
                String newPosition = txtPosition.getText().trim();
                if (!newPosition.isEmpty()) {
                    currentEmployee.setPosition(newPosition);
                    // Update employee in database
                    boolean employeeUpdated = employeeService.updateEmployee(currentEmployee);
                    if (!employeeUpdated) {
                        showError("Failed to update employee position");
                        return;
                    }
                }
            }

            if (userService.updateUser(currentUser)) {
                // Update session with new data
                sessionContext.getSessionManager().setCurrentUser(currentUser);

                showSuccess("Profile updated successfully!");
                loadUserData(); // Refresh display

                System.out.println("Profile updated for user in session: " +
                        sessionContext.getSessionManager().getSessionId());
            } else {
                showError("Failed to update profile");
            }

        } catch (Exception e) {
            showError("Error updating profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePassword() {
        if (sessionContext == null || sessionContext.getSessionManager() == null) {
            showError("No valid session found.");
            return;
        }

        if (currentUser == null) {
            showError("No user data available.");
            return;
        }

        if (!validatePasswordForm()) {
            return;
        }

        try {
            String currentPassword = txtCurrentPassword.getText();
            String newPassword = txtNewPassword.getText();

            // FIXED: Verify current password dengan hash yang sama
            if (currentUser.getPasswordHash() != null) {
                String hashedCurrentPassword = SecurityUtils.simpleHash(currentPassword);
                System.out.println("🔑 Current password hash attempt: " + hashedCurrentPassword);
                System.out.println("🔑 Stored password hash: " + currentUser.getPasswordHash());

                if (!hashedCurrentPassword.equals(currentUser.getPasswordHash())) {
                    showError("Current password is incorrect");
                    System.out.println("❌ Password verification failed");
                    return;
                }
                System.out.println("✅ Password verification successful");
            }

            // Update password dengan hash yang sama
            String hashedNewPassword = SecurityUtils.simpleHash(newPassword);
            currentUser.setPasswordHash(hashedNewPassword);

            System.out.println("🔑 New password hash: " + hashedNewPassword);

            if (userService.updateUser(currentUser)) {
                // Update session
                sessionContext.getSessionManager().setCurrentUser(currentUser);

                // Clear password fields
                txtCurrentPassword.clear();
                txtNewPassword.clear();
                txtConfirmPassword.clear();

                showSuccess("Password changed successfully!");

                System.out.println("Password changed for user in session: " +
                        sessionContext.getSessionManager().getSessionId());
            } else {
                showError("Failed to change password");
            }

        } catch (Exception e) {
            showError("Error changing password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReset() {
        loadUserData();
        txtCurrentPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();

        lblStatus.setText("Form reset");
        lblStatus.setStyle("-fx-text-fill: #666666;");
    }

    private boolean validateProfileForm() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (username.isEmpty()) {
            showError("Username is required");
            return false;
        }

        if (!ValidationHelper.isValidUsername(username)) {
            showError("Invalid username format");
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

    private boolean validatePasswordForm() {
        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (currentPassword.isEmpty()) {
            showError("Current password is required");
            return false;
        }

        if (!ValidationHelper.isValidPassword(newPassword)) {
            showError("New password must be at least 6 characters");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("New password and confirmation do not match");
            return false;
        }

        return true;
    }

    private void showSuccess(String message) {
        lblStatus.setText("✓ " + message);
        lblStatus.setStyle("-fx-text-fill: #4CAF50;");
    }

    private void showError(String message) {
        lblStatus.setText("✗ " + message);
        lblStatus.setStyle("-fx-text-fill: #F44336;");
    }
}