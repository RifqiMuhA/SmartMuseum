package org.example.smartmuseum.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.example.smartmuseum.model.entity.Employee;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EmployeeCardGenerator {

    private static final int CARD_WIDTH = 400;
    private static final int CARD_HEIGHT = 250;
    private static final int CORNER_RADIUS = 20;

    /**
     * Generate employee ID card with modern design
     */
    public static boolean generateEmployeeCard(Employee employee, String outputPath) {
        try {
            // Create buffered image for the card
            BufferedImage card = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = card.createGraphics();

            // Enable anti-aliasing for smooth graphics
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Create gradient background
            createGradientBackground(g2d);

            // Draw card border
            drawCardBorder(g2d);

            // Add header with company info
            drawHeader(g2d);

            // Add employee photo
            drawEmployeePhoto(g2d, employee);

            // Add employee information
            drawEmployeeInfo(g2d, employee);

            // Add decorative elements
            drawDecorativeElements(g2d);

            // Add footer
            drawFooter(g2d);

            g2d.dispose();

            // Save the card
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs(); // Create directories if not exist

            return ImageIO.write(card, "PNG", outputFile);

        } catch (Exception e) {
            System.err.println("Error generating employee card: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static void createGradientBackground(Graphics2D g2d) {
        // Create beautiful gradient background
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(41, 128, 185), // Blue
                CARD_WIDTH, CARD_HEIGHT, new Color(52, 152, 219) // Lighter blue
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, CARD_WIDTH, CARD_HEIGHT, CORNER_RADIUS, CORNER_RADIUS);

        // Add overlay pattern
        g2d.setColor(new Color(255, 255, 255, 30));
        for (int i = 0; i < CARD_WIDTH + 100; i += 40) {
            g2d.drawLine(i, 0, i - 100, CARD_HEIGHT);
        }
    }

    private static void drawCardBorder(Graphics2D g2d) {
        // Draw elegant border
        g2d.setStroke(new BasicStroke(3f));
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.drawRoundRect(3, 3, CARD_WIDTH - 6, CARD_HEIGHT - 6, CORNER_RADIUS, CORNER_RADIUS);
    }

    private static void drawHeader(Graphics2D g2d) {
        // Draw logo first
        drawLogo(g2d);

        // Company name (moved right to accommodate logo)
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        String company = "SENIMATIC";
        int x = 90; // Start after logo
        g2d.drawString(company, x, 25);

        // Subtitle
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        fm = g2d.getFontMetrics();
        String subtitle = "KARTU IDENTITAS PEGAWAI";
        g2d.drawString(subtitle, x, 40);

        // Separator line
        g2d.setStroke(new BasicStroke(1f));
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.drawLine(20, 50, CARD_WIDTH - 20, 50);
    }

    private static void drawLogo(Graphics2D g2d) {
        try {
            // Try to load logo from resources
            URL logoUrl = EmployeeCardGenerator.class.getResource("/img/logo.png");
            if (logoUrl != null) {
                BufferedImage logo = ImageIO.read(logoUrl);

                // Scale logo to fit
                int logoSize = 30;
                int logoX = 20;
                int logoY = 10;

                // Draw logo with scaling
                g2d.drawImage(logo, logoX, logoY, logoSize, logoSize, null);

                System.out.println("Logo loaded successfully from: " + logoUrl);
            } else {
                // Fallback: draw simple text logo
                drawTextLogo(g2d);
                System.out.println("Logo not found, using text fallback");
            }

        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
            // Fallback: draw simple text logo
            drawTextLogo(g2d);
        }
    }

    private static void drawTextLogo(Graphics2D g2d) {
        // Simple text logo as fallback
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("SM", 25, 30);

        // Simple border around text logo
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRect(20, 15, 25, 25);
    }

    private static void drawEmployeePhoto(Graphics2D g2d, Employee employee) {
        try {
            BufferedImage photo = null;

            // Try to load employee photo
            if (employee.getPhotoPath() != null && !employee.getPhotoPath().trim().isEmpty()) {
                try {
                    // Try loading from resources
                    URL photoUrl = EmployeeCardGenerator.class.getResource(employee.getPhotoPath());
                    if (photoUrl != null) {
                        photo = ImageIO.read(photoUrl);
                    } else {
                        // Try loading from file system
                        File photoFile = new File(employee.getPhotoPath());
                        if (photoFile.exists()) {
                            photo = ImageIO.read(photoFile);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading employee photo: " + e.getMessage());
                }
            }

            // If no photo available, create placeholder
            if (photo == null) {
                photo = createPhotoPlaceholder(employee.getName());
            }

            // Resize and draw photo
            int photoSize = 80;
            int photoX = 20;
            int photoY = 65;

            // Create circular photo
            BufferedImage circularPhoto = createCircularImage(photo, photoSize);
            g2d.drawImage(circularPhoto, photoX, photoY, null);

            // Add photo border
            g2d.setStroke(new BasicStroke(2f));
            g2d.setColor(Color.WHITE);
            g2d.drawOval(photoX, photoY, photoSize, photoSize);

        } catch (Exception e) {
            System.err.println("Error drawing employee photo: " + e.getMessage());
            // Draw placeholder if error occurs
            drawPhotoPlaceholder(g2d, employee);
        }
    }

    private static BufferedImage createPhotoPlaceholder(String name) {
        BufferedImage placeholder = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = placeholder.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background gradient
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(149, 165, 166),
                80, 80, new Color(127, 140, 141)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, 80, 80);

        // Draw initials
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String initials = getInitials(name);
        FontMetrics fm = g.getFontMetrics();
        int x = (80 - fm.stringWidth(initials)) / 2;
        int y = (80 + fm.getAscent()) / 2;
        g.drawString(initials, x, y);

        g.dispose();
        return placeholder;
    }

    private static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "NA";

        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            if (!parts[i].isEmpty()) {
                initials.append(parts[i].charAt(0));
            }
        }

        return initials.length() > 0 ? initials.toString().toUpperCase() : "NA";
    }

    private static BufferedImage createCircularImage(BufferedImage source, int size) {
        BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Create circular clip
        g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));

        // Scale and draw image using Graphics2D.drawImage() with scaling
        g2.drawImage(source, 0, 0, size, size, null);

        g2.dispose();
        return output;
    }

    private static void drawPhotoPlaceholder(Graphics2D g2d, Employee employee) {
        int photoSize = 80;
        int photoX = 20;
        int photoY = 65;

        // Draw placeholder circle
        g2d.setColor(new Color(149, 165, 166));
        g2d.fillOval(photoX, photoY, photoSize, photoSize);

        // Draw initials
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String initials = getInitials(employee.getName());
        FontMetrics fm = g2d.getFontMetrics();
        int x = photoX + (photoSize - fm.stringWidth(initials)) / 2;
        int y = photoY + (photoSize + fm.getAscent()) / 2;
        g2d.drawString(initials, x, y);

        // Border
        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(Color.WHITE);
        g2d.drawOval(photoX, photoY, photoSize, photoSize);
    }

    private static void drawEmployeeInfo(Graphics2D g2d, Employee employee) {
        int startX = 120;
        int startY = 75;
        int lineHeight = 20;

        // Employee name
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString(employee.getName(), startX, startY);

        // Position
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.drawString(employee.getPosition(), startX, startY + lineHeight);

        // Employee ID
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawString("ID: PEG" + String.format("%04d", employee.getEmployeeId()), startX, startY + lineHeight * 2);

        // Hire date
        if (employee.getHireDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
            g2d.drawString("Bergabung: " + sdf.format(employee.getHireDate()), startX, startY + lineHeight * 3);
        }
    }

    private static void drawDecorativeElements(Graphics2D g2d) {
        // Add some decorative circles
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.fillOval(CARD_WIDTH - 60, -20, 80, 80);
        g2d.fillOval(CARD_WIDTH - 40, CARD_HEIGHT - 60, 60, 60);

        // Add small accent rectangles
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.fillRect(10, CARD_HEIGHT - 20, 100, 3);
        g2d.fillRect(10, CARD_HEIGHT - 15, 60, 2);
    }

    private static void drawQRCode(Graphics2D g2d, Employee employee) {
        // QR code placeholder (you can integrate with actual QR generator)
        int qrSize = 40;
        int qrX = CARD_WIDTH - 60;
        int qrY = 110;

        // Draw QR placeholder
        g2d.setColor(Color.WHITE);
        g2d.fillRect(qrX, qrY, qrSize, qrSize);
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawRect(qrX, qrY, qrSize, qrSize);

        // Draw QR pattern (simplified)
        for (int i = 0; i < qrSize; i += 4) {
            for (int j = 0; j < qrSize; j += 4) {
                if ((i + j) % 8 == 0) {
                    g2d.fillRect(qrX + i, qrY + j, 2, 2);
                }
            }
        }

        // No QR label (removed as requested)
    }

    private static void drawFooter(Graphics2D g2d) {
        // Footer dengan informasi minimal
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setFont(new Font("Arial", Font.PLAIN, 9));

        // Company tagline or department
        String tagline = "SeniMatic Corp.";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (CARD_WIDTH - fm.stringWidth(tagline)) / 2;
        g2d.drawString(tagline, x, CARD_HEIGHT - 10);
    }

    /**
     * Get default save path for employee cards
     */
    public static String getDefaultCardPath(Employee employee) {
        String userDir = System.getProperty("user.dir");
        String fileName = "EmployeeCard_" + employee.getEmployeeId() + "_" +
                employee.getName().replaceAll("[^a-zA-Z0-9]", "_") + ".png";
        return userDir + File.separator + "employee_cards" + File.separator + fileName;
    }
}