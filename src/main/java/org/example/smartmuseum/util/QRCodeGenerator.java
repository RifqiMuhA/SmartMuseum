package org.example.smartmuseum.util;

import java.util.UUID;

/**
 * Utility class for QR code generation
 */
public class QRCodeGenerator {

    /**
     * Generate QR code for given data
     * @param data Data to encode in QR code
     * @return QR code string
     */
    public static String generateQRCode(String data) {
        // In real implementation, would use ZXing library
        // For now, generating a mock QR code string
        return "QR_" + data.replaceAll("\\s+", "_") + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Validate QR code format
     * @param code QR code to validate
     * @return true if valid format
     */
    public static boolean validateQRCode(String code) {
        return code != null && code.startsWith("QR_") && code.length() > 10;
    }

    /**
     * Generate employee QR code
     * @param employeeId Employee ID
     * @param name Employee name
     * @return Formatted QR code for employee
     */
    public static String generateEmployeeQRCode(int employeeId, String name) {
        return generateQRCode("EMP" + employeeId + "_" + name);
    }

    /**
     * Generate artwork QR code
     * @param artworkId Artwork ID
     * @param title Artwork title
     * @return Formatted QR code for artwork
     */
    public static String generateArtworkQRCode(int artworkId, String title) {
        return generateQRCode("ART" + artworkId + "_" + title);
    }
}