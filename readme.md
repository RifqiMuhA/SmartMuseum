<p align="center">
  <img src="src/main/resources/img/logo.png" alt="SeniMatic Logo" width="100"/>
</p>

# SeniMatic - Sistem Museum Pintar

![Java](https://img.shields.io/badge/Java-11+-blue.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-UI_Framework-orange)
![Database](https://img.shields.io/badge/MySQL-8.0-green)
![Platform](https://img.shields.io/badge/Platform-Desktop-blueviolet)

Aplikasi JavaFX komprehensif untuk mengelola galeri seni dan sistem lelang dengan fungsionalitas QR code terintegrasi dan asisten chatbot cerdas.

## Fitur Utama

### Fungsionalitas Inti
- **Manajemen Pengguna**: Sistem autentikasi multi-role (Admin, Staff, Pengunjung)
- **Manajemen Karya Seni**: Katalog lengkap karya seni dengan informasi seniman
- **Sistem Lelang**: Platform bidding real-time dengan manajemen lelang otomatis
- **Manajemen Karyawan**: Administrasi staff dengan tracking absensi berbasis QR
- **Sistem QR Code**: Generate dan scan QR code untuk karya seni dan absensi karyawan
- **Chatbot Cerdas**: Asisten interaktif dengan respon suara dan alur percakapan

### Fitur Teknis
- **Integrasi Database**: Database MySQL dengan persistensi data komprehensif
- **Sistem Suara**: Respon audio untuk interaksi chatbot
- **Update Real-time**: Bidding lelang dan monitoring absensi secara langsung
- **Manajemen Sesi**: Sesi pengguna persisten dan riwayat chat
- **UI Responsif**: Antarmuka JavaFX modern dengan styling CSS

## Teknologi yang Digunakan

### Bahasa Pemrograman
- **Java**: Bahasa pemrograman utama untuk pengembangan aplikasi

### Framework GUI
- **JavaFX**: Framework untuk antarmuka pengguna yang interaktif dan modern

### Database
- **MySQL**: Sistem manajemen basis data relasional

### Arsitektur
- **Model-View-Controller (MVC)**: Pola arsitektur untuk pemisahan logika aplikasi
- **Client-Server**: Arsitektur terdistribusi untuk skalabilitas

### Library Pendukung
- **MySQL Connector/J**: Koneksi antara Java dan database MySQL
- **ZXing (Zebra Crossing)**: Pembuatan dan pemindaian QR Code
- **JavaCV (OpenCV wrapper)**: Pengolahan kamera dalam fitur QR
- **FreeTTS / Java Speech API**: Fitur Text to Speech (TTS) pada chatbot
- **ZegoCloud**: Sistem lelang berbasis website

### Versi Kontrol
- **Git**: Sistem kontrol versi terdistribusi
- **GitHub**: Platform kolaborasi pengembangan tim

## Skema Database

Sistem menggunakan 11 tabel yang saling terhubung:
- Manajemen pengguna (Users, Employees, Attendance)
- Manajemen konten (Artists, Artworks)
- Sistem lelang (Auctions, Bids)
- Sistem chatbot (Chat_Logs, Conversation_Flows, Flow_Nodes, Node_Options)

## Instalasi

### Prasyarat
- Java 11 atau lebih tinggi
- MySQL 8.0+
- Maven 3.6+
- JavaFX SDK
- Library OpenCV
- Perangkat kamera (untuk scanning QR)

### Langkah Setup

1. Clone repository
```bash
git clone https://github.com/username/senimatic-javafx.git
cd senimatic-javafx
```

2. Konfigurasi database
```sql
CREATE DATABASE smartmuseum;
-- Import skema SQL yang disediakan
```

3. Update konfigurasi database
```properties
# src/main/java/org/example/smartmuseum/database/DatabaseConnection.java
private static final String URL = "jdbc:mysql://localhost:3306/smartmuseum";
private static final String USERNAME = "root";
private static final String PASSWORD = "";
```

4. Install dependencies
```bash
mvn clean install
// atau lewat UI intellij
```


## Struktur Proyek

```
src/main/java/org/example/smartmuseum/
├── controller/          # Controller JavaFX
├── model/
│   ├── entity/         # Entitas data
│   ├── service/        # Logika bisnis
│   ├── concrete/       # Implementasi konkret
│   └── abstracts/      # Kelas abstrak
├── database/           # Layer DAO
├── util/              # Kelas utilitas
└── SmartMuseumAttendanceApp.java <-- pintu masuk

src/main/resources/
├── fxml/              # Layout FXML
├── css/               # Stylesheet
├── sounds/            # File audio
└── images/            # Aset gambar
```

## Komponen Utama

### Sistem QR Code
- **Generasi**: Membuat QR code unik untuk karya seni dan karyawan
- **Scanning**: Deteksi QR code berbasis kamera menggunakan OpenCV
- **Pemrosesan**: Library ZXing untuk decoding QR code
- **Integrasi**: Tracking absensi dan informasi karya seni yang seamless

### Sistem Chatbot
- **Alur Percakapan**: Manajemen dialog berbasis database
- **Integrasi Suara**: Respon audio menggunakan JavaFX MediaPlayer
- **Manajemen Sesi**: Riwayat chat persisten dan konteks
- **Navigasi**: Integrasi langsung dengan fitur aplikasi

### Platform Lelang
- **Bidding Real-time**: Partisipasi lelang secara langsung
- **Manajemen Otomatis**: Penjadwalan mulai/selesai lelang
- **Tracking Bid**: Riwayat bidding lengkap
- **Verifikasi Pengguna**: Autentikasi bidder yang aman

## Penggunaan

### Dashboard Admin
- Akses semua fitur manajemen
- Monitor aktivitas sistem
- Generate laporan dan analitik

### Operasi QR Code
- Generate QR code untuk karya seni baru
- Scan QR code karyawan untuk absensi
- Print QR code untuk penempatan fisik

### Interaksi Chatbot
- Panduan suara informasi museum
- Penemuan karya seni interaktif
- Panduan partisipasi lelang
- Bantuan dukungan teknis