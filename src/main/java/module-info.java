module org.example.smartmuseum {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.smartmuseum to javafx.fxml;
    exports org.example.smartmuseum;
}