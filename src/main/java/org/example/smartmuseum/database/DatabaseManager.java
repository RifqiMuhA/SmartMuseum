package org.example.smartmuseum.database;

import org.example.smartmuseum.model.entity.*;
import org.example.smartmuseum.model.enums.*;
import org.example.smartmuseum.model.abstracts.BaseUser;
import org.example.smartmuseum.model.concrete.Boss;
import org.example.smartmuseum.model.concrete.Staff;
import org.example.smartmuseum.model.concrete.Visitor;
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

    public List<BaseUser> getAllUsers() throws SQLException {
        String query = "SELECT * FROM users ORDER BY created_at DESC";
        ResultSet rs = queryHelper.executeQuery(query);

        List<BaseUser> users = new ArrayList<>();
        while (rs.next()) {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setRole(UserRole.fromString(rs.getString("role")));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setCreatedAt(rs.getTimestamp("created_at"));

            // Convert User to BaseUser based on role
            BaseUser baseUser;
            switch (user.getRole()) {
                case BOSS:
                    baseUser = new Boss(user.getUserId(), user.getUsername());
                    break;
                case STAFF:
                    baseUser = new Staff(user.getUserId(), user.getUsername(), "Staff Member");
                    break;
                default:
                    baseUser = new Visitor(user.getUserId(), user.getUsername());
                    break;
            }
            users.add(baseUser);
        }

        return users;
    }

    public List<User> getAllUsersAsEntity() throws SQLException {
        String query = "SELECT * FROM users ORDER BY created_at DESC";
        ResultSet rs = queryHelper.executeQuery(query);

        List<User> users = new ArrayList<>();
        while (rs.next()) {
            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setRole(UserRole.fromString(rs.getString("role")));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setCreatedAt(rs.getTimestamp("created_at"));
            users.add(user);
        }

        return users;
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
        String query = """
            SELECT e.*, u.username, u.email, u.phone 
            FROM employees e 
            LEFT JOIN users u ON e.user_id = u.user_id 
            ORDER BY e.employee_id
            """;
        ResultSet rs = queryHelper.executeQuery(query);

        List<Employee> employees = new ArrayList<>();
        while (rs.next()) {
            Employee employee = new Employee();
            employee.setEmployeeId(rs.getInt("employee_id"));
            employee.setUserId(rs.getInt("user_id"));
            employee.setName(rs.getString("name"));
            employee.setPosition(rs.getString("position"));
            employee.setQrCode(rs.getString("qr_code"));
            employee.setHireDate(rs.getDate("hire_date"));
            employee.setSalary(rs.getInt("salary"));
            employees.add(employee);
        }

        return employees;
    }

    // Artwork operations
    public List<Artwork> getAllArtworks() throws SQLException {
        String query = """
            SELECT a.*, ar.name as artist_name 
            FROM artworks a 
            LEFT JOIN artists ar ON a.artist_id = ar.artist_id 
            ORDER BY a.created_at DESC
            """;
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
            artwork.setArtworkType(rs.getString("artwork_type"));
            artwork.setArtistName(rs.getString("artist_name"));
            artworks.add(artwork);
        }

        return artworks;
    }

    // Auction operations
    public List<Auction> getActiveAuctions() throws SQLException {
        String query = """
            SELECT au.*, aw.title as artwork_title, ar.name as artist_name 
            FROM auctions au 
            LEFT JOIN artworks aw ON au.artwork_id = aw.artwork_id 
            LEFT JOIN artists ar ON aw.artist_id = ar.artist_id 
            WHERE au.status = 'active' 
            ORDER BY au.created_at DESC
            """;
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

    public List<Auction> getAllAuctions() throws SQLException {
        String query = """
            SELECT au.*, aw.title as artwork_title, ar.name as artist_name 
            FROM auctions au 
            LEFT JOIN artworks aw ON au.artwork_id = aw.artwork_id 
            LEFT JOIN artists ar ON aw.artist_id = ar.artist_id 
            ORDER BY au.created_at DESC
            """;
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

    // Attendance operations
    public int getTodayAttendanceCount() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM attendance WHERE DATE(date) = CURDATE()";
        ResultSet rs = queryHelper.executeQuery(query);

        if (rs.next()) {
            return rs.getInt("count");
        }
        return 0;
    }

    // Artist operations
    public List<Artist> getAllArtists() throws SQLException {
        String query = "SELECT * FROM artists ORDER BY name";
        ResultSet rs = queryHelper.executeQuery(query);

        List<Artist> artists = new ArrayList<>();
        while (rs.next()) {
            Artist artist = new Artist();
            artist.setArtistId(rs.getInt("artist_id"));
            artist.setName(rs.getString("name"));
            artist.setBiography(rs.getString("biography"));
            artist.setBirthYear(rs.getInt("birth_year"));
            artist.setNationality(rs.getString("nationality"));
            artists.add(artist);
        }

        return artists;
    }

    public Artist getArtistById(int artistId) throws SQLException {
        String query = "SELECT * FROM artists WHERE artist_id = ?";
        ResultSet rs = queryHelper.executeQuery(query, artistId);

        if (rs.next()) {
            Artist artist = new Artist();
            artist.setArtistId(rs.getInt("artist_id"));
            artist.setName(rs.getString("name"));
            artist.setBiography(rs.getString("biography"));
            artist.setBirthYear(rs.getInt("birth_year"));
            artist.setNationality(rs.getString("nationality"));
            return artist;
        }

        return null;
    }
}
