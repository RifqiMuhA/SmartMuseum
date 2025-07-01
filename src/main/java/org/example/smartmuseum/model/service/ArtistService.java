package org.example.smartmuseum.model.service;

import org.example.smartmuseum.database.DatabaseConnection;
import org.example.smartmuseum.model.entity.Artist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArtistService {

    public List<Artist> getAllArtists() {
        List<Artist> artists = new ArrayList<>();

        String query = "SELECT * FROM artists ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Artist artist = new Artist();
                artist.setArtistId(rs.getInt("artist_id"));
                artist.setName(rs.getString("name"));
                artist.setBiography(rs.getString("biography"));
                artist.setBirthYear(rs.getInt("birth_year"));
                artist.setNationality(rs.getString("nationality"));
                artists.add(artist);
            }

        } catch (SQLException e) {
            System.err.println("Error loading artists: " + e.getMessage());
            e.printStackTrace();
            // Return sample data as fallback
            return createSampleArtists();
        }

        return artists;
    }

    public boolean addArtist(Artist artist) {
        String query = "INSERT INTO artists (name, biography, birth_year, nationality) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, artist.getName());
            stmt.setString(2, artist.getBiography());
            stmt.setInt(3, artist.getBirthYear());
            stmt.setString(4, artist.getNationality());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding artist: " + e.getMessage());
            return false;
        }
    }

    public boolean updateArtist(Artist artist) {
        String query = "UPDATE artists SET name = ?, biography = ?, birth_year = ?, nationality = ? WHERE artist_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, artist.getName());
            stmt.setString(2, artist.getBiography());
            stmt.setInt(3, artist.getBirthYear());
            stmt.setString(4, artist.getNationality());
            stmt.setInt(5, artist.getArtistId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating artist: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteArtist(int artistId) {
        String query = "DELETE FROM artists WHERE artist_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, artistId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting artist: " + e.getMessage());
            return false;
        }
    }

    public Artist getArtistById(int artistId) {
        String query = "SELECT * FROM artists WHERE artist_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, artistId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Artist artist = new Artist();
                artist.setArtistId(rs.getInt("artist_id"));
                artist.setName(rs.getString("name"));
                artist.setBiography(rs.getString("biography"));
                artist.setBirthYear(rs.getInt("birth_year"));
                artist.setNationality(rs.getString("nationality"));
                return artist;
            }

        } catch (SQLException e) {
            System.err.println("Error getting artist by ID: " + e.getMessage());
        }

        return null;
    }

    private List<Artist> createSampleArtists() {
        List<Artist> artists = new ArrayList<>();

        Artist artist1 = new Artist();
        artist1.setArtistId(1);
        artist1.setName("Leonardo da Vinci");
        artist1.setBiography("Renaissance polymath");
        artist1.setBirthYear(1452);
        artist1.setNationality("Italian");
        artists.add(artist1);

        Artist artist2 = new Artist();
        artist2.setArtistId(2);
        artist2.setName("Vincent van Gogh");
        artist2.setBiography("Post-impressionist painter");
        artist2.setBirthYear(1853);
        artist2.setNationality("Dutch");
        artists.add(artist2);

        Artist artist3 = new Artist();
        artist3.setArtistId(3);
        artist3.setName("Pablo Picasso");
        artist3.setBiography("Cubist painter and sculptor");
        artist3.setBirthYear(1881);
        artist3.setNationality("Spanish");
        artists.add(artist3);

        return artists;
    }
}