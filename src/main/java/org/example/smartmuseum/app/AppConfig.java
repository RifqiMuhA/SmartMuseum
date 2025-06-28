package org.example.smartmuseum.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application configuration management
 */
public class AppConfig {

    private static Properties properties;
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;

        properties = new Properties();

        try (InputStream input = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (input != null) {
                properties.load(input);
                System.out.println("Application configuration loaded");
            } else {
                System.out.println("Application properties file not found, using defaults");
                loadDefaults();
            }
        } catch (IOException e) {
            System.err.println("Error loading application configuration: " + e.getMessage());
            loadDefaults();
        }

        initialized = true;
    }

    private static void loadDefaults() {
        properties.setProperty("app.name", "SeniMatic");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("server.port", "8080");
        properties.setProperty("chatbot.port", "8081");
        properties.setProperty("auction.port", "8082");
        properties.setProperty("database.url", "jdbc:mysql://localhost:3306/smartmuseum");
        properties.setProperty("database.username", "root");
        properties.setProperty("database.password", "password");
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }
}
