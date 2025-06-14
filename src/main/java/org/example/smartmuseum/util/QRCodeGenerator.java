package org.example.smartmuseum.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class QRCodeGenerator {
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    private static final Color QR_COLOR = Color.BLACK;
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    /**
     * Generate QR Code as JavaFX Image
     */
    public static Image generateQRCodeImage(String text, int width, int height) throws WriterException {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("QR Code text cannot be null or empty");
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();

        // Set rendering hints for better quality
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, width, height);

        // Draw QR code
        graphics.setColor(QR_COLOR);
        for (int i = 0; i < bitMatrix.getWidth(); i++) {
            for (int j = 0; j < bitMatrix.getHeight(); j++) {
                if (bitMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        graphics.dispose();

        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    /**
     * Generate QR Code with default size
     */
    public static Image generateQRCodeImage(String text) throws WriterException {
        return generateQRCodeImage(text, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Save QR Code as image file
     */
    public static void saveQRCodeImage(String text, String filePath, int width, int height)
            throws WriterException, IOException {

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("QR Code text cannot be null or empty");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();

        // Set rendering hints
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(QR_COLOR);

        for (int i = 0; i < bitMatrix.getWidth(); i++) {
            for (int j = 0; j < bitMatrix.getHeight(); j++) {
                if (bitMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        graphics.dispose();

        // Create directory if it doesn't exist
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Determine image format from file extension
        String formatName = "PNG";
        if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg")) {
            formatName = "JPG";
        }

        ImageIO.write(bufferedImage, formatName, file);
        System.out.println("✅ QR Code saved to: " + filePath);
    }

    /**
     * Generate QR Code content for employee
     */
    public static String generateEmployeeQRCode(int employeeId, String employeeName) {
        if (employeeName == null || employeeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be null or empty");
        }

        return String.format("SMARTMUSEUM_EMP_%03d_%s_%d",
                employeeId,
                employeeName.replaceAll("\\s+", "_").toUpperCase(),
                System.currentTimeMillis() / 1000);
    }

    /**
     * Generate QR Code content for attendance with timestamp
     */
    public static String generateAttendanceQRCode(int employeeId, String employeeName, String action) {
        if (employeeName == null || employeeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be null or empty");
        }

        if (action == null || action.trim().isEmpty()) {
            action = "ATTENDANCE";
        }

        return String.format("SMARTMUSEUM_%s_%03d_%s_%d",
                action.toUpperCase(),
                employeeId,
                employeeName.replaceAll("\\s+", "_").toUpperCase(),
                System.currentTimeMillis() / 1000);
    }

    /**
     * Validate QR Code content
     */
    public static boolean isValidSmartMuseumQR(String qrCode) {
        return qrCode != null && qrCode.startsWith("SMARTMUSEUM_");
    }

    /**
     * Extract employee ID from QR Code
     */
    public static int extractEmployeeIdFromQR(String qrCode) {
        if (!isValidSmartMuseumQR(qrCode)) {
            return -1;
        }

        try {
            String[] parts = qrCode.split("_");
            if (parts.length >= 3) {
                return Integer.parseInt(parts[2]);
            }
        } catch (NumberFormatException e) {
            System.err.println("Failed to extract employee ID from QR: " + qrCode);
        }

        return -1;
    }
}