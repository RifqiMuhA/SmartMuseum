package org.example.smartmuseum.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class PresensiPegawaiView {

    @FXML
    private Button button1;  // Tombol pertama dari FXML

    @FXML
    private Button button2;  // Tombol kedua dari FXML

    // Method untuk menangani klik tombol pertama
    @FXML
    private void handleButton1Click() {
        System.out.println("Tombol pertama diklik");
    }

    // Method untuk menangani klik tombol kedua
    @FXML
    private void handleButton2Click() {
        System.out.println("Tombol kedua diklik");
    }
}
