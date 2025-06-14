module org.example.smartmuseum {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    // Database
    requires java.sql;

    // QR Code (ZXing)
    requires com.google.zxing;
    requires com.google.zxing.javase;

    // Java base modules
    requires java.desktop;
    requires java.base;

    // Export packages
    exports org.example.smartmuseum;
    exports org.example.smartmuseum.controller;
    exports org.example.smartmuseum.model.entity;
    exports org.example.smartmuseum.model.enums;
    exports org.example.smartmuseum.view;

    // Open packages (untuk refleksi oleh FXMLLoader)
    opens org.example.smartmuseum to javafx.fxml;
    opens org.example.smartmuseum.controller to javafx.fxml;
    opens org.example.smartmuseum.view to javafx.fxml;
    opens org.example.smartmuseum.model.entity to javafx.base;
    opens org.example.smartmuseum.model.enums to javafx.base;
}
