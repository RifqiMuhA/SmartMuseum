package org.example.smartmuseum;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.smartmuseum.database.DatabaseConnection;
import org.example.smartmuseum.server.SmartMuseumServer;
import org.example.smartmuseum.util.SessionManager;

import java.io.IOException;

/**
 * Main application entry point for Smart Museum
 * This is the ONLY entry point for the entire application
 */
public class SmartMuseumAttendanceApp extends Application {

    private SmartMuseumServer server;

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database connection
        try {
            DatabaseConnection.getInstance().getConnection();
            System.out.println("✅ Database connection established");
        } catch (Exception e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }

        // Start the server
        startServer();

        // Load login scene
        FXMLLoader fxmlLoader = new FXMLLoader(SmartMuseumAttendanceApp.class.getResource("/org/example/smartmuseum/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        stage.setTitle("Smart Museum - SeniMatic");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Handle application close
        stage.setOnCloseRequest(event -> {
            stopServer();
//            SessionManager.getInstance().clearSession();
            System.exit(0);
        });
    }

    private void startServer() {
        try {
            server = new SmartMuseumServer();
            Thread serverThread = new Thread(() -> {
                try {
                    server.startServer();
                } catch (Exception e) {
                    System.err.println("❌ Server error: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            System.out.println("🚀 Smart Museum Server started");
        } catch (Exception e) {
            System.err.println("❌ Failed to start server: " + e.getMessage());
        }
    }

    private void stopServer() {
        if (server != null) {
            try {
                server.stopServer();
                System.out.println("🛑 Smart Museum Server stopped");
            } catch (Exception e) {
                System.err.println("❌ Error stopping server: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("🏛️ Starting Smart Museum Application...");
        launch();
    }
}
