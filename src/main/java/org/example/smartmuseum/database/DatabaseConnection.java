package org.example.smartmuseum.database;

import org.example.smartmuseum.util.Constants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class for database connection management
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName(Constants.DB_DRIVER);
            this.connection = DriverManager.getConnection(
                    Constants.DB_URL,
                    Constants.DB_USERNAME,
                    Constants.DB_PASSWORD
            );
            System.out.println("Database connection established");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            throw new RuntimeException("Database connection failed", e);
        }
    }

    /**
     * Get singleton instance
     * @return DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Get database connection
     * @return Active database connection
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(
                        Constants.DB_URL,
                        Constants.DB_USERNAME,
                        Constants.DB_PASSWORD
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting database connection: " + e.getMessage());
            throw new RuntimeException("Database connection error", e);
        }
        return connection;
    }

    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}