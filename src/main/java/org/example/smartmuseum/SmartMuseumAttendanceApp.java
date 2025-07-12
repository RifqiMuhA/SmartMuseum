package org.example.smartmuseum;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.smartmuseum.database.DatabaseConnection;
import org.example.smartmuseum.server.SmartMuseumServer;
import org.example.smartmuseum.util.SessionManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main application entry point for Smart Museum
 * Updated to support multi-session system
 */
public class SmartMuseumAttendanceApp extends Application {

    private SmartMuseumServer server;
    private Timer sessionCleanupTimer;

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

        // Setup session cleanup timer
        setupSessionCleanup();

        // Load login scene (entry point untuk multi-session system)
        FXMLLoader fxmlLoader = new FXMLLoader(
                SmartMuseumAttendanceApp.class.getResource("/org/example/smartmuseum/fxml/login.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        // Add CSS
        scene.getStylesheets().add(
                getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm()
        );

        stage.setTitle("Smart Museum - Login");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();

        // Handle application close
        stage.setOnCloseRequest(event -> {
            System.out.println("🔄 Application shutdown initiated...");

            // Stop server
            stopServer();

            // Cleanup all active sessions
            cleanupAllSessions();

            // Stop cleanup timer
            if (sessionCleanupTimer != null) {
                sessionCleanupTimer.cancel();
            }

            System.out.println("👋 Application shutdown complete");
            System.exit(0);
        });

        System.out.println("🚀 Smart Museum Application started successfully");
        System.out.println("📱 Multi-session system ready for multiple users");
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
            System.out.println("🚀 Smart Museum Server started on background thread");
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

    private void setupSessionCleanup() {
        // Setup automatic session cleanup every 30 minutes
        sessionCleanupTimer = new Timer("SessionCleanupTimer", true);
        sessionCleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    int beforeCount = SessionManager.getActiveSessionCount();
                    SessionManager.cleanupInactiveSessions(30); // 30 minutes
                    int afterCount = SessionManager.getActiveSessionCount();

                    if (beforeCount != afterCount) {
                        System.out.println("🧹 Session cleanup: " + (beforeCount - afterCount) +
                                " inactive sessions removed");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error during session cleanup: " + e.getMessage());
                }
            }
        }, 30 * 60 * 1000, 30 * 60 * 1000); // 30 minutes interval

        System.out.println("⏰ Session cleanup timer started (30 minutes interval)");
    }

    private void cleanupAllSessions() {
        try {
            int activeCount = SessionManager.getActiveSessionCount();
            if (activeCount > 0) {
                System.out.println("🧹 Cleaning up " + activeCount + " active sessions...");

                // Get all active sessions and cleanup
                var sessions = SessionManager.getAllActiveSessions();
                sessions.values().forEach(session -> {
                    try {
                        session.destroySession();
                    } catch (Exception e) {
                        System.err.println("Error cleaning up session: " + e.getMessage());
                    }
                });

                System.out.println("✅ All sessions cleaned up");
            }
        } catch (Exception e) {
            System.err.println("❌ Error during session cleanup: " + e.getMessage());
        }
    }

    /**
     * Print session statistics untuk debugging
     */
    public static void printSessionStats() {
        int totalSessions = SessionManager.getActiveSessionCount();
        int loggedInUsers = SessionManager.getLoggedInUserCount();

        System.out.println("📊 Session Statistics:");
        System.out.println("   Total Sessions: " + totalSessions);
        System.out.println("   Logged In Users: " + loggedInUsers);

        if (totalSessions > 0) {
            System.out.println("   Active Sessions:");
            SessionManager.getAllActiveSessions().forEach((id, session) -> {
                System.out.println("     " + id.substring(0, 8) + "... -> " +
                        session.getCurrentUsername() + " (" +
                        session.getCurrentUserRole() + ")");
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}