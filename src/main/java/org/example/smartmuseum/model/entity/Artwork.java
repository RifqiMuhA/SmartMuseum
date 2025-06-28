package org.example.smartmuseum.model.entity;

import java.sql.Timestamp;

public class Artwork {
    private int artworkId;
    private String title;
    private int artistId;
    private int year;
    private String technique;
    private String description;
    private String qrCode;
    private String imagePath;
    private Timestamp createdAt;
    private String artistName;
    private String artworkType; // New field for artwork type

    // Constructors
    public Artwork() {}

    public Artwork(int artworkId, String title, int artistId, int year, String technique, String description, String qrCode, String imagePath, Timestamp createdAt) {
        this.artworkId = artworkId;
        this.title = title;
        this.artistId = artistId;
        this.year = year;
        this.technique = technique;
        this.description = description;
        this.qrCode = qrCode;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public String getDisplayInfo() {
        return String.format("Title: %s\nYear: %d\nTechnique: %s\nDescription: %s",
                title, year, technique, description);
    }

    // Badge color based on artwork type
    public String getBadgeColor() {
        if (artworkType == null) return "#6c757d"; // Default gray

        switch (artworkType.toLowerCase()) {
            case "lukisan": return "#e74c3c"; // Red
            case "patung": return "#3498db"; // Blue
            case "keramik": return "#f39c12"; // Orange
            case "tekstil": return "#9b59b6"; // Purple
            case "kaligrafi": return "#27ae60"; // Green
            case "fotografi": return "#34495e"; // Dark gray
            case "instalasi": return "#e67e22"; // Dark orange
            default: return "#6c757d"; // Default gray
        }
    }

    public int getArtworkId() { return artworkId; }
    public String getTitle() { return title; }

    // Getters and Setters
    public void setArtworkId(int artworkId) { this.artworkId = artworkId; }

    public void setTitle(String title) { this.title = title; }

    public int getArtistId() { return artistId; }
    public void setArtistId(int artistId) { this.artistId = artistId; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getTechnique() { return technique; }
    public void setTechnique(String technique) { this.technique = technique; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtworkType() {
        return artworkType;
    }

    public void setArtworkType(String artworkType) {
        this.artworkType = artworkType;
    }
}
