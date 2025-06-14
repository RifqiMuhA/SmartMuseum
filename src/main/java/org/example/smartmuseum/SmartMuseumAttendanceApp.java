package org.example.smartmuseum;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.smartmuseum.database.DatabaseConnection;

public class SmartMuseumAttendanceApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        try {
            // Start with welcome screen
            showWelcomeScreen();

            // Configure stage
            primaryStage.setTitle("Smart Museum - Sistem Presensi QR Code");
            primaryStage.setMaximized(true);
            primaryStage.show();

            System.out.println("✅ Smart Museum Application started successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Failed to start application: " + e.getMessage());
        }
    }

    public static void showWelcomeScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SmartMuseumAttendanceApp.class.getResource("/org/example/smartmuseum/fxml/welcome.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            scene.getStylesheets().add(
                    SmartMuseumAttendanceApp.class.getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Smart Museum - Welcome");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SmartMuseumAttendanceApp.class.getResource("/org/example/smartmuseum/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 900);
            scene.getStylesheets().add(
                    SmartMuseumAttendanceApp.class.getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Smart Museum - Dashboard");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() throws Exception {
        try {
            DatabaseConnection.getInstance().closeConnection();
            System.out.println("✅ Application closed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}