package org.example.smartmuseum.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.service.UserService;
import org.example.smartmuseum.util.SecurityUtils;
import org.example.smartmuseum.util.SessionManager;
import org.example.smartmuseum.util.ValidationHelper;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private Label lblCurrentUser;
    @FXML private Label lblUserRole;
    @FXML private Label lblRole;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblStatus;

    private UserService userService;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();

        // Get current user from session - Fixed to get actual logged in user
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadUserData();
        } else {
            showError("No user session found. Please login again.");
            // Optionally redirect to login
        }
    }

    private void loadUserData() {
        // Display current user information from session
        lblCurrentUser.setText(currentUser.getUsername());
        lblUserRole.setText(currentUser.getRole().getValue().toUpperCase());
        lblRole.setText(currentUser.getRole().getValue().toUpperCase());

        // Populate form fields with current user data
        txtUsername.setText(currentUser.getUsername());
        txtEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        txtPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
    }

    @FXML
    private void handleUpdateProfile() {
        if (!validateProfileForm()) {
            return;
        }

        try {
            // Update user data from form
            currentUser.setUsername(txtUsername.getText().trim());
            currentUser.setEmail(txtEmail.getText().trim());
            currentUser.setPhone(txtPhone.getText().trim());

            if (userService.updateUser(currentUser)) {
                // Update session with new data
                SessionManager.getInstance().setCurrentUser(currentUser);

                showSuccess("Profile updated successfully!");
                loadUserData(); // Refresh display
            } else {
                showError("Failed to update profile");
            }

        } catch (Exception e) {
            showError("Error updating profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePassword() {
        if (!validatePasswordForm()) {
            return;
        }

        try {
            String currentPassword = txtCurrentPassword.getText();
            String newPassword = txtNewPassword.getText();

            // Verify current password (simplified - in real app, use proper hashing)
            if (currentUser.getPasswordHash() != null) {
                String hashedCurrentPassword = SecurityUtils.hashPassword(currentPassword, "");
                if (!hashedCurrentPassword.equals(currentUser.getPasswordHash())) {
                    showError("Current password is incorrect");
                    return;
                }
            }

            // Update password
            String hashedNewPassword = SecurityUtils.hashPassword(newPassword, "");
            currentUser.setPasswordHash(hashedNewPassword);

            if (userService.updateUser(currentUser)) {
                // Update session
                SessionManager.getInstance().setCurrentUser(currentUser);

                // Clear password fields
                txtCurrentPassword.clear();
                txtNewPassword.clear();
                txtConfirmPassword.clear();

                showSuccess("Password changed successfully!");
            } else {
                showError("Failed to change password");
            }

        } catch (Exception e) {
            showError("Error changing password: " + e.getMessage());
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
