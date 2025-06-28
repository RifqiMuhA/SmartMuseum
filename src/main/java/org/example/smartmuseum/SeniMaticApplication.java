package org.example.smartmuseum;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Deprecated - Use SmartMuseumAttendanceApp instead
 */
public class SeniMaticApplication extends Application {

    private static final String APP_TITLE = "SeniMatic - Smart Museum System";
    private static final String APP_VERSION = "1.0.0";
    private static final int MIN_WIDTH = 1000;
    private static final int MIN_HEIGHT = 700;

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("⚠️ SeniMaticApplication is deprecated. Redirecting to SmartMuseumAttendanceApp...");

        // Redirect to main application
        SmartMuseumAttendanceApp mainApp = new SmartMuseumAttendanceApp();
        mainApp.start(stage);
    }

    /**
     * Setup primary stage properties
     */
    private void setupPrimaryStage(Stage stage) {
        stage.setTitle(APP_TITLE + " v" + APP_VERSION);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        // Handle close request
        stage.setOnCloseRequest(event -> {
            handleApplicationExit();
        });
    }

    /**
     * Load initial scene (chatbot for demo)
     */
    private void loadInitialScene(Stage stage) throws IOException {
        try {
            // Load FXML from resources
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/org/example/smartmuseum/fxml/chatbot.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load(), MIN_WIDTH, MIN_HEIGHT);

            // Load CSS if exists
            loadStylesheets(scene);

            stage.setScene(scene);

        } catch (IOException e) {
            System.err.println("Failed to load initial scene: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Load CSS stylesheets
     */
    private void loadStylesheets(Scene scene) {
        try {
            String cssPath = "/org/example/smartmuseum/css/application.css";
            String css = getClass().getResource(cssPath).toExternalForm();
            scene.getStylesheets().add(css);
            System.out.println("Stylesheets loaded successfully");
        } catch (Exception e) {
            System.out.println("Could not load stylesheets: " + e.getMessage());
        }
    }

    /**
     * Handle application exit
     */
    private void handleApplicationExit() {
        try {
            System.out.println("Application cleanup completed");
            Platform.exit();
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error during application exit: " + e.getMessage());
            Platform.exit();
            System.exit(1);
        }
    }

    /**
     * Show error dialog
     */
    private void showErrorDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Warning");
        alert.setContentText(message);
        alert.show();
    }

    public static void main(String[] args) {
        System.out.println("⚠️ Please use SmartMuseumAttendanceApp as main entry point");
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        System.out.println("SeniMatic Application stopping...");
        handleApplicationExit();
        super.stop();
    }
}
