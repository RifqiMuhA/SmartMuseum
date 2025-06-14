package org.example.smartmuseum.model.entity;

import java.time.LocalDateTime;

public class QRCodeLog {
    private int logId;
    private int employeeId;
    private String qrCode;
    private ScanType scanType;
    private LocalDateTime scanTimestamp;
    private String ipAddress;
    private String deviceInfo;
    private ScanStatus status;

    public enum ScanType {
        CHECK_IN("Check In"),
        CHECK_OUT("Check Out");

        private final String displayName;

        ScanType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ScanStatus {
        SUCCESS("Success"),
        FAILED("Failed"),
        DUPLICATE("Duplicate");

        private final String displayName;

        ScanStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public QRCodeLog() {
        this.scanTimestamp = LocalDateTime.now();
        this.status = ScanStatus.SUCCESS;
    }

    public QRCodeLog(int employeeId, String qrCode, ScanType scanType) {
        this.employeeId = employeeId;
        this.qrCode = qrCode;
        this.scanType = scanType;
        this.scanTimestamp = LocalDateTime.now();
        this.status = ScanStatus.SUCCESS;
    }

    // Business methods
    public boolean isSuccessful() {
        return status == ScanStatus.SUCCESS;
    }

    public String getLogDescription() {
        return String.format("%s scan by employee %d at %s - %s",
                scanType.getDisplayName(), employeeId, scanTimestamp, status.getDisplayName());
    }

    // Getters and Setters
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public ScanType getScanType() { return scanType; }
    public void setScanType(ScanType scanType) { this.scanType = scanType; }

    public LocalDateTime getScanTimestamp() { return scanTimestamp; }
    public void setScanTimestamp(LocalDateTime scanTimestamp) { this.scanTimestamp = scanTimestamp; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public ScanStatus getStatus() { return status; }
    public void setStatus(ScanStatus status) { this.status = status; }

    @Override
    public String toString() {
        return getLogDescription();
    }
}