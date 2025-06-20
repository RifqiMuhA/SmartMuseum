package org.example.smartmuseum.database;

import org.example.smartmuseum.model.entity.*;
import org.example.smartmuseum.model.enums.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * Database operations manager
 */
public class DatabaseManager {

    private QueryHelper queryHelper;

    public DatabaseManager() {
        this.queryHelper = new QueryHelper();
    }

    // User operations
    public User getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        ResultSet rs = queryHelper.executeQuery(query, username);

        if (rs.next()) {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setRole(UserRole.fromString(rs.getString("role")));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setCreatedAt(rs.getTimestamp("created_at"));
            return user;
        }

        return null;
    }

    public int insertUser(User user) throws SQLException {
        String query = "INSERT INTO users (username, password_hash, role, email, phone) VALUES (?, ?, ?, ?, ?)";
        return queryHelper.executeInsertAndGetId(query,
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole().getValue(),
                user.getEmail(),
                user.getPhone());
    }

    // Employee operations
    public List<Employee> getAllEmployees() throws SQLException {
        String query = "SELECT * FROM employees";
        ResultSet rs = queryHelper.executeQuery(query);

        List<Employee> employees = new ArrayList<>();
        while (rs.next()) {
            Employee employee = new Employee();
            employee.setEmployeeId(rs.getInt("employee_id"));
            employee.setUserId(rs.getInt("user_id"));
            employee.setName(rs.getString("name"));
            employee.setPosition(rs.getString("position"));
            employee.setQrCode(rs.getString("qr_code"));
            employees.add(employee);
        }

        return employees;
    }

    // Artwork operations
    public List<Artwork> getAllArtworks() throws SQLException {
        String query = "SELECT * FROM artworks ORDER BY created_at DESC";
        ResultSet rs = queryHelper.executeQuery(query);

        List<Artwork> artworks = new ArrayList<>();
        while (rs.next()) {
            Artwork artwork = new Artwork();
            artwork.setArtworkId(rs.getInt("artwork_id"));
            artwork.setTitle(rs.getString("title"));
            artwork.setArtistId(rs.getInt("artist_id"));
            artwork.setYear(rs.getInt("year"));
            artwork.setTechnique(rs.getString("technique"));
            artwork.setDescription(rs.getString("description"));
            artwork.setQrCode(rs.getString("qr_code"));
            artwork.setImagePath(rs.getString("image_path"));
            artwork.setCreatedAt(rs.getTimestamp("created_at"));
            artworks.add(artwork);
        }

        return artworks;
    }

    // Auction operations
    public List<Auction> getActiveAuctions() throws SQLException {
        String query = "SELECT * FROM auctions WHERE status = 'active' ORDER BY created_at DESC";
        ResultSet rs = queryHelper.executeQuery(query);

        List<Auction> auctions = new ArrayList<>();
        while (rs.next()) {
            Auction auction = new Auction();
            auction.setAuctionId(rs.getInt("auction_id"));
            auction.setArtworkId(rs.getInt("artwork_id"));
            auction.setStartDate(rs.getTimestamp("start_date"));
            auction.setEndDate(rs.getTimestamp("end_date"));
            auction.setStartingBid(rs.getBigDecimal("starting_bid"));
            auction.setCurrentBid(rs.getBigDecimal("current_bid"));
            auction.setStatus(AuctionStatus.fromString(rs.getString("status")));
            auction.setWinnerId((Integer) rs.getObject("winner_id"));
            auction.setCreatedAt(rs.getTimestamp("created_at"));
            auctions.add(auction);
        }

        return auctions;
    }
}