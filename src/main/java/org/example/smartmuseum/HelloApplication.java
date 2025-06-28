package org.example.smartmuseum;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Deprecated - Use SmartMuseumAttendanceApp instead
 */
@Deprecated
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("⚠️ HelloApplication is deprecated. Redirecting to SmartMuseumAttendanceApp...");

        // Redirect to main application
        SmartMuseumAttendanceApp mainApp = new SmartMuseumAttendanceApp();
        mainApp.start(stage);
    }

    public static void main(String[] args) {
        System.out.println("⚠️ Please use SmartMuseumAttendanceApp as main entry point");
        launch(args);
    }
}
