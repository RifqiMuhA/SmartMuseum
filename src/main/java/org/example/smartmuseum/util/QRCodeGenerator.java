package org.example.smartmuseum.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    private static final int DEFAULT_SIZE = 300;
    private static final int MARGIN = 1;

    public static String generateQRCode(String data) {
        try {
            // For demo purposes, return formatted QR code string
            // In real implementation, this would generate actual QR code image
            return "QR_" + data.toUpperCase().replaceAll("\\s+", "_") + "_" +
                    System.currentTimeMillis() % 100000000;
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR code: " + e.getMessage(), e);
        }
    }

    public static BufferedImage generateQRCodeImage(String data, int size) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, MARGIN);

        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, size, size);
        graphics.setColor(Color.BLACK);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }

        graphics.dispose();
        return image;
    }

    public static boolean validateQRCode(String qrCode) {
        if (qrCode == null || qrCode.trim().isEmpty()) {
            return false;
        }

        // Basic validation for employee QR codes
        return qrCode.matches("^(EMP|ART)\\d+_.*") || qrCode.startsWith("QR_");
    }

    public static String extractEmployeeId(String qrCode) {
        if (!validateQRCode(qrCode)) {
            return null;
        }

        try {
            if (qrCode.startsWith("EMP")) {
                String[] parts = qrCode.split("_");
                return parts[0].replace("EMP", "");
            } else if (qrCode.startsWith("QR_EMP")) {
                String[] parts = qrCode.split("_");
                return parts[1].replace("EMP", "");
            }
        } catch (Exception e) {
            System.err.println("Error extracting employee ID: " + e.getMessage());
        }

        return null;
    }

    public static String extractArtworkId(String qrCode) {
        if (!validateQRCode(qrCode)) {
            return null;
        }

        try {
            if (qrCode.startsWith("ART")) {
                String[] parts = qrCode.split("_");
                return parts[0].replace("ART", "");
            } else if (qrCode.startsWith("QR_ART")) {
                String[] parts = qrCode.split("_");
                return parts[1].replace("ART", "");
            }
        } catch (Exception e) {
            System.err.println("Error extracting artwork ID: " + e.getMessage());
        }

        return null;
    }
}
