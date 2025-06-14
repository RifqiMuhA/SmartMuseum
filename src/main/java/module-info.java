module org.example.smartmuseum {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    // Database
    requires java.sql;

    // QR Code
    requires com.google.zxing;
    requires com.google.zxing.javase;
    opens org.example.smartmuseum.controller to javafx.fxml;
    opens org.example.smartmuseum.view to javafx.fxml;

    // Java base modules
    requires java.desktop;
    requires java.base;

    // Export packages to JavaFX FXML
    exports org.example.smartmuseum to javafx.fxml, javafx.graphics;
    exports org.example.smartmuseum.controller to javafx.fxml;
    exports org.example.smartmuseum.model.entity to javafx.base;
    exports org.example.smartmuseum.model.enums to javafx.base;

    // Open packages for reflection (untuk FXML)
    opens org.example.smartmuseum to javafx.fxml;
    opens org.example.smartmuseum.controller to javafx.fxml;
    opens org.example.smartmuseum.model.entity to javafx.base;
    opens org.example.smartmuseum.model.enums to javafx.base;
    exports org.example.smartmuseum.view;
}
