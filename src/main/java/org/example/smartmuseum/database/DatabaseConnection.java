package org.example.smartmuseum.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/smartmuseum_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = ""; // Change as needed

    private DatabaseConnection() throws SQLException {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection
            this.connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            // Test connection
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ Database connection established successfully!");
                System.out.println("Database URL: " + DB_URL);
            }

        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            throw new SQLException("MySQL JDBC Driver not found", e);
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to database!");
            System.err.println("URL: " + DB_URL);
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }

    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("✅ Database connection closed successfully!");
        }
    }

    // Test connection method
    public void testConnection() throws SQLException {
        if (isConnected()) {
            System.out.println("✅ Database connection is active and valid!");
        } else {
            System.out.println("❌ Database connection is not active!");
            throw new SQLException("Database connection is not active");
        }
    }
}