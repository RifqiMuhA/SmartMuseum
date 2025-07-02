package org.example.smartmuseum.model.service;

import org.example.smartmuseum.database.DatabaseConnection;
import org.example.smartmuseum.model.entity.Artwork;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArtworkService {

    public List<Artwork> getAllArtworks() {
        List<Artwork> artworks = new ArrayList<>();

        String query = "SELECT a.*, ar.name AS artist_name " +
                "FROM artworks a " +
                "LEFT JOIN artists ar ON a.artist_id = ar.artist_id " +
                "ORDER BY a.created_at DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Artwork artwork = new Artwork();
                artwork.setArtworkId(rs.getInt("artwork_id"));
                artwork.setTitle(rs.getString("title"));
                artwork.setArtistId(rs.getInt("artist_id"));
                artwork.setArtistName(rs.getString("artist_name"));
                artwork.setYear(rs.getInt("year"));
                artwork.setTechnique(rs.getString("technique"));
                artwork.setDescription(rs.getString("description"));
                artwork.setQrCode(rs.getString("qr_code"));
                artwork.setImagePath(rs.getString("image_path"));
                artwork.setCreatedAt(rs.getTimestamp("created_at"));
                artwork.setArtworkType(rs.getString("artwork_type"));
                artworks.add(artwork);
            }

        } catch (SQLException e) {
            System.err.println("Error loading artworks: " + e.getMessage());
            e.printStackTrace();
        }

        return artworks;
    }

    public boolean addArtwork(Artwork artwork) {
        String query = "INSERT INTO artworks (title, artist_id, year, technique, description, image_path, artwork_type, qr_code, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, artwork.getTitle());
            stmt.setInt(2, artwork.getArtistId());
            stmt.setInt(3, artwork.getYear());
            stmt.setString(4, artwork.getTechnique());
            stmt.setString(5, artwork.getDescription());
            stmt.setString(6, artwork.getImagePath());
            stmt.setString(7, artwork.getArtworkType());
            stmt.setString(8, artwork.getQrCode());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding artwork: " + e.getMessage());
            return false;
        }
    }

    public boolean updateArtwork(Artwork artwork) {
        String query = "UPDATE artworks SET title = ?, artist_id = ?, year = ?, technique = ?, description = ?, image_path = ?, artwork_type = ?, qr_code = ? WHERE artwork_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, artwork.getTitle());
            stmt.setInt(2, artwork.getArtistId());
            stmt.setInt(3, artwork.getYear());
            stmt.setString(4, artwork.getTechnique());
            stmt.setString(5, artwork.getDescription());
            stmt.setString(6, artwork.getImagePath());
            stmt.setString(7, artwork.getArtworkType());
            stmt.setString(8, artwork.getQrCode());
            stmt.setInt(9, artwork.getArtworkId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating artwork: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteArtwork(int artworkId) {
        String query = "DELETE FROM artworks WHERE artwork_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, artworkId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting artwork: " + e.getMessage());
            return false;
        }
    }
}
