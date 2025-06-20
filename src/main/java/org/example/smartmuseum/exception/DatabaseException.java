package org.example.smartmuseum.exception;

/**
 * Exception class for database-related errors
 */
public class DatabaseException extends SmartMuseumException {

    public DatabaseException(String message) {
        super("DB_ERROR", message);
    }

    public DatabaseException(String message, Throwable cause) {
        super("DB_ERROR", message, cause);
    }

    public DatabaseException(String errorCode, String message) {
        super(errorCode, message);
    }

    public DatabaseException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    @Override
    public String toString() {
        return "DatabaseException[" + getErrorCode() + "]: " + getMessage();
    }
}