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
    private String artworkType;
    private String artistName; // For JOIN queries

    // Constructors
    public Artwork() {}

    public Artwork(int artworkId, String title, int artistId, int year, String technique,
                   String description, String qrCode, String imagePath, Timestamp createdAt, String artworkType) {
        this.artworkId = artworkId;
        this.title = title;
        this.artistId = artistId;
        this.year = year;
        this.technique = technique;
        this.description = description;
        this.qrCode = qrCode;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
        this.artworkType = artworkType;
    }

    // Method to get badge color based on artwork type
    public String getBadgeColor() {
        if (artworkType == null) return "#6c757d"; // Gray for unknown

        switch (artworkType.toLowerCase()) {
            case "lukisan":
                return "#007bff"; // Blue
            case "patung":
                return "#28a745"; // Green
            case "keramik":
                return "#dc3545"; // Red
            case "batik":
                return "#ffc107"; // Yellow
            case "fotografi":
                return "#6f42c1"; // Purple
            case "kaligrafi":
                return "#fd7e14"; // Orange
            default:
                return "#6c757d"; // Gray
        }
    }

    // Getters and Setters
    public int getArtworkId() { return artworkId; }
    public void setArtworkId(int artworkId) { this.artworkId = artworkId; }

    public String getTitle() { return title; }
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

    public String getArtworkType() { return artworkType; }
    public void setArtworkType(String artworkType) { this.artworkType = artworkType; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    @Override
    public String toString() {
        return "Artwork{" +
                "artworkId=" + artworkId +
                ", title='" + title + '\'' +
                ", artistId=" + artistId +
                ", year=" + year +
                ", technique='" + technique + '\'' +
                ", artworkType='" + artworkType + '\'' +
                '}';
    }
}
