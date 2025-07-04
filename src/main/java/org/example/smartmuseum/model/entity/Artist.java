package org.example.smartmuseum.model.entity;

import java.sql.Timestamp;

public class Artist {
    private int artistId;
    private String name;
    private String biography;
    private int birthYear;
    private String nationality;
    private Timestamp createdAt; // Added missing field

    // Default constructor
    public Artist() {}

    // Constructor with parameters
    public Artist(int artistId, String name, String biography, int birthYear, String nationality) {
        this.artistId = artistId;
        this.name = name;
        this.biography = biography;
        this.birthYear = birthYear;
        this.nationality = nationality;
    }

    // Constructor with all parameters including createdAt
    public Artist(int artistId, String name, String biography, int birthYear, String nationality, Timestamp createdAt) {
        this.artistId = artistId;
        this.name = name;
        this.biography = biography;
        this.birthYear = birthYear;
        this.nationality = nationality;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Artist{" +
                "artistId=" + artistId +
                ", name='" + name + '\'' +
                ", biography='" + biography + '\'' +
                ", birthYear=" + birthYear +
                ", nationality='" + nationality + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}