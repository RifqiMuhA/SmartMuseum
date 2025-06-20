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
}