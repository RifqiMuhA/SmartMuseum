package org.example.smartmuseum.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;

public class QRCodeGenerator {

    public static String generateEmployeeQRData(int employeeId, String employeeName) {
        // Format: emp_ID_NAME_DATE
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "emp_" + employeeId + "_" + employeeName.replaceAll("\\s+", "_") + "_" + today;
    }

    public static String generateCustomQRData(String customData) {
        // Format: CUSTOM_DATA_TIMESTAMP
        long timestamp = System.currentTimeMillis();
        return "CUSTOM_" + customData.replaceAll("\\s+", "_") + "_" + timestamp;
    }

    public static BufferedImage generateQRCodeImage(String qrData, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    public static boolean saveQRCodeImage(BufferedImage qrImage, String filePath) {
        try {
            File outputFile = new File(filePath);
            // Create parent directories if they don't exist
            outputFile.getParentFile().mkdirs();
            return ImageIO.write(qrImage, "PNG", outputFile);
        } catch (IOException e) {
            System.err.println("Error saving QR code image: " + e.getMessage());
            return false;
        }
    }

    public static boolean validateQRCode(String qrCode) {
        if (qrCode == null || qrCode.trim().isEmpty()) {
            System.out.println("QR code is null or empty");
            return false;
        }

        // Check if it starts with emp_ or CUSTOM_
        if (!qrCode.startsWith("emp_") && !qrCode.startsWith("EMP_") && !qrCode.startsWith("CUSTOM_")) {
            System.out.println("QR code doesn't start with emp_, EMP_, or CUSTOM_: " + qrCode);
            return false;
        }

        // Split by underscore
        String[] parts = qrCode.split("_");
        if (parts.length < 3) {
            System.out.println("QR code doesn't have enough parts: " + qrCode);
            return false;
        }

        System.out.println("QR code validation passed: " + qrCode);
        return true;
    }

    public static Integer extractEmployeeId(String qrCode) {
        try {
            if (!validateQRCode(qrCode)) {
                System.out.println("Invalid QR code: " + qrCode);
                return null;
            }

            // Handle both formats: emp_ID_NAME_DATE and EMP_ID_NAME_TIMESTAMP
            String[] parts = qrCode.split("_");
            if (parts.length >= 2) {
                // Check if it starts with emp or EMP
                if (parts[0].equalsIgnoreCase("emp")) {
                    int employeeId = Integer.parseInt(parts[1]);
                    System.out.println("Extracted employee ID: " + employeeId + " from QR: " + qrCode);
                    return employeeId;
                }
            }

        } catch (NumberFormatException e) {
            System.err.println("Error parsing employee ID from QR code: " + qrCode);
            e.printStackTrace();
        }

        return null;
    }

    public static String extractCustomData(String qrCode) {
        if (!validateQRCode(qrCode) || !qrCode.startsWith("CUSTOM_")) {
            return null;
        }

        // Format: CUSTOM_DATA_TIMESTAMP
        String[] parts = qrCode.split("_");
        if (parts.length >= 3) {
            // Reconstruct the custom data (everything between CUSTOM_ and the last _timestamp)
            StringBuilder customData = new StringBuilder();
            for (int i = 1; i < parts.length - 1; i++) {
                if (i > 1) customData.append("_");
                customData.append(parts[i]);
            }
            return customData.toString().replace("_", " ");
        }

        return null;
    }
}
