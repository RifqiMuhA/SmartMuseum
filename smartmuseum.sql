-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Jul 12, 2025 at 09:02 AM
-- Server version: 8.0.30
-- PHP Version: 8.3.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `smartmuseum`
--

-- --------------------------------------------------------

--
-- Table structure for table `artists`
--

CREATE TABLE `artists` (
  `artist_id` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `biography` text,
  `birth_year` int DEFAULT NULL,
  `nationality` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `artists`
--

INSERT INTO `artists` (`artist_id`, `name`, `biography`, `birth_year`, `nationality`) VALUES
(1, 'Raden Saleh', 'Pelukis Indonesia terkenal abad ke-19', 1811, 'Indonesia'),
(2, 'Affandi', 'Pelukis ekspresionisme Indonesia', 1907, 'Indonesia'),
(3, 'Basuki Abdullah', 'Pelukis realis Indonesia', 1915, 'Indonesia');

-- --------------------------------------------------------

--
-- Table structure for table `artworks`
--

CREATE TABLE `artworks` (
  `artwork_id` int NOT NULL,
  `title` varchar(200) NOT NULL,
  `artist_id` int DEFAULT NULL,
  `year` int DEFAULT NULL,
  `technique` varchar(100) DEFAULT NULL,
  `description` text,
  `qr_code` varchar(255) DEFAULT NULL,
  `image_path` varchar(500) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `artwork_type` varchar(50) DEFAULT 'Lukisan'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `artworks`
--

INSERT INTO `artworks` (`artwork_id`, `title`, `artist_id`, `year`, `technique`, `description`, `qr_code`, `image_path`, `created_at`, `artwork_type`) VALUES
(1, 'Penangkapan Pangeran Diponegoro', 1, 1857, 'Oil on Canvas', 'Lukisan sejarah terkenal karya Raden Saleh', 'ART001', '/images/raden_saleh_1.jpg', '2025-06-20 10:48:05', 'Lukisan'),
(2, 'Pemandangan Gunung Merapi', 2, 1960, 'Oil on Canvas', 'Lukisan ekspresionisme pemandangan alam', 'ART002', '/images/affandi_1.jpg', '2025-06-20 10:48:05', 'Patung'),
(3, 'Potret Wanita Jawa', 3, 1950, 'Oil on Canvas', 'Lukisan realis potret tradisional', 'ART003', '/images/basuki_1.jpg', '2025-06-20 10:48:05', 'Keramik'),
(4, 'Pemandangan Gunung', 1, 1965, 'Cat Minyak', 'Lukisan pemandangan gunung yang indah dengan teknik impresionisme', NULL, '/images/lukisan1.jpg', '2025-06-24 17:35:06', 'Lukisan'),
(5, 'Patung Garuda', 2, 1970, 'Perunggu', 'Patung Garuda yang menggambarkan kekuatan dan keagungan', NULL, '/images/patung1.jpg', '2025-06-24 17:35:06', 'Patung'),
(6, 'Vas Bunga Tradisional', 3, 1980, 'Keramik Glasir', 'Vas bunga dengan motif tradisional Indonesia', '', '/images/awan.jpg', '2025-06-24 17:35:06', 'Keramik');

-- --------------------------------------------------------

--
-- Table structure for table `attendance`
--

CREATE TABLE `attendance` (
  `attendance_id` int NOT NULL,
  `employee_id` int DEFAULT NULL,
  `check_in` timestamp NULL DEFAULT NULL,
  `check_out` timestamp NULL DEFAULT NULL,
  `date` date DEFAULT NULL,
  `status` enum('present','absent','late') DEFAULT 'present'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `attendance`
--

INSERT INTO `attendance` (`attendance_id`, `employee_id`, `check_in`, `check_out`, `date`, `status`) VALUES
(1, 3, '2025-06-28 16:49:03', '2025-06-28 16:50:10', '2025-06-28', 'present'),
(2, 4, '2025-06-28 17:08:34', '2025-06-28 17:09:35', '2025-06-29', 'present'),
(3, 4, '2025-06-30 06:22:18', NULL, '2025-06-30', 'present'),
(4, 4, '2025-06-30 22:50:41', NULL, '2025-07-01', 'present'),
(5, 15, '2025-07-01 07:53:10', NULL, '2025-07-01', 'present'),
(6, 4, '2025-07-02 07:30:32', NULL, '2025-07-02', 'present'),
(7, 4, '2025-07-03 06:39:59', NULL, '2025-07-03', 'present'),
(8, 4, '2025-07-04 05:06:23', '2025-07-04 05:41:45', '2025-07-04', 'present'),
(9, 2, '2025-07-08 12:11:31', '2025-07-08 12:12:58', '2025-07-08', 'present'),
(10, 3, '2025-07-08 12:12:53', '2025-07-08 12:13:54', '2025-07-08', 'present'),
(11, 4, '2025-07-08 12:13:51', '2025-07-08 12:14:54', '2025-07-08', 'present'),
(12, 2, '2025-07-09 09:59:44', NULL, '2025-07-09', 'present'),
(13, 3, '2025-07-09 09:59:51', NULL, '2025-07-09', 'present'),
(14, 3, '2025-07-10 06:46:39', NULL, '2025-07-10', 'present');

-- --------------------------------------------------------

--
-- Table structure for table `auctions`
--

CREATE TABLE `auctions` (
  `auction_id` int NOT NULL,
  `artwork_id` int DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `starting_bid` decimal(15,2) DEFAULT NULL,
  `current_bid` decimal(15,2) DEFAULT NULL,
  `status` enum('upcoming','active','ended') DEFAULT 'upcoming',
  `winner_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `auctions`
--

INSERT INTO `auctions` (`auction_id`, `artwork_id`, `start_date`, `end_date`, `starting_bid`, `current_bid`, `status`, `winner_id`, `created_at`, `updated_at`) VALUES
(1, 1, '2024-01-15 10:00:00', '2025-07-03 13:44:13', 50000000.00, 75000000.00, 'ended', NULL, '2025-06-20 10:48:05', '2025-07-03 06:44:13'),
(2, 2, '2024-01-10 09:00:00', '2024-01-15 17:00:00', 25000000.00, 35000000.00, 'ended', 3, '2025-06-20 10:48:05', '2025-07-03 00:27:49'),
(3, 3, '2024-01-25 11:00:00', '2025-07-03 09:25:38', 30000000.00, 30000000.00, 'ended', NULL, '2025-06-20 10:48:05', '2025-07-03 02:25:38'),
(4, 2, NULL, '2025-07-03 09:34:16', 100000.00, 100000.00, 'ended', NULL, '2025-07-03 02:24:12', '2025-07-03 02:34:16'),
(5, 5, '2025-07-03 13:45:15', '2025-07-04 06:07:06', 5000000.00, 5000000.00, 'ended', NULL, '2025-07-03 06:44:55', '2025-07-03 23:07:06'),
(6, 6, '2025-07-04 06:07:08', '2025-07-04 08:14:24', 6000000.00, 6000000.00, 'ended', NULL, '2025-07-03 23:06:55', '2025-07-04 01:14:23'),
(7, 5, '2025-07-04 08:14:29', '2025-07-04 08:15:10', 5000000.00, 5000000.00, 'ended', NULL, '2025-07-04 01:14:15', '2025-07-04 01:15:09'),
(8, 5, NULL, NULL, 2000000.00, 2000000.00, 'upcoming', NULL, '2025-07-04 03:00:57', '2025-07-04 03:00:57'),
(9, 4, '2025-07-04 10:31:00', '2025-07-04 10:32:01', 2000000.00, 2000000.00, 'ended', NULL, '2025-07-04 03:04:52', '2025-07-04 03:32:00'),
(10, 6, '2025-07-04 10:29:12', '2025-07-04 10:30:37', 6000000.00, 6000000.00, 'ended', NULL, '2025-07-04 03:05:10', '2025-07-04 03:30:36'),
(11, 4, '2025-07-04 10:17:45', '2025-07-04 10:18:29', 5000000.00, 5000000.00, 'ended', NULL, '2025-07-04 03:16:32', '2025-07-04 03:18:28'),
(12, 1, '2025-07-04 11:11:53', '2025-07-04 11:16:23', 6000000.00, 6000000.00, 'ended', NULL, '2025-07-04 04:10:59', '2025-07-04 04:16:22'),
(13, 2, '2025-07-04 11:18:05', '2025-07-04 11:18:48', 6000000.00, 6000000.00, 'ended', NULL, '2025-07-04 04:17:47', '2025-07-04 04:18:47');

-- --------------------------------------------------------

--
-- Table structure for table `bids`
--

CREATE TABLE `bids` (
  `bid_id` int NOT NULL,
  `auction_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `bid_amount` decimal(15,2) DEFAULT NULL,
  `bid_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `chat_logs`
--

CREATE TABLE `chat_logs` (
  `log_id` int NOT NULL,
  `session_id` int DEFAULT NULL,
  `user_input` varchar(255) DEFAULT NULL,
  `bot_response` text,
  `node_id` int DEFAULT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `chat_logs`
--

INSERT INTO `chat_logs` (`log_id`, `session_id`, `user_input`, `bot_response`, `node_id`, `timestamp`) VALUES
(37, 11, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 08:47:25'),
(38, 11, '1', NULL, NULL, '2025-06-28 08:47:27'),
(39, 11, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-06-28 08:47:28'),
(40, 11, '2', NULL, NULL, '2025-06-28 08:47:29'),
(41, 11, NULL, '          Cari Berdasarkan Kategori\n\nKategori artwork:\n• Lukisan - Karya seni rupa 2D\n• Patung - Karya seni rupa 3D\n• Keramik - Seni kerajinan tanah liat\n• Batik - Seni tekstil tradisional\n\nKetik kategori atau \'menu\' untuk kembali.', NULL, '2025-06-28 08:47:30'),
(42, 11, 'menu', NULL, NULL, '2025-06-28 08:47:46'),
(43, 11, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 08:47:47'),
(44, 11, '2', NULL, NULL, '2025-06-28 08:48:00'),
(45, 11, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-06-28 08:48:00'),
(46, 11, '4', NULL, NULL, '2025-06-28 08:48:02'),
(47, 11, NULL, '     Mulai Bidding\n\nCara bidding:\n1. Pilih artwork yang diminati\n2. Tentukan batas maksimal bid\n3. Klik tombol \'Bid Now\'\n4. Konfirmasi bid amount\n5. Tunggu hasil lelang\n\nKetik \'menu\' untuk kembali.', NULL, '2025-06-28 08:48:03'),
(48, 11, '1', NULL, NULL, '2025-06-28 08:49:48'),
(49, 11, NULL, '           Registrasi Akun\n\nLangkah registrasi:\n1. Kunjungi halaman registrasi\n2. Isi data pribadi lengkap\n3. Upload foto KTP/identitas\n4. Verifikasi email\n5. Tunggu konfirmasi admin\n\nKetik \'menu\' untuk kembali.', NULL, '2025-06-28 08:49:49'),
(50, 11, 'menu', NULL, NULL, '2025-06-28 08:49:51'),
(51, 11, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 08:49:52'),
(52, 12, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 08:49:55'),
(53, 12, '1', NULL, NULL, '2025-06-28 08:49:57'),
(54, 12, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-06-28 08:49:57'),
(55, 12, '3', NULL, NULL, '2025-06-28 08:49:59'),
(56, 12, NULL, '    Artwork Terpopuler\n\nTop 3 artwork favorit pengunjung:\n1. \'Pemandangan Borobudur\' - Affandi\n2. \'Penangkapan Diponegoro\' - Raden Saleh\n3. \'Gadis Bali\' - Basuki Abdullah\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-06-28 08:50:00'),
(57, 13, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 08:59:27'),
(58, 13, '1', NULL, NULL, '2025-06-28 08:59:34'),
(59, 13, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-06-28 08:59:34'),
(60, 13, '1', NULL, NULL, '2025-06-28 08:59:40'),
(61, 13, NULL, '         Cari Berdasarkan Seniman\n\nSeniman terkenal di koleksi kami:\n• Affandi - Pelukis ekspresionisme\n• Raden Saleh - Pelukis romantisme\n• Basuki Abdullah - Pelukis realis\n• Sudjojono - Pelukis naturalis\n\nKetik nama seniman atau \'menu\' untuk kembali.', NULL, '2025-06-28 08:59:41'),
(62, 13, 'Affandi', NULL, NULL, '2025-06-28 08:59:51'),
(63, 13, NULL, 'Mohon masukkan nomor pilihan yang valid atau ketik \'menu\' untuk kembali ke menu utama.', NULL, '2025-06-28 08:59:51'),
(64, 13, '1', NULL, NULL, '2025-06-28 09:00:07'),
(65, 13, NULL, '         Cari Berdasarkan Seniman\n\nSeniman terkenal di koleksi kami:\n• Affandi - Pelukis ekspresionisme\n• Raden Saleh - Pelukis romantisme\n• Basuki Abdullah - Pelukis realis\n• Sudjojono - Pelukis naturalis\n\nKetik nama seniman atau \'menu\' untuk kembali.', NULL, '2025-06-28 09:00:08'),
(66, 13, 'menu', NULL, NULL, '2025-06-28 09:00:10'),
(67, 13, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:00:11'),
(68, 13, '1', NULL, NULL, '2025-06-28 09:00:12'),
(69, 13, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-06-28 09:00:12'),
(70, 13, 'menu', NULL, NULL, '2025-06-28 09:00:26'),
(71, 13, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:00:27'),
(72, 13, '2', NULL, NULL, '2025-06-28 09:00:29'),
(73, 13, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-06-28 09:00:30'),
(74, 13, '4', NULL, NULL, '2025-06-28 09:00:36'),
(75, 13, NULL, '     Mulai Bidding\n\nCara bidding:\n1. Pilih artwork yang diminati\n2. Tentukan batas maksimal bid\n3. Klik tombol \'Bid Now\'\n4. Konfirmasi bid amount\n5. Tunggu hasil lelang\n\nKetik \'menu\' untuk kembali.', NULL, '2025-06-28 09:00:37'),
(76, 13, 'menu', NULL, NULL, '2025-06-28 09:00:42'),
(77, 13, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:00:43'),
(78, 13, 'menu', NULL, NULL, '2025-06-28 09:00:47'),
(79, 13, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:00:47'),
(80, 13, '3', NULL, NULL, '2025-06-28 09:00:52'),
(81, 13, NULL, '        Info Galeri\n\nJam Operasional: 09:00 - 17:00\nLokasi: Jl. Seni Raya No. 123\nTelp: (021) 1234-5678\nEmail: info@senimatic.com\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-06-28 09:00:53'),
(82, 13, 'menu', NULL, NULL, '2025-06-28 09:00:57'),
(83, 13, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:00:57'),
(84, 13, '4', NULL, NULL, '2025-06-28 09:00:59'),
(85, 13, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-06-28 09:00:59'),
(86, 13, '1', NULL, NULL, '2025-06-28 09:01:07'),
(87, 13, NULL, '          Masalah Login\n\nSolusi umum masalah login:\n• Pastikan username/email benar\n• Cek caps lock pada password\n• Clear browser cache & cookies\n• Coba browser lain\n• Reset password jika perlu\n\nMasih bermasalah? Hubungi admin.\nKetik \'menu\' untuk kembali.', NULL, '2025-06-28 09:01:07'),
(88, 13, 'menu', NULL, NULL, '2025-06-28 09:01:29'),
(89, 13, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:01:30'),
(90, 13, '4', NULL, NULL, '2025-06-28 09:01:33'),
(91, 13, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-06-28 09:01:33'),
(92, 13, '2', NULL, NULL, '2025-06-28 09:01:35'),
(93, 13, NULL, '      Reset Password\n\nCara reset password:\n1. Klik \'Lupa Password\' di halaman login\n2. Masukkan email terdaftar\n3. Cek email untuk link reset\n4. Klik link dalam 15 menit\n5. Buat password baru\n\nPassword harus min. 8 karakter.\nKetik \'menu\' untuk kembali.', NULL, '2025-06-28 09:01:35'),
(94, 13, 'menu', NULL, NULL, '2025-06-28 09:01:40'),
(95, 13, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:01:40'),
(96, 13, '4', NULL, NULL, '2025-06-28 09:01:47'),
(97, 13, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-06-28 09:01:47'),
(98, 13, '3', NULL, NULL, '2025-06-28 09:01:48'),
(99, 13, NULL, '            Hubungi Admin\n\nKontak admin SeniMatic:\n       Email: admin@senimatic.com\n         WhatsApp: +62 812-3456-7890\n       Telepon: (021) 1234-5678\n         Jam kerja: 09:00 - 17:00 WIB\n\nRespon dalam 1x24 jam.\nKetik \'menu\' untuk kembali.', NULL, '2025-06-28 09:01:49'),
(100, 13, 'menu', NULL, NULL, '2025-06-28 09:01:51'),
(101, 13, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:01:52'),
(102, 13, '2', NULL, NULL, '2025-06-28 09:03:05'),
(103, 13, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-06-28 09:03:06'),
(104, 13, '1', NULL, NULL, '2025-06-28 09:03:08'),
(105, 13, NULL, '           Registrasi Akun\n\nLangkah registrasi:\n1. Kunjungi halaman registrasi\n2. Isi data pribadi lengkap\n3. Upload foto KTP/identitas\n4. Verifikasi email\n5. Tunggu konfirmasi admin\n\nKetik \'menu\' untuk kembali.', NULL, '2025-06-28 09:03:08'),
(106, 13, 'menu', NULL, NULL, '2025-06-28 09:03:19'),
(107, 13, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:03:20'),
(108, 14, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:05:12'),
(109, 14, '1', NULL, NULL, '2025-06-28 09:05:14'),
(110, 14, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-06-28 09:05:15'),
(111, 15, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:19:42'),
(112, 15, '2', NULL, NULL, '2025-06-28 09:19:44'),
(113, 15, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-06-28 09:19:44'),
(114, 16, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:35:57'),
(115, 16, '1', NULL, NULL, '2025-06-28 09:36:00'),
(116, 16, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-06-28 09:36:00'),
(117, 17, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:36:06'),
(118, 17, '2', NULL, NULL, '2025-06-28 09:36:10'),
(119, 17, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-06-28 09:36:11'),
(120, 17, '4', NULL, NULL, '2025-06-28 09:36:16'),
(121, 17, NULL, '     Mulai Bidding\n\nCara bidding:\n1. Pilih artwork yang diminati\n2. Tentukan batas maksimal bid\n3. Klik tombol \'Bid Now\'\n4. Konfirmasi bid amount\n5. Tunggu hasil lelang\n\nKetik \'menu\' untuk kembali.', NULL, '2025-06-28 09:36:17'),
(122, 18, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:36:26'),
(123, 18, '1', NULL, NULL, '2025-06-28 09:36:35'),
(124, 18, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-06-28 09:36:35'),
(125, 19, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:36:41'),
(126, 19, '2', NULL, NULL, '2025-06-28 09:36:44'),
(127, 19, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-06-28 09:36:44'),
(128, 20, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:37:13'),
(129, 21, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:45:46'),
(130, 21, '1', NULL, NULL, '2025-06-28 09:45:48'),
(131, 21, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-06-28 09:45:48'),
(132, 22, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:45:55'),
(133, 22, '2', NULL, NULL, '2025-06-28 09:45:56'),
(134, 22, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-06-28 09:45:57'),
(135, 23, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:47:18'),
(136, 23, '1', NULL, NULL, '2025-06-28 09:47:21'),
(137, 23, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-06-28 09:47:21'),
(138, 24, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 09:47:26'),
(139, 24, '2', NULL, NULL, '2025-06-28 09:47:30'),
(140, 24, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-06-28 09:47:31'),
(141, 25, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 11:22:50'),
(142, 25, '1', NULL, NULL, '2025-06-28 11:22:53'),
(143, 25, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-06-28 11:22:54'),
(144, 26, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 13:12:35'),
(145, 27, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 13:13:17'),
(146, 28, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 13:13:34'),
(147, 29, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-28 13:14:28'),
(148, 30, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-29 03:06:33'),
(149, 30, '1', NULL, NULL, '2025-06-29 03:06:38'),
(150, 30, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-06-29 03:06:38'),
(151, 31, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-29 03:07:43'),
(152, 32, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-06-30 06:09:03'),
(153, 33, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-04 05:05:03'),
(154, 33, '1', NULL, NULL, '2025-07-04 05:05:07'),
(155, 33, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-04 05:05:08'),
(156, 34, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-04 05:45:03'),
(157, 34, '1', NULL, NULL, '2025-07-04 05:45:07'),
(158, 34, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-04 05:45:07'),
(159, 35, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-04 05:45:22'),
(160, 35, '2', NULL, NULL, '2025-07-04 05:45:24'),
(161, 35, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-04 05:45:25'),
(162, 36, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:10:12'),
(163, 36, '3', NULL, NULL, '2025-07-06 02:10:39'),
(164, 36, NULL, '        Info Galeri\n\nJam Operasional: 09:00 - 17:00\nLokasi: Jl. Seni Raya No. 123\nTelp: (021) 1234-5678\nEmail: info@senimatic.com\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-06 02:10:40'),
(165, 36, '1', NULL, NULL, '2025-07-06 02:10:43'),
(166, 36, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 02:10:43'),
(167, 36, 'menu', NULL, NULL, '2025-07-06 02:10:49'),
(168, 36, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:10:50'),
(169, 36, '1', NULL, NULL, '2025-07-06 02:11:19'),
(170, 36, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 02:11:19'),
(171, 36, '1', NULL, NULL, '2025-07-06 02:11:30'),
(172, 36, NULL, '         Cari Berdasarkan Seniman\n\nSeniman terkenal di koleksi kami:\n• Affandi - Pelukis ekspresionisme\n• Raden Saleh - Pelukis romantisme\n• Basuki Abdullah - Pelukis realis\n• Sudjojono - Pelukis naturalis\n\nKetik nama seniman atau \'menu\' untuk kembali.', NULL, '2025-07-06 02:11:30'),
(173, 36, 'menu', NULL, NULL, '2025-07-06 02:11:47'),
(174, 36, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:11:47'),
(175, 36, '1', NULL, NULL, '2025-07-06 02:11:57'),
(176, 36, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 02:11:58'),
(177, 36, '2', NULL, NULL, '2025-07-06 02:11:59'),
(178, 36, NULL, '          Cari Berdasarkan Kategori\n\nKategori artwork:\n• Lukisan - Karya seni rupa 2D\n• Patung - Karya seni rupa 3D\n• Keramik - Seni kerajinan tanah liat\n• Batik - Seni tekstil tradisional\n\nKetik kategori atau \'menu\' untuk kembali.', NULL, '2025-07-06 02:12:00'),
(179, 36, 'menu', NULL, NULL, '2025-07-06 02:12:07'),
(180, 36, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:12:08'),
(181, 36, '1', NULL, NULL, '2025-07-06 02:12:08'),
(182, 36, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 02:12:09'),
(183, 36, '3', NULL, NULL, '2025-07-06 02:12:10'),
(184, 36, NULL, '    Artwork Terpopuler\n\nTop 3 artwork favorit pengunjung:\n1. \'Pemandangan Borobudur\' - Affandi\n2. \'Penangkapan Diponegoro\' - Raden Saleh\n3. \'Gadis Bali\' - Basuki Abdullah\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-06 02:12:11'),
(185, 36, 'menu', NULL, NULL, '2025-07-06 02:17:12'),
(186, 36, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:17:13'),
(187, 36, '1', NULL, NULL, '2025-07-06 02:17:23'),
(188, 36, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 02:17:23'),
(189, 36, 'menu', NULL, NULL, '2025-07-06 02:21:26'),
(190, 36, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:21:27'),
(191, 36, '4', NULL, NULL, '2025-07-06 02:21:31'),
(192, 36, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-07-06 02:21:32'),
(193, 36, '1', NULL, NULL, '2025-07-06 02:21:36'),
(194, 36, NULL, '          Masalah Login\n\nSolusi umum masalah login:\n• Pastikan username/email benar\n• Cek caps lock pada password\n• Clear browser cache & cookies\n• Coba browser lain\n• Reset password jika perlu\n\nMasih bermasalah? Hubungi admin.\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 02:21:37'),
(195, 36, 'menu', NULL, NULL, '2025-07-06 02:21:41'),
(196, 36, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:21:41'),
(197, 36, 'menu', NULL, NULL, '2025-07-06 02:21:44'),
(198, 36, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:21:44'),
(199, 36, '2', NULL, NULL, '2025-07-06 02:21:57'),
(200, 36, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 02:21:58'),
(201, 36, '2', NULL, NULL, '2025-07-06 02:22:00'),
(202, 36, NULL, '     Verifikasi Identitas\n\nDokumen yang diperlukan:\n• KTP/Paspor yang masih berlaku\n• NPWP (untuk lelang >50 juta)\n• Surat keterangan domisili\n• Foto selfie dengan KTP\n\nProses verifikasi: 1-3 hari kerja\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 02:22:01'),
(203, 36, '1', NULL, NULL, '2025-07-06 02:29:30'),
(204, 36, NULL, '           Registrasi Akun\n\nLangkah registrasi:\n1. Kunjungi halaman registrasi\n2. Isi data pribadi lengkap\n3. Upload foto KTP/identitas\n4. Verifikasi email\n5. Tunggu konfirmasi admin\n\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 02:29:30'),
(205, 36, 'menu', NULL, NULL, '2025-07-06 02:29:32'),
(206, 36, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:29:32'),
(207, 36, '1', NULL, NULL, '2025-07-06 02:29:34'),
(208, 36, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 02:29:34'),
(209, 36, '1', NULL, NULL, '2025-07-06 02:29:38'),
(210, 36, NULL, '         Cari Berdasarkan Seniman\n\nSeniman terkenal di koleksi kami:\n• Affandi - Pelukis ekspresionisme\n• Raden Saleh - Pelukis romantisme\n• Basuki Abdullah - Pelukis realis\n• Sudjojono - Pelukis naturalis\n\nKetik nama seniman atau \'menu\' untuk kembali.', NULL, '2025-07-06 02:29:39'),
(211, 36, 'menu', NULL, NULL, '2025-07-06 02:29:41'),
(212, 36, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:29:42'),
(213, 36, '2', NULL, NULL, '2025-07-06 02:29:42'),
(214, 36, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 02:29:43'),
(215, 36, '1', NULL, NULL, '2025-07-06 02:29:44'),
(216, 36, NULL, '           Registrasi Akun\n\nLangkah registrasi:\n1. Kunjungi halaman registrasi\n2. Isi data pribadi lengkap\n3. Upload foto KTP/identitas\n4. Verifikasi email\n5. Tunggu konfirmasi admin\n\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 02:29:45'),
(217, 37, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:49:05'),
(218, 38, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 02:55:58'),
(219, 39, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:06:23'),
(220, 40, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:06:42'),
(221, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:14:20'),
(222, 41, '1', NULL, NULL, '2025-07-06 03:14:34'),
(223, 41, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 03:14:35'),
(224, 41, '1', NULL, NULL, '2025-07-06 03:15:55'),
(225, 41, NULL, '         Cari Berdasarkan Seniman\n\nSeniman terkenal di koleksi kami:\n• Affandi - Pelukis ekspresionisme\n• Raden Saleh - Pelukis romantisme\n• Basuki Abdullah - Pelukis realis\n• Sudjojono - Pelukis naturalis\n\nKetik nama seniman atau \'menu\' untuk kembali.', NULL, '2025-07-06 03:15:55'),
(226, 41, 'menu', NULL, NULL, '2025-07-06 03:18:48'),
(227, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:18:49'),
(228, 41, '1', NULL, NULL, '2025-07-06 03:18:53'),
(229, 41, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 03:18:54'),
(230, 41, '2', NULL, NULL, '2025-07-06 03:19:00'),
(231, 41, NULL, '          Cari Berdasarkan Kategori\n\nKategori artwork:\n• Lukisan - Karya seni rupa 2D\n• Patung - Karya seni rupa 3D\n• Keramik - Seni kerajinan tanah liat\n• Batik - Seni tekstil tradisional\n\nKetik kategori atau \'menu\' untuk kembali.', NULL, '2025-07-06 03:19:01'),
(232, 41, 'menu', NULL, NULL, '2025-07-06 03:19:35'),
(233, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:19:36'),
(234, 41, '1', NULL, NULL, '2025-07-06 03:19:38'),
(235, 41, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 03:19:39'),
(236, 41, '1', NULL, NULL, '2025-07-06 03:19:41'),
(237, 41, NULL, '         Cari Berdasarkan Seniman\n\nSeniman terkenal di koleksi kami:\n• Affandi - Pelukis ekspresionisme\n• Raden Saleh - Pelukis romantisme\n• Basuki Abdullah - Pelukis realis\n• Sudjojono - Pelukis naturalis\n\nKetik nama seniman atau \'menu\' untuk kembali.', NULL, '2025-07-06 03:19:41'),
(238, 41, 'menu', NULL, NULL, '2025-07-06 03:19:55'),
(239, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:19:56'),
(240, 41, '1', NULL, NULL, '2025-07-06 03:19:57'),
(241, 41, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 03:19:58'),
(242, 41, '2', NULL, NULL, '2025-07-06 03:20:00'),
(243, 41, NULL, '          Cari Berdasarkan Kategori\n\nKategori artwork:\n• Lukisan - Karya seni rupa 2D\n• Patung - Karya seni rupa 3D\n• Keramik - Seni kerajinan tanah liat\n• Batik - Seni tekstil tradisional\n\nKetik kategori atau \'menu\' untuk kembali.', NULL, '2025-07-06 03:20:01'),
(244, 41, 'menu', NULL, NULL, '2025-07-06 03:21:53'),
(245, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:21:54'),
(246, 41, '1', NULL, NULL, '2025-07-06 03:21:55'),
(247, 41, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 03:21:55'),
(248, 41, '3', NULL, NULL, '2025-07-06 03:21:58'),
(249, 41, NULL, '    Artwork Terpopuler\n\nTop 3 artwork favorit pengunjung:\n1. \'Pemandangan Borobudur\' - Affandi\n2. \'Penangkapan Diponegoro\' - Raden Saleh\n3. \'Gadis Bali\' - Basuki Abdullah\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-06 03:21:59'),
(250, 41, 'menu', NULL, NULL, '2025-07-06 03:29:35'),
(251, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:29:36'),
(252, 41, '2', NULL, NULL, '2025-07-06 03:29:42'),
(253, 41, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 03:29:42'),
(254, 41, '1', NULL, NULL, '2025-07-06 03:29:47'),
(255, 41, NULL, '           Registrasi Akun\n\nLangkah registrasi:\n1. Kunjungi halaman registrasi\n2. Isi data pribadi lengkap\n5. Registrasi selesai\n\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:29:48'),
(256, 41, 'menu', NULL, NULL, '2025-07-06 03:29:50'),
(257, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:29:51'),
(258, 41, '2', NULL, NULL, '2025-07-06 03:29:59'),
(259, 41, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 03:30:00'),
(260, 41, 'menu', NULL, NULL, '2025-07-06 03:30:11'),
(261, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:30:12'),
(262, 41, '1', NULL, NULL, '2025-07-06 03:30:15'),
(263, 41, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 03:30:16'),
(264, 41, 'menu', NULL, NULL, '2025-07-06 03:30:23'),
(265, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:30:23'),
(266, 41, '2', NULL, NULL, '2025-07-06 03:30:26'),
(267, 41, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 03:30:27'),
(268, 41, '1', NULL, NULL, '2025-07-06 03:33:51'),
(269, 41, NULL, '           Registrasi Akun\n\nLangkah registrasi:\n1. Kunjungi halaman registrasi\n2. Isi data pribadi lengkap\n5. Registrasi selesai\n\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:33:51'),
(270, 41, 'menu', NULL, NULL, '2025-07-06 03:36:49'),
(271, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:36:50'),
(272, 41, '2', NULL, NULL, '2025-07-06 03:36:51'),
(273, 41, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 03:36:52'),
(274, 41, '1', NULL, NULL, '2025-07-06 03:37:05'),
(275, 41, NULL, '           Registrasi Akun\n\nLangkah registrasi:\n1. Kunjungi halaman registrasi\n2. Isi data pribadi lengkap\n5. Registrasi selesai\n\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:37:06'),
(276, 41, 'menu', NULL, NULL, '2025-07-06 03:37:12'),
(277, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:37:12'),
(278, 41, '1', NULL, NULL, '2025-07-06 03:37:15'),
(279, 41, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 03:37:15'),
(280, 41, 'menu', NULL, NULL, '2025-07-06 03:37:17'),
(281, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:37:18'),
(282, 41, '2', NULL, NULL, '2025-07-06 03:37:20'),
(283, 41, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 03:37:20'),
(284, 41, '2', NULL, NULL, '2025-07-06 03:37:25'),
(285, 41, NULL, '     Verifikasi Identitas\n\nDokumen yang diperlukan:\n• KTP/Paspor yang masih berlaku\n• NPWP (untuk lelang >50 juta)\n• Surat keterangan domisili\n• Foto selfie dengan KTP\n\nSerahkan dokumen melalui whatsapp admin\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:37:25'),
(286, 41, 'menu', NULL, NULL, '2025-07-06 03:39:49'),
(287, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:39:49'),
(288, 41, '2', NULL, NULL, '2025-07-06 03:39:51'),
(289, 41, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 03:39:51'),
(290, 41, '2', NULL, NULL, '2025-07-06 03:40:04'),
(291, 41, NULL, '     Verifikasi Identitas\n\nDokumen yang diperlukan:\n• KTP/Paspor yang masih berlaku\n• NPWP (untuk lelang >50 juta)\n• Surat keterangan domisili\n• Foto selfie dengan KTP\n\nSerahkan dokumen melalui whatsapp admin\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:40:05'),
(292, 41, 'menu', NULL, NULL, '2025-07-06 03:40:20'),
(293, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:40:21'),
(294, 41, '2', NULL, NULL, '2025-07-06 03:40:22'),
(295, 41, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 03:40:22'),
(296, 41, '3', NULL, NULL, '2025-07-06 03:40:24'),
(297, 41, NULL, '      Deposit Jaminan\n\nKetentuan deposit:\n• Minimal 10% dari nilai lelang\n• Transfer ke rekening resmi\n• Deposit dikembalikan jika tidak menang\n• Berlaku untuk 1 sesi lelang\n\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:40:24'),
(298, 41, 'menu', NULL, NULL, '2025-07-06 03:42:04'),
(299, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:42:04'),
(300, 41, '2', NULL, NULL, '2025-07-06 03:42:05'),
(301, 41, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 03:42:06'),
(302, 41, '4', NULL, NULL, '2025-07-06 03:42:08'),
(303, 41, NULL, '     Mulai Bidding\n\nCara bidding:\n1. Masuk ke halaman lelang terkini\n2. Pantau barang yang sedang dilelang\n3. Konfirmasi bid amount\n4. Klik tombol \'Bid Now\'\n5. Tunggu hasil lelang sampai kamu menang!\n\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:42:09'),
(304, 41, 'menu', NULL, NULL, '2025-07-06 03:43:36'),
(305, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:43:37'),
(306, 41, '1', NULL, NULL, '2025-07-06 03:43:47'),
(307, 41, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 03:43:47'),
(308, 41, 'menu', NULL, NULL, '2025-07-06 03:43:54'),
(309, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:43:54'),
(310, 41, '2', NULL, NULL, '2025-07-06 03:43:57'),
(311, 41, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 03:43:57'),
(312, 41, 'menu', NULL, NULL, '2025-07-06 03:44:13'),
(313, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:44:14'),
(314, 41, '3', NULL, NULL, '2025-07-06 03:44:21'),
(315, 41, NULL, '        Info Galeri\n\nJam Operasional: 09:00 - 17:00\nLokasi: Jl. Seni Raya No. 123\nTelp: (021) 1234-5678\nEmail: info@senimatic.com\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-06 03:44:22'),
(316, 41, 'menu', NULL, NULL, '2025-07-06 03:44:26'),
(317, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:44:27'),
(318, 41, '4', NULL, NULL, '2025-07-06 03:44:29'),
(319, 41, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-07-06 03:44:29'),
(320, 41, '1', NULL, NULL, '2025-07-06 03:44:31'),
(321, 41, NULL, '          Masalah Login\n\nSolusi umum masalah login:\n• Pastikan username/email benar\n• Cek caps lock pada password\n• Clear browser cache & cookies\n• Coba browser lain\n• Reset password jika perlu\n\nMasih bermasalah? Hubungi admin.\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:44:31'),
(322, 41, 'menu', NULL, NULL, '2025-07-06 03:44:34'),
(323, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:44:35'),
(324, 41, '4', NULL, NULL, '2025-07-06 03:44:37'),
(325, 41, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-07-06 03:44:38'),
(326, 41, 'menu', NULL, NULL, '2025-07-06 03:48:28'),
(327, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:48:28'),
(328, 41, '4', NULL, NULL, '2025-07-06 03:48:30'),
(329, 41, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-07-06 03:48:31'),
(330, 41, '1', NULL, NULL, '2025-07-06 03:48:40'),
(331, 41, NULL, '          Masalah Login\n\nSolusi umum masalah login:\n• Pastikan username/email benar\n• Cek caps lock pada password\n• Clear browser cache & cookies\n• Coba browser lain\n• Reset password jika perlu\n\nMasih bermasalah? Hubungi admin.\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:48:40'),
(332, 41, 'menu', NULL, NULL, '2025-07-06 03:49:19'),
(333, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:49:20'),
(334, 41, '1', NULL, NULL, '2025-07-06 03:49:22'),
(335, 41, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 03:49:22'),
(336, 41, '1', NULL, NULL, '2025-07-06 03:49:24'),
(337, 41, NULL, '         Cari Berdasarkan Seniman\n\nSeniman terkenal di koleksi kami:\n• Affandi - Pelukis ekspresionisme\n• Raden Saleh - Pelukis romantisme\n• Basuki Abdullah - Pelukis realis\n• Sudjojono - Pelukis naturalis\n\nKetik nama seniman atau \'menu\' untuk kembali.', NULL, '2025-07-06 03:49:24'),
(338, 41, 'menu', NULL, NULL, '2025-07-06 03:49:38'),
(339, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:49:38'),
(340, 41, '4', NULL, NULL, '2025-07-06 03:49:40'),
(341, 41, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-07-06 03:49:41'),
(342, 41, '1', NULL, NULL, '2025-07-06 03:49:45'),
(343, 41, NULL, '          Masalah Login\n\nSolusi umum masalah login:\n• Pastikan username/email benar\n• Cek caps lock pada password\n• Clear browser cache & cookies\n• Coba browser lain\n• Reset password jika perlu\n\nMasih bermasalah? Hubungi admin.\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:49:46'),
(344, 41, 'menu', NULL, NULL, '2025-07-06 03:52:14'),
(345, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:52:15'),
(346, 41, '4', NULL, NULL, '2025-07-06 03:52:17'),
(347, 41, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-07-06 03:52:17'),
(348, 41, '1', NULL, NULL, '2025-07-06 03:52:20'),
(349, 41, NULL, '          Masalah Login\n\nSolusi umum masalah login:\n• Pastikan username/email benar\n• Cek caps lock pada password\n• Clear browser cache & cookies\n• Coba browser lain\n• Reset password jika perlu\n\nMasih bermasalah? Hubungi admin.\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:52:20'),
(350, 41, 'menu', NULL, NULL, '2025-07-06 03:52:25'),
(351, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:52:26'),
(352, 41, '4', NULL, NULL, '2025-07-06 03:52:28'),
(353, 41, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-07-06 03:52:29'),
(354, 41, '2', NULL, NULL, '2025-07-06 03:52:30'),
(355, 41, NULL, '      Reset Password\n\nCara reset password:\n1. Klik \'Lupa Password\' di halaman login\n2. Masukkan email terdaftar\n3. Cek email untuk link reset\n4. Klik link dalam 15 menit\n5. Buat password baru\n\nPassword harus min. 8 karakter.\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:52:31'),
(356, 41, 'menu', NULL, NULL, '2025-07-06 03:53:26');
INSERT INTO `chat_logs` (`log_id`, `session_id`, `user_input`, `bot_response`, `node_id`, `timestamp`) VALUES
(357, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:53:27'),
(358, 41, '4', NULL, NULL, '2025-07-06 03:53:29'),
(359, 41, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-07-06 03:53:30'),
(360, 41, '2', NULL, NULL, '2025-07-06 03:53:33'),
(361, 41, NULL, '      Reset Password\n\nCara reset password:\n1. Klik \'Lupa Password\' di halaman login\n2. Masukkan email terdaftar\n3. Cek email untuk link reset\n4. Klik link dalam 15 menit\n5. Buat password baru\n\nPassword harus min. 8 karakter.\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:53:33'),
(362, 41, 'menu', NULL, NULL, '2025-07-06 03:53:35'),
(363, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:53:36'),
(364, 41, '4', NULL, NULL, '2025-07-06 03:53:38'),
(365, 41, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-07-06 03:53:39'),
(366, 41, '3', NULL, NULL, '2025-07-06 03:53:40'),
(367, 41, NULL, '            Hubungi Admin\n\nKontak admin SeniMatic:\n       Email: admin@senimatic.com\n         WhatsApp: +62 812-3456-7890\n       Telepon: (021) 1234-5678\n         Jam kerja: 09:00 - 17:00 WIB\n\nRespon dalam 1x24 jam.\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:53:41'),
(368, 41, 'menu', NULL, NULL, '2025-07-06 03:55:15'),
(369, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:55:15'),
(370, 41, '3', NULL, NULL, '2025-07-06 03:55:16'),
(371, 41, NULL, '        Info Galeri\n\nJam Operasional: 09:00 - 17:00\nLokasi: Jl. Seni Raya No. 123\nTelp: (021) 1234-5678\nEmail: info@senimatic.com\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-06 03:55:17'),
(372, 41, 'menu', NULL, NULL, '2025-07-06 03:56:14'),
(373, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:56:15'),
(374, 41, '2', NULL, NULL, '2025-07-06 03:56:27'),
(375, 41, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 03:56:27'),
(376, 41, 'menu', NULL, NULL, '2025-07-06 03:56:40'),
(377, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:56:40'),
(378, 41, '3', NULL, NULL, '2025-07-06 03:56:42'),
(379, 41, NULL, '        Info Galeri\n\nJam Operasional: 09:00 - 17:00\nLokasi: Jl. Seni Raya No. 123\nTelp: (021) 1234-5678\nEmail: info@senimatic.com\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-06 03:56:43'),
(380, 41, '1', NULL, NULL, '2025-07-06 03:56:49'),
(381, 41, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-06 03:56:49'),
(382, 41, '3', NULL, NULL, '2025-07-06 03:57:00'),
(383, 41, NULL, '    Artwork Terpopuler\n\nTop 3 artwork favorit pengunjung:\n1. \'Pemandangan Borobudur\' - Affandi\n2. \'Penangkapan Diponegoro\' - Raden Saleh\n3. \'Gadis Bali\' - Basuki Abdullah\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-06 03:57:00'),
(384, 41, 'menu', NULL, NULL, '2025-07-06 03:57:02'),
(385, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:57:03'),
(386, 41, '3', NULL, NULL, '2025-07-06 03:57:08'),
(387, 41, NULL, '        Info Galeri\n\nJam Operasional: 09:00 - 17:00\nLokasi: Jl. Seni Raya No. 123\nTelp: (021) 1234-5678\nEmail: info@senimatic.com\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-06 03:57:09'),
(388, 41, '2', NULL, NULL, '2025-07-06 03:57:15'),
(389, 41, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-06 03:57:15'),
(390, 41, '3', NULL, NULL, '2025-07-06 03:57:24'),
(391, 41, NULL, '      Deposit Jaminan\n\nKetentuan deposit:\n• Minimal 10% dari nilai lelang\n• Transfer ke rekening resmi\n• Deposit dikembalikan jika tidak menang\n• Berlaku untuk 1 sesi lelang\n\nKetik \'menu\' untuk kembali.', NULL, '2025-07-06 03:57:25'),
(392, 41, 'menu', NULL, NULL, '2025-07-06 03:57:29'),
(393, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:57:30'),
(394, 41, '3', NULL, NULL, '2025-07-06 03:57:31'),
(395, 41, NULL, '        Info Galeri\n\nJam Operasional: 09:00 - 17:00\nLokasi: Jl. Seni Raya No. 123\nTelp: (021) 1234-5678\nEmail: info@senimatic.com\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-06 03:57:32'),
(396, 41, '4', NULL, NULL, '2025-07-06 03:59:11'),
(397, 41, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-07-06 03:59:11'),
(398, 41, 'menu', NULL, NULL, '2025-07-06 03:59:17'),
(399, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:59:17'),
(400, 41, '9', NULL, NULL, '2025-07-06 03:59:21'),
(401, 41, NULL, 'Pilihan tidak valid. Silakan ketik nomor 1-4 untuk memilih menu.', NULL, '2025-07-06 03:59:21'),
(402, 41, 'menu', NULL, NULL, '2025-07-06 03:59:49'),
(403, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 03:59:50'),
(404, 41, '3', NULL, NULL, '2025-07-06 03:59:54'),
(405, 41, NULL, '        Info Galeri\n\nJam Operasional: 09:00 - 17:00\nLokasi: Jl. Seni Raya No. 123\nTelp: (021) 1234-5678\nEmail: info@senimatic.com\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-06 03:59:54'),
(406, 41, 'menu', NULL, NULL, '2025-07-06 04:03:55'),
(407, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 04:03:55'),
(408, 41, '3', NULL, NULL, '2025-07-06 04:03:57'),
(409, 41, NULL, '        Info Galeri\n\nJam Operasional: 09:00 - 17:00\nLokasi: Jl. Seni Raya No. 123\nTelp: (021) 1234-5678\nEmail: info@senimatic.com\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-06 04:03:58'),
(410, 41, 'menu', NULL, NULL, '2025-07-06 04:04:17'),
(411, 41, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 04:04:18'),
(412, 41, '5', NULL, NULL, '2025-07-06 04:04:19'),
(413, 41, NULL, 'Pilihan tidak valid. Silakan ketik nomor 1-4 untuk memilih menu.', NULL, '2025-07-06 04:04:20'),
(414, 41, 'j', NULL, NULL, '2025-07-06 04:04:24'),
(415, 41, NULL, 'Mohon masukkan nomor pilihan yang valid (1-4) atau ketik \'halo\' untuk memulai.', NULL, '2025-07-06 04:04:24'),
(416, 42, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-06 04:55:15'),
(417, 43, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-08 12:20:55'),
(418, 44, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-08 12:26:01'),
(419, 44, '1', NULL, NULL, '2025-07-08 12:26:17'),
(420, 44, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-08 12:26:17'),
(421, 44, '1', NULL, NULL, '2025-07-08 12:26:22'),
(422, 44, NULL, '         Cari Berdasarkan Seniman\n\nSeniman terkenal di koleksi kami:\n• Affandi - Pelukis ekspresionisme\n• Raden Saleh - Pelukis romantisme\n• Basuki Abdullah - Pelukis realis\n• Sudjojono - Pelukis naturalis\n\nKetik nama seniman atau \'menu\' untuk kembali.', NULL, '2025-07-08 12:26:23'),
(423, 44, 'menu', NULL, NULL, '2025-07-08 12:26:34'),
(424, 44, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-08 12:26:35'),
(425, 44, '2', NULL, NULL, '2025-07-08 12:26:41'),
(426, 44, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-08 12:26:41'),
(427, 44, '1', NULL, NULL, '2025-07-08 12:26:55'),
(428, 44, NULL, '           Registrasi Akun\n\nLangkah registrasi:\n1. Kunjungi halaman registrasi\n2. Isi data pribadi lengkap\n5. Registrasi selesai\n\nKetik \'menu\' untuk kembali.', NULL, '2025-07-08 12:26:55'),
(429, 44, 'menu', NULL, NULL, '2025-07-08 12:27:10'),
(430, 44, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-08 12:27:11'),
(431, 44, '9', NULL, NULL, '2025-07-08 12:27:13'),
(432, 44, NULL, 'Pilihan tidak valid', NULL, '2025-07-08 12:27:14'),
(433, 44, '9', NULL, NULL, '2025-07-08 12:27:17'),
(434, 44, NULL, 'Pilihan tidak valid', NULL, '2025-07-08 12:27:18'),
(435, 44, '9', NULL, NULL, '2025-07-08 12:28:40'),
(436, 44, NULL, 'Pilihan tidak valid', NULL, '2025-07-08 12:28:41'),
(437, 44, '9', NULL, NULL, '2025-07-08 12:29:47'),
(438, 44, NULL, 'Pilihan tidak valid', NULL, '2025-07-08 12:29:48'),
(439, 45, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-08 12:30:31'),
(440, 45, '9', NULL, NULL, '2025-07-08 12:30:35'),
(441, 45, NULL, 'Pilihan tidak valid', NULL, '2025-07-08 12:30:36'),
(442, 45, '3', NULL, NULL, '2025-07-08 12:30:42'),
(443, 45, NULL, '        Info Galeri\n\nJam Operasional: 09:00 - 17:00\nLokasi: Jl. Seni Raya No. 123\nTelp: (021) 1234-5678\nEmail: info@senimatic.com\n\nKetik \'menu\' untuk kembali ke menu utama.', NULL, '2025-07-08 12:30:43'),
(444, 45, 'menu', NULL, NULL, '2025-07-08 12:30:51'),
(445, 45, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-08 12:30:51'),
(446, 45, '4', NULL, NULL, '2025-07-08 12:30:58'),
(447, 45, NULL, '    Bantuan Teknis\n\n1. Masalah login\n2. Reset password\n3. Hubungi admin\n\nKetik nomor untuk bantuan:', NULL, '2025-07-08 12:30:58'),
(448, 45, 'menu', NULL, NULL, '2025-07-08 12:31:07'),
(449, 45, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-08 12:31:07'),
(450, 46, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-09 10:42:05'),
(451, 47, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-10 06:40:04'),
(452, 47, '1', NULL, NULL, '2025-07-10 06:40:36'),
(453, 47, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-10 06:40:36'),
(454, 47, '2', NULL, NULL, '2025-07-10 06:40:45'),
(455, 47, NULL, '          Cari Berdasarkan Kategori\n\nKategori artwork:\n• Lukisan - Karya seni rupa 2D\n• Patung - Karya seni rupa 3D\n• Keramik - Seni kerajinan tanah liat\n• Batik - Seni tekstil tradisional\n\nKetik kategori atau \'menu\' untuk kembali.', NULL, '2025-07-10 06:40:46'),
(456, 47, 'menu', NULL, NULL, '2025-07-10 06:40:54'),
(457, 47, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-10 06:40:55'),
(458, 47, '2', NULL, NULL, '2025-07-10 06:41:03'),
(459, 47, NULL, '       Cara Mengikuti Lelang\n\nLangkah-langkah:\n1. Registrasi akun\n2. Verifikasi identitas\n3. Deposit jaminan\n4. Mulai bidding\n\nPilih nomor untuk detail:', NULL, '2025-07-10 06:41:03'),
(460, 47, 'menu', NULL, NULL, '2025-07-10 06:41:20'),
(461, 47, NULL, 'Selamat datang di SeniMatic Chat Assistant!         \n\nSaya siap membantu Anda dengan:\n1. Informasi Artwork\n2. Cara Mengikuti Lelang\n3. Info Galeri\n4. Bantuan Teknis\n\nKetik nomor pilihan Anda untuk memulai:', NULL, '2025-07-10 06:41:20'),
(462, 47, '1', NULL, NULL, '2025-07-10 06:41:43'),
(463, 47, NULL, '        Informasi Artwork\n\nPilih kategori:\n1. Cari berdasarkan seniman\n2. Cari berdasarkan kategori\n3. Artwork terpopuler\n\nKetik nomor pilihan:', NULL, '2025-07-10 06:41:43');

-- --------------------------------------------------------

--
-- Table structure for table `conversation_flows`
--

CREATE TABLE `conversation_flows` (
  `flow_id` int NOT NULL,
  `flow_name` varchar(100) NOT NULL,
  `description` text,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `conversation_flows`
--

INSERT INTO `conversation_flows` (`flow_id`, `flow_name`, `description`, `is_active`, `created_at`) VALUES
(1, 'main', 'Main conversation flow for SeniMatic chatbot', 1, '2025-06-24 00:48:21');

-- --------------------------------------------------------

--
-- Table structure for table `employees`
--

CREATE TABLE `employees` (
  `employee_id` int NOT NULL,
  `user_id` int DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `position` varchar(50) DEFAULT NULL,
  `qr_code` varchar(255) DEFAULT NULL,
  `hire_date` date DEFAULT NULL,
  `salary` int NOT NULL,
  `photo_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `employees`
--

INSERT INTO `employees` (`employee_id`, `user_id`, `name`, `position`, `qr_code`, `hire_date`, `salary`, `photo_path`) VALUES
(1, 1, 'Admin Boss', 'Museum Director', 'EMP001', '2025-07-01', 10000000, NULL),
(2, 2, 'Staff Satu', 'Curator', 'emp_2_Staff_Satu_20250708', '2025-07-01', 8000000, NULL),
(3, 6, 'Staff Duaaa', 'Guide', 'emp_3_Staff_Duaaa_20250708', '2025-07-01', 7000000, NULL),
(4, 7, 'Staff Tiga', 'Technician', 'emp_4_Staff_Tiga_20250708', '2025-07-01', 7500000, NULL),
(5, 8, 'Staff Empat Baru', 'Security', 'emp_5_Staff_Empat_Baru_20250704', '2025-07-01', 6500000, NULL),
(11, 9, 'Staff Baru banget z', 'Guide', '', '2025-07-01', 7500000, '/images-employee/Babon.jpg'),
(15, 16, 'mbah kakungz', 'Guide', 'emp_15_mbah_kakungz_20250710', '2025-07-01', 7000000, '/images-employee/Raju.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `flow_nodes`
--

CREATE TABLE `flow_nodes` (
  `node_id` int NOT NULL,
  `flow_id` int DEFAULT NULL,
  `parent_node_id` int DEFAULT NULL,
  `node_type` enum('menu','question','response','input') NOT NULL,
  `node_text` text NOT NULL,
  `is_root` tinyint(1) DEFAULT '0',
  `order_position` int DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `flow_nodes`
--

INSERT INTO `flow_nodes` (`node_id`, `flow_id`, `parent_node_id`, `node_type`, `node_text`, `is_root`, `order_position`, `created_at`) VALUES
(1, 1, NULL, 'menu', '🎨 Selamat datang di SeniMatic! 🎨\n\nSaya adalah asisten virtual Anda. Pilih menu di bawah ini:\n\n1️⃣ Informasi Artwork\n2️⃣ Cara Mengikuti Lelang\n3️⃣ Info Galeri\n4️⃣ Bantuan Teknis\n\nKetik nomor pilihan Anda (1-4):', 1, 1, '2025-06-24 00:48:21'),
(2, 1, 1, 'menu', '🎨 Informasi Artwork\n\nPilih jenis informasi yang Anda butuhkan:\n\n1️⃣ Cari berdasarkan seniman\n2️⃣ Cari berdasarkan kategori\n3️⃣ Artwork terpopuler\n\nKetik nomor pilihan Anda atau \"menu\" untuk kembali:', 0, 2, '2025-06-24 00:48:21'),
(3, 1, 1, 'menu', '🔨 Cara Mengikuti Lelang\n\nPanduan lengkap mengikuti lelang:\n\n1️⃣ Registrasi akun\n2️⃣ Verifikasi identitas\n3️⃣ Deposit jaminan\n4️⃣ Mulai bidding\n\nKetik nomor pilihan Anda atau \"menu\" untuk kembali:', 0, 3, '2025-06-24 00:48:21'),
(4, 1, 1, 'response', '🏢 Info Galeri SeniMatic\n\n📍 Alamat: Jl. Seni Raya No. 123, Jakarta\n🕐 Jam Buka: 09:00 - 17:00 WIB\n📞 Telepon: (021) 1234-5678\n📧 Email: info@senimatic.com\n🌐 Website: www.senimatic.com\n\n🎫 Tiket Masuk:\n• Dewasa: Rp 25.000\n• Anak-anak: Rp 15.000\n• Pelajar/Mahasiswa: Rp 20.000\n\nKetik \"menu\" untuk kembali.', 0, 4, '2025-06-24 00:48:21'),
(5, 1, 1, 'menu', '🛠 Bantuan Teknis\n\nAda masalah? Kami siap membantu:\n\n1️⃣ Masalah login\n2️⃣ Reset password\n3️⃣ Hubungi admin\n\nKetik nomor pilihan Anda atau \"menu\" untuk kembali:', 0, 5, '2025-06-24 00:48:21'),
(6, 1, 2, 'response', '👨‍🎨 Cari Berdasarkan Seniman\n\nSeniman terkenal di koleksi kami:\n• Raden Saleh - Pelukis romantis Indonesia\n• Affandi - Maestro ekspresionisme\n• Basuki Abdullah - Pelukis realis\n• Sudjojono - Bapak seni lukis modern\n• Hendra Gunawan - Pelukis revolusioner\n\nUntuk info detail, sebutkan nama seniman atau ketik \"menu\" untuk kembali.', 0, 6, '2025-06-24 00:48:21'),
(7, 1, 2, 'response', '🎭 Cari Berdasarkan Kategori\n\nKategori artwork tersedia:\n🖼 Lukisan Klasik\n🎨 Seni Modern\n🗿 Patung & Sculpture\n📸 Fotografi Seni\n✍ Kaligrafi\n🏺 Keramik & Pottery\n\nSebutkan kategori yang diminati atau ketik \"menu\" untuk kembali.', 0, 7, '2025-06-24 00:48:21'),
(8, 1, 2, 'response', '⭐ Artwork Terpopuler\n\nKoleksi paling diminati:\n\n1. \"Penangkapan Diponegoro\" - Raden Saleh\n2. \"Potret Diri\" - Affandi\n3. \"Gadis Bali\" - Basuki Abdullah\n4. \"Pasar Malam\" - Hendra Gunawan\n5. \"Pemandangan Borobudur\" - Sudjojono\n\nUntuk detail lengkap, sebutkan judul karya atau ketik \"menu\" untuk kembali.', 0, 8, '2025-06-24 00:48:21'),
(9, 1, 3, 'response', '📝 Registrasi Akun Lelang\n\nLangkah registrasi:\n1. Kunjungi halaman \"Daftar\"\n2. Isi data pribadi lengkap\n3. Upload foto KTP/Passport\n4. Verifikasi email\n5. Tunggu konfirmasi admin (1x24 jam)\n\n✅ Setelah disetujui, Anda bisa ikut lelang!\n\nKetik \"menu\" untuk kembali.', 0, 9, '2025-06-24 00:48:21'),
(10, 1, 3, 'response', '🔍 Verifikasi Identitas\n\nDokumen yang diperlukan:\n📄 KTP/Passport (scan jelas)\n🏠 Bukti alamat (tagihan listrik/air)\n💳 Rekening bank aktif\n📞 Nomor telepon aktif\n\n⏱ Proses verifikasi: 1-3 hari kerja\n📧 Notifikasi dikirim via email\n\nKetik \"menu\" untuk kembali.', 0, 10, '2025-06-24 00:48:21'),
(11, 1, 3, 'response', '💰 Deposit Jaminan\n\nKetentuan deposit:\n• Minimal: Rp 1.000.000\n• Transfer ke rekening resmi SeniMatic\n• Deposit dikembalikan jika tidak menang\n• Potong otomatis jika menang lelang\n\n🏦 Rekening Deposit:\nBank BCA: 1234567890\nA.n. PT SeniMatic Indonesia\n\nKetik \"menu\" untuk kembali.', 0, 11, '2025-06-24 00:48:21'),
(12, 1, 3, 'response', '🎯 Mulai Bidding\n\nCara ikut lelang:\n1. Pilih artwork yang diminati\n2. Lihat starting price & increment\n3. Klik \"Place Bid\"\n4. Masukkan nominal bid\n5. Konfirmasi bid Anda\n\n⏰ Lelang berakhir otomatis sesuai jadwal\n🏆 Pemenang akan dihubungi admin\n\nSelamat berburu karya seni! 🎨\n\nKetik \"menu\" untuk kembali.', 0, 12, '2025-06-24 00:48:21'),
(13, 1, 5, 'response', '🔐 Masalah Login\n\nSolusi umum masalah login:\n• Pastikan username/email benar\n• Cek caps lock pada password\n• Clear browser cache & cookies\n• Coba browser lain\n• Reset password jika perlu\n\nMasih bermasalah? Hubungi admin.\nKetik \"menu\" untuk kembali.', 0, 13, '2025-06-24 00:48:21'),
(14, 1, 5, 'response', '🔑 Reset Password\n\nCara reset password:\n1. Klik \"Lupa Password\" di halaman login\n2. Masukkan email terdaftar\n3. Cek email untuk link reset\n4. Klik link dalam 15 menit\n5. Buat password baru\n\nPassword harus min. 8 karakter.\nKetik \"menu\" untuk kembali.', 0, 14, '2025-06-24 00:48:21'),
(15, 1, 5, 'response', '👨‍💼 Hubungi Admin\n\nKontak admin SeniMatic:\n📧 Email: admin@senimatic.com\n📱 WhatsApp: +62 812-3456-7890\n☎ Telepon: (021) 1234-5678\n🕐 Jam kerja: 09:00 - 17:00 WIB\n\nRespon dalam 1x24 jam.\nKetik \"menu\" untuk kembali.', 0, 15, '2025-06-24 00:48:21');

-- --------------------------------------------------------

--
-- Table structure for table `node_options`
--

CREATE TABLE `node_options` (
  `option_id` int NOT NULL,
  `node_id` int DEFAULT NULL,
  `option_number` int DEFAULT NULL,
  `option_text` varchar(255) DEFAULT NULL,
  `target_node_id` int DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `node_options`
--

INSERT INTO `node_options` (`option_id`, `node_id`, `option_number`, `option_text`, `target_node_id`, `is_active`) VALUES
(1, 1, 1, 'Informasi Artwork', 2, 1),
(2, 1, 2, 'Cara Mengikuti Lelang', 3, 1),
(3, 1, 3, 'Info Galeri', 4, 1),
(4, 1, 4, 'Bantuan Teknis', 5, 1),
(5, 2, 1, 'Cari berdasarkan seniman', 6, 1),
(6, 2, 2, 'Cari berdasarkan kategori', 7, 1),
(7, 2, 3, 'Artwork terpopuler', 8, 1),
(8, 3, 1, 'Registrasi akun', 9, 1),
(9, 3, 2, 'Verifikasi identitas', 10, 1),
(10, 3, 3, 'Deposit jaminan', 11, 1),
(11, 3, 4, 'Mulai bidding', 12, 1),
(12, 5, 1, 'Masalah login', 13, 1),
(13, 5, 2, 'Reset password', 14, 1),
(14, 5, 3, 'Hubungi admin', 15, 1);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('boss','staff','visitor') NOT NULL DEFAULT 'visitor',
  `email` varchar(100) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role`, `email`, `phone`, `created_at`) VALUES
(1, 'admin', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'boss', 'admin@senimatic.com', '081234567890', '2025-06-20 10:48:05'),
(2, 'staff1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'staff', 'staff1@senimatic.com', '081234567891', '2025-06-20 10:48:05'),
(3, 'visitor1', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'visitor', 'visitor1@senimatic.com', '081234567892', '2025-06-20 10:48:05'),
(6, 'staff2', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'staff', 'staff2@senimatic.com', '081234567892', '2025-06-20 10:48:05'),
(7, 'staff3', 'xZz9WQLkHFyCTcPchqCz/x397J2iF2dIfviMfSH4wBY=', 'staff', 'staff3@senimatic.com', '081234567893', '2025-06-20 10:48:05'),
(8, 'staff4', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'staff', 'staff4@senimatic.com', '081234567894', '2025-06-20 10:48:05'),
(9, 'staff_baru_banget', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'staff', 'staff_baru_banget@senimatic.com', '080000000000', '2025-07-01 01:57:22'),
(16, 'mbah_kakungz_p', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'staff', 'mbah_kakungz@senimatic.com', '080000000000', '2025-07-01 02:25:55'),
(18, 'coba_lagi_lagi', 'C0X5/EP+WSwakMdZ4p0v0TuokFIfN7JKNlUF81DiHqc=', 'staff', 'coba@gmail.co.id', '081247281726', '2025-07-01 03:36:30'),
(20, 'visitor2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'visitor', 'visitor2@gmail.com', '081264517281', '2025-07-04 02:50:16'),
(21, 'visitor3', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'visitor', 'visitor3@gmail.com', '0812370313', '2025-07-04 02:53:16'),
(22, 'staff5', 'avmUZ4dtUdmowGqiRvrZbYE2i129QXOk8b6/IJypTRg=', 'staff', 'staff5@gmail.com', '082172638471', '2025-07-08 13:31:33'),
(23, 'staff6', 'pQOKOHFaHXgF7DJAuf9xmZpE+6Ds7Wqge8gz1vqnTug=', 'staff', 'staff6@gmail.com', '081276281726', '2025-07-08 13:55:20'),
(24, 'harry', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'visitor', 'harry@gmail.com', '08761528087', '2025-07-10 06:37:32'),
(25, 'pppp', 'UlfW5ESnzBXKApHsSc3VXKPOPPhqmS3WIX25UYSeuTo=', 'visitor', 'pp@gmail.com', '0988766666', '2025-07-10 06:47:34');

-- --------------------------------------------------------

--
-- Table structure for table `user_chat_sessions`
--

CREATE TABLE `user_chat_sessions` (
  `session_id` int NOT NULL,
  `user_id` int DEFAULT NULL,
  `current_node_id` int DEFAULT NULL,
  `session_data` text,
  `last_activity` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_active` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `user_chat_sessions`
--

INSERT INTO `user_chat_sessions` (`session_id`, `user_id`, `current_node_id`, `session_data`, `last_activity`, `is_active`) VALUES
(11, 1, 1, '{}', '2025-06-28 08:49:52', 1),
(12, 1, 1, '{}', '2025-06-28 08:50:00', 1),
(13, 1, 1, '{}', '2025-06-28 09:03:20', 1),
(14, 1, 1, '{}', '2025-06-28 09:05:15', 1),
(15, 1, 1, '{}', '2025-06-28 09:19:44', 1),
(16, 1, 1, '{}', '2025-06-28 09:36:00', 1),
(17, 1, 1, '{}', '2025-06-28 09:36:17', 1),
(18, 1, 1, '{}', '2025-06-28 09:36:35', 1),
(19, 1, 1, '{}', '2025-06-28 09:36:44', 1),
(20, 1, 1, '{}', '2025-06-28 09:37:13', 1),
(21, 1, 1, '{}', '2025-06-28 09:45:48', 1),
(22, 1, 1, '{}', '2025-06-28 09:45:57', 1),
(23, 1, 1, '{}', '2025-06-28 09:47:21', 1),
(24, 1, 1, '{}', '2025-06-28 09:47:31', 1),
(25, 1, 1, '{}', '2025-06-28 11:22:54', 1),
(26, 1, 1, '{}', '2025-06-28 13:12:34', 1),
(27, 1, 1, '{}', '2025-06-28 13:13:17', 1),
(28, 1, 1, '{}', '2025-06-28 13:13:34', 1),
(29, 1, 1, '{}', '2025-06-28 13:14:28', 1),
(30, 1, 1, '{}', '2025-06-29 03:06:38', 1),
(31, 1, 1, '{}', '2025-06-29 03:07:43', 1),
(32, 1, 1, '{}', '2025-06-30 06:09:03', 1),
(33, 1, 1, '{}', '2025-07-04 05:05:08', 1),
(34, 1, 1, '{}', '2025-07-04 05:45:07', 1),
(35, 1, 1, '{}', '2025-07-04 05:45:25', 1),
(36, 1, 1, '{}', '2025-07-06 02:29:45', 1),
(37, 1, 1, '{}', '2025-07-06 02:49:05', 1),
(38, 1, 1, '{}', '2025-07-06 02:55:58', 1),
(39, 1, 1, '{}', '2025-07-06 03:06:23', 1),
(40, 1, 1, '{}', '2025-07-06 03:06:42', 1),
(41, 1, 1, '{}', '2025-07-06 04:04:24', 1),
(42, 1, 1, '{}', '2025-07-06 04:55:15', 1),
(43, 1, 1, '{}', '2025-07-08 12:20:55', 1),
(44, 1, 1, '{}', '2025-07-08 12:29:48', 1),
(45, 1, 1, '{}', '2025-07-08 12:31:07', 1),
(46, 1, 1, '{}', '2025-07-09 10:42:05', 1),
(47, 1, 1, '{}', '2025-07-10 06:41:43', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `artists`
--
ALTER TABLE `artists`
  ADD PRIMARY KEY (`artist_id`);

--
-- Indexes for table `artworks`
--
ALTER TABLE `artworks`
  ADD PRIMARY KEY (`artwork_id`),
  ADD UNIQUE KEY `qr_code` (`qr_code`),
  ADD KEY `artist_id` (`artist_id`);

--
-- Indexes for table `attendance`
--
ALTER TABLE `attendance`
  ADD PRIMARY KEY (`attendance_id`),
  ADD KEY `employee_id` (`employee_id`);

--
-- Indexes for table `auctions`
--
ALTER TABLE `auctions`
  ADD PRIMARY KEY (`auction_id`),
  ADD KEY `artwork_id` (`artwork_id`),
  ADD KEY `winner_id` (`winner_id`);

--
-- Indexes for table `bids`
--
ALTER TABLE `bids`
  ADD PRIMARY KEY (`bid_id`),
  ADD KEY `auction_id` (`auction_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `chat_logs`
--
ALTER TABLE `chat_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `session_id` (`session_id`),
  ADD KEY `node_id` (`node_id`);

--
-- Indexes for table `conversation_flows`
--
ALTER TABLE `conversation_flows`
  ADD PRIMARY KEY (`flow_id`);

--
-- Indexes for table `employees`
--
ALTER TABLE `employees`
  ADD PRIMARY KEY (`employee_id`),
  ADD KEY `employees_ibfk_1` (`user_id`);

--
-- Indexes for table `flow_nodes`
--
ALTER TABLE `flow_nodes`
  ADD PRIMARY KEY (`node_id`),
  ADD KEY `flow_id` (`flow_id`),
  ADD KEY `parent_node_id` (`parent_node_id`);

--
-- Indexes for table `node_options`
--
ALTER TABLE `node_options`
  ADD PRIMARY KEY (`option_id`),
  ADD KEY `node_id` (`node_id`),
  ADD KEY `target_node_id` (`target_node_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `user_chat_sessions`
--
ALTER TABLE `user_chat_sessions`
  ADD PRIMARY KEY (`session_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `current_node_id` (`current_node_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `artists`
--
ALTER TABLE `artists`
  MODIFY `artist_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `artworks`
--
ALTER TABLE `artworks`
  MODIFY `artwork_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `attendance`
--
ALTER TABLE `attendance`
  MODIFY `attendance_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `auctions`
--
ALTER TABLE `auctions`
  MODIFY `auction_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `bids`
--
ALTER TABLE `bids`
  MODIFY `bid_id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `chat_logs`
--
ALTER TABLE `chat_logs`
  MODIFY `log_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=464;

--
-- AUTO_INCREMENT for table `conversation_flows`
--
ALTER TABLE `conversation_flows`
  MODIFY `flow_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `employees`
--
ALTER TABLE `employees`
  MODIFY `employee_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `flow_nodes`
--
ALTER TABLE `flow_nodes`
  MODIFY `node_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `node_options`
--
ALTER TABLE `node_options`
  MODIFY `option_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT for table `user_chat_sessions`
--
ALTER TABLE `user_chat_sessions`
  MODIFY `session_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=48;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `artworks`
--
ALTER TABLE `artworks`
  ADD CONSTRAINT `artworks_ibfk_1` FOREIGN KEY (`artist_id`) REFERENCES `artists` (`artist_id`);

--
-- Constraints for table `attendance`
--
ALTER TABLE `attendance`
  ADD CONSTRAINT `attendance_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`);

--
-- Constraints for table `auctions`
--
ALTER TABLE `auctions`
  ADD CONSTRAINT `auctions_ibfk_1` FOREIGN KEY (`artwork_id`) REFERENCES `artworks` (`artwork_id`),
  ADD CONSTRAINT `auctions_ibfk_2` FOREIGN KEY (`winner_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `bids`
--
ALTER TABLE `bids`
  ADD CONSTRAINT `bids_ibfk_1` FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`auction_id`),
  ADD CONSTRAINT `bids_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `chat_logs`
--
ALTER TABLE `chat_logs`
  ADD CONSTRAINT `chat_logs_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `user_chat_sessions` (`session_id`),
  ADD CONSTRAINT `chat_logs_ibfk_2` FOREIGN KEY (`node_id`) REFERENCES `flow_nodes` (`node_id`);

--
-- Constraints for table `employees`
--
ALTER TABLE `employees`
  ADD CONSTRAINT `employees_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `flow_nodes`
--
ALTER TABLE `flow_nodes`
  ADD CONSTRAINT `flow_nodes_ibfk_1` FOREIGN KEY (`flow_id`) REFERENCES `conversation_flows` (`flow_id`),
  ADD CONSTRAINT `flow_nodes_ibfk_2` FOREIGN KEY (`parent_node_id`) REFERENCES `flow_nodes` (`node_id`);

--
-- Constraints for table `node_options`
--
ALTER TABLE `node_options`
  ADD CONSTRAINT `node_options_ibfk_1` FOREIGN KEY (`node_id`) REFERENCES `flow_nodes` (`node_id`),
  ADD CONSTRAINT `node_options_ibfk_2` FOREIGN KEY (`target_node_id`) REFERENCES `flow_nodes` (`node_id`);

--
-- Constraints for table `user_chat_sessions`
--
ALTER TABLE `user_chat_sessions`
  ADD CONSTRAINT `user_chat_sessions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `user_chat_sessions_ibfk_2` FOREIGN KEY (`current_node_id`) REFERENCES `flow_nodes` (`node_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
