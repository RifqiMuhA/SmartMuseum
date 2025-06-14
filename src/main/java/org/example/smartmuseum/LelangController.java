package org.example.smartmuseum;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

public class LelangController {

    @FXML private ImageView logo;
    @FXML private Label LabelLogo;
    @FXML private TextField Search;
    @FXML private ImageView SearchLogo;
    @FXML private ImageView User;

    @FXML private ImageView Gambar1Dinamis;
    @FXML private ImageView Gambar2Dinamis;
    @FXML private ImageView Gambar3Dinamis;
    @FXML private ImageView GambarUtamaDinamis;

    @FXML private Label NamaBenda;
    @FXML private Label CurrentBidStatis;
    @FXML private Label JumlahBidDinamis;
    @FXML private Label HargaDinamis;
    @FXML private Label YourBid;
    @FXML private TextField InputBid;
    @FXML private Button ButtonBid;
    @FXML private Label Warning;

    @FXML private Label AuctionInformation;
    @FXML private Label AuctionId;
    @FXML private Label AuctionDate;
    @FXML private Label LastAuctionUpdate;
    @FXML private Label NamaPemilikDinamis;
    @FXML private Label AuctionIdDinamis;
    @FXML private Label DateDinamis;
    @FXML private Label WaktuDinamis;

    @FXML private Label TimeLeftStatis;
    @FXML private Label DetikStatis;
    @FXML private Label DetikDinamis;

    // Tambahkan logika controller sesuai kebutuhan di sini

    @FXML
    public void initialize() {
        // Contoh: set label awal
        NamaBenda.setText("Lukisan Modern");
    }
}
