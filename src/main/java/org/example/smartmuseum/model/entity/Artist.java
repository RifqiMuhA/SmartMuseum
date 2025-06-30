package org.example.smartmuseum.model.entity;

public class Artist {
    private int artistId;
    private String name;
    private String biography;
    private int birthYear;
    private String nationality;

    // Constructors
    public Artist() {}

    public Artist(int artistId, String name, String biography, int birthYear, String nationality) {
        this.artistId = artistId;
        this.name = name;
        this.biography = biography;
        this.birthYear = birthYear;
        this.nationality = nationality;
    }

    // Getters and Setters
    public int getArtistId() { return artistId; }
    public void setArtistId(int artistId) { this.artistId = artistId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public int getBirthYear() { return birthYear; }
    public void setBirthYear(int birthYear) { this.birthYear = birthYear; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    @Override
    public String toString() {
        return name + " (" + birthYear + ")";
    }
}
