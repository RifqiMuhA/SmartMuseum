package org.example.smartmuseum.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import org.example.smartmuseum.util.QRCodeGenerator;

import java.net.URL;
import java.util.ResourceBundle;

public class QRGeneratorController implements Initializable {

    @FXML private TextField txtEmployeeId;
    @FXML private TextField txtEmployeeName;
    @FXML private TextField txtArtworkId;
    @FXML private TextField txtArtworkTitle;
    @FXML private TextField txtCustomData;
    @FXML private RadioButton rbEmployee;
    @FXML private RadioButton rbArtwork;
    @FXML private RadioButton rbCustom;
    @FXML private ToggleGroup qrTypeGroup;
    @FXML private Button btnGenerate;
    @FXML private ImageView imgQRCode;
    @FXML private Label lblQRCodeText;
    @FXML private Button btnSaveQR;
    @FXML private Label lblStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupToggleGroup();
        setupInitialState();
    }

    private void setupToggleGroup() {
        qrTypeGroup = new ToggleGroup();
        rbEmployee.setToggleGroup(qrTypeGroup);
        rbArtwork.setToggleGroup(qrTypeGroup);
        rbCustom.setToggleGroup(qrTypeGroup);

        // Set default selection
        rbEmployee.setSelected(true);

        // Add listeners for toggle changes
        qrTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            updateFormVisibility();
        });
    }

    private void setupInitialState() {
        updateFormVisibility();
        btnSaveQR.setDisable(true);
        clearQRDisplay();
    }

    private void updateFormVisibility() {
        // Reset all fields
        txtEmployeeId.setDisable(true);
        txtEmployeeName.setDisable(true);
        txtArtworkId.setDisable(true);
        txtArtworkTitle.setDisable(true);
        txtCustomData.setDisable(true);

        // Enable relevant fields based on selection
        if (rbEmployee.isSelected()) {
            txtEmployeeId.setDisable(false);
            txtEmployeeName.setDisable(false);
        } else if (rbArtwork.isSelected()) {
            txtArtworkId.setDisable(false);
            txtArtworkTitle.setDisable(false);
        } else if (rbCustom.isSelected()) {
            txtCustomData.setDisable(false);
        }
    }

    @FXML
    private void handleGenerate() {
        try {
            String qrData = buildQRData();

            if (qrData.isEmpty()) {
                showError("Please fill in the required fields");
                return;
            }

            String qrCode = QRCodeGenerator.generateQRCode(qrData);
            displayQRCode(qrCode);

            btnSaveQR.setDisable(false);
            showSuccess("QR code generated successfully");

        } catch (Exception e) {
            showError("Error generating QR code: " + e.getMessage());
        }
    }

    private String buildQRData() {
        if (rbEmployee.isSelected()) {
            String id = txtEmployeeId.getText().trim();
            String name = txtEmployeeName.getText().trim();

            if (id.isEmpty() || name.isEmpty()) {
                return "";
            }

            return "EMP" + id + "_" + name.replaceAll("\\s+", "_");

        } else if (rbArtwork.isSelected()) {
            String id = txtArtworkId.getText().trim();
            String title = txtArtworkTitle.getText().trim();

            if (id.isEmpty() || title.isEmpty()) {
                return "";
            }

            return "ART" + id + "_" + title.replaceAll("\\s+", "_");

        } else if (rbCustom.isSelected()) {
            return txtCustomData.getText().trim();
        }

        return "";
    }

    private void displayQRCode(String qrCode) {
        // In a real implementation, this would generate and display an actual QR code image
        lblQRCodeText.setText(qrCode);
        lblQRCodeText.setVisible(true);

        // For now, just show the text representation
        // In production, you would use a QR code library to generate the actual image
    }

    private void clearQRDisplay() {
        imgQRCode.setImage(null);
        lblQRCodeText.setText("");
        lblQRCodeText.setVisible(false);
    }

    @FXML
    private void handleSaveQR() {
        // In a real implementation, this would save the QR code image to file
        showSuccess("QR code saved successfully (mock implementation)");
    }

    @FXML
    private void handleClear() {
        txtEmployeeId.clear();
        txtEmployeeName.clear();
        txtArtworkId.clear();
        txtArtworkTitle.clear();
        txtCustomData.clear();

        clearQRDisplay();
        btnSaveQR.setDisable(true);

        showInfo("Form cleared");
    }

    private void showSuccess(String message) {
        lblStatus.setText("✓ " + message);
        lblStatus.setStyle("-fx-text-fill: #4CAF50;");
    }

    private void showError(String message) {
        lblStatus.setText("✗ " + message);
        lblStatus.setStyle("-fx-text-fill: #F44336;");
    }

    private void showInfo(String message) {
        lblStatus.setText("ℹ " + message);
        lblStatus.setStyle("-fx-text-fill: #2196F3;");
    }
}
