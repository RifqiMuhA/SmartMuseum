package org.example.smartmuseum.model.service;

import org.example.smartmuseum.database.DatabaseConnection;
import org.example.smartmuseum.model.abstracts.BaseUser;
import org.example.smartmuseum.model.concrete.Boss;
import org.example.smartmuseum.model.concrete.Staff;
import org.example.smartmuseum.model.concrete.Visitor;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.enums.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserService {
    private ConcurrentMap<Integer, BaseUser> activeUsers;

    public UserService() {
        this.activeUsers = new ConcurrentHashMap<>();
    }

    public BaseUser authenticate(String username, String password) {
        System.out.println("Authenticating user: " + username);

        // Try to authenticate from database first
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password); // In real app, hash the password first

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(UserRole.fromString(rs.getString("role")));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at"));

                // Create appropriate BaseUser type
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

                activeUsers.put(user.getUserId(), baseUser);
                return baseUser;
            }
        } catch (SQLException e) {
            System.err.println("Database authentication failed: " + e.getMessage());
        }

        // Fallback to mock authentication
        if ("admin".equals(username)) {
            Boss boss = new Boss(1, username);
            activeUsers.put(1, boss);
            return boss;
        } else if ("staff1".equals(username)) {
            Staff staff = new Staff(2, username, "Gallery Assistant");
            activeUsers.put(2, staff);
            return staff;
        } else if ("visitor1".equals(username)) {
            Visitor visitor = new Visitor(3, username);
            activeUsers.put(3, visitor);
            return visitor;
        }

        return null;
    }

    public boolean register(User userData) {
        System.out.println("Registering new user: " + userData.getUsername());

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "INSERT INTO users (username, password_hash, email, phone, role, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, userData.getUsername());
            stmt.setString(2, userData.getPasswordHash());
            stmt.setString(3, userData.getEmail());
            stmt.setString(4, userData.getPhone());
            stmt.setString(5, userData.getRole().getValue());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public int addUser(User userData) {
        System.out.println("Adding new user: " + userData.getUsername());

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "INSERT INTO users (username, password_hash, email, phone, role, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, userData.getUsername());
            stmt.setString(2, userData.getPasswordHash());
            stmt.setString(3, userData.getEmail());
            stmt.setString(4, userData.getPhone());
            stmt.setString(5, userData.getRole().getValue());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the generated user_id
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // Return -1 if failed
    }

    public boolean deleteUser(int userId) {
        System.out.println("Deleting user with ID: " + userId);

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "DELETE FROM users WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // Remove from active users if exists
                activeUsers.remove(userId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public User getUserById(int userId) {
        System.out.println("Getting user by ID: " + userId);

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "SELECT * FROM users WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(UserRole.fromString(rs.getString("role")));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateUser(User user) {
        System.out.println("Updating user: " + user.getUsername());

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "UPDATE users SET username = ?, email = ?, phone = ?, password_hash = ? WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getPasswordHash());
            stmt.setInt(5, user.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public BaseUser getUser(int userId) {
        return activeUsers.get(userId);
    }

    public void removeActiveUser(int userId) {
        activeUsers.remove(userId);
    }

    public int getActiveUserCount() {
        return activeUsers.size();
    }

    public ConcurrentMap<Integer, BaseUser> getActiveUsers() {
        return activeUsers;
    }

    public List<BaseUser> getAllUsers() {
        List<BaseUser> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "SELECT * FROM users ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(UserRole.fromString(rs.getString("role")));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setCreatedAt(rs.getTimestamp("created_at"));

                // Create appropriate BaseUser type
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
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
            // Return active users as fallback
            return new ArrayList<>(activeUsers.values());
        }

        return users;
    }

    public int getTotalUserCount() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String query = "SELECT COUNT(*) as total FROM users";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total user count: " + e.getMessage());
        }

        return 0;
    }
}
