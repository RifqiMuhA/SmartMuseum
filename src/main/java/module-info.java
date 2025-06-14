module org.example.smartmuseum {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.smartmuseum.controller to javafx.fxml;
    opens org.example.smartmuseum.view to javafx.fxml;

    exports org.example.smartmuseum.view;
}
