package org.example.smartmuseum;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.smartmuseum.database.DatabaseConnection;

public class SmartMuseumAttendanceApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartmuseum/fxml/attendance-dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);

            // Load CSS
            scene.getStylesheets().add(getClass().getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm());

            // Configure stage
            primaryStage.setTitle("Smart Museum - Sistem Presensi QR Code");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

            System.out.println("Smart Museum Attendance System started successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        try {
            DatabaseConnection.getInstance().closeConnection();
            System.out.println("Application closed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}