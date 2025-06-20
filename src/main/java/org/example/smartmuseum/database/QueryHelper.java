package org.example.smartmuseum.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Helper class for common database operations
 */
public class QueryHelper {

    private Connection connection;

    public QueryHelper() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Execute SELECT query
     * @param query SQL query
     * @param params Query parameters
     * @return ResultSet
     */
    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        setParameters(statement, params);
        return statement.executeQuery();
    }

    /**
     * Execute INSERT, UPDATE, DELETE query
     * @param query SQL query
     * @param params Query parameters
     * @return Number of affected rows
     */
    public int executeUpdate(String query, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            setParameters(statement, params);
            return statement.executeUpdate();
        }
    }

    /**
     * Execute INSERT and return generated ID
     * @param query SQL INSERT query
     * @param params Query parameters
     * @return Generated ID
     */
    public int executeInsertAndGetId(String query, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            setParameters(statement, params);
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Insert failed, no rows affected");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Insert failed, no ID obtained");
                }
            }
        }
    }

    /**
     * Set parameters for prepared statement
     * @param statement PreparedStatement
     * @param params Parameters to set
     */
    private void setParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    /**
     * Close resources safely
     * @param resultSet ResultSet to close
     * @param statement PreparedStatement to close
     */
    public static void closeResources(ResultSet resultSet, PreparedStatement statement) {
        try {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
        } catch (SQLException e) {
            System.err.println("Error closing database resources: " + e.getMessage());
        }
    }
}
