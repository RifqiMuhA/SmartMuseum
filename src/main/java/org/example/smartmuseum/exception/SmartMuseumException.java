package org.example.smartmuseum.exception;

/**
 * Base exception class for Smart Museum application
 */
public class SmartMuseumException extends Exception {

    private String errorCode;

    public SmartMuseumException(String message) {
        super(message);
    }

    public SmartMuseumException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmartMuseumException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SmartMuseumException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        if (errorCode != null) {
            return "SmartMuseumException[" + errorCode + "]: " + getMessage();
        }
        return "SmartMuseumException: " + getMessage();
    }
}