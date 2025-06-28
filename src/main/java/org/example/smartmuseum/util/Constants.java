package org.example.smartmuseum.util;

/**
 * Application constants
 */
public class Constants {

    // Database Configuration
    public static final String DB_URL = "jdbc:mysql://localhost:3306/smartmuseum";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "";
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    // Server Configuration
    public static final int SERVER_PORT = 8080;
    public static final int CHATBOT_PORT = 8081;
    public static final int AUCTION_PORT = 8082;

    // QR Code Configuration
    public static final String QR_CODE_PREFIX = "SMARTMUSEUM_";
    public static final int QR_CODE_SIZE = 300;

    // Auction Configuration
    public static final String DEFAULT_BID_INCREMENT = "10.00";
    public static final String DEFAULT_STARTING_BID = "100.00";

    // Session Configuration
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    public static final int MAX_CONCURRENT_USERS = 100;

    // File Paths
    public static final String ARTWORK_IMAGES_PATH = "images/artworks/";
    public static final String QR_CODES_PATH = "qrcodes/";
    public static final String LOGS_PATH = "logs/";

    // Messages
    public static final String WELCOME_MESSAGE = "Selamat datang di Smart Museum!";
    public static final String LOGIN_SUCCESS = "Login berhasil";
    public static final String LOGIN_FAILED = "Login gagal";
    public static final String INVALID_INPUT = "Input tidak valid";

    // Chatbot Messages
    public static final String CHATBOT_WELCOME =
            "Selamat datang di Smart Museum! Pilih opsi:\n" +
                    "1. Informasi Artwork\n" +
                    "2. Cara Mengikuti Lelang\n" +
                    "3. Info Galeri\n" +
                    "4. Bantuan Teknis\n" +
                    "Ketik nomor pilihan Anda:";

    private Constants() {
        // Prevent instantiation
    }
}