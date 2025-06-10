# WorshipSound - Aplikasi Musik Rohani Android

Sebuah aplikasi Android yang dibangun dalam Java untuk menemukan dan menikmati musik spiritual/rohani dengan bantuan dari API Deezer. Aplikasi ini memenuhi semua persyaratan teknis untuk proyek akhir Mobile Lab.

## 📱 Fitur

### Fungsionalitas Utama
- **Autentikasi Pengguna**: Sistem login dan registrasi dengan database SQLite
- **Pencarian Musik**: Mencari lagu spiritual, worship, gospel, dan Kristen menggunakan API Deezer
- **Preview Audio**: Memutar preview lagu 30 detik dengan kontrol media
- **Manajemen Playlist**: Menyimpan lagu favorit ke playlist lokal
- **Akses Offline**: Melihat lagu yang tersimpan saat offline
- **Dukungan Tema**: Tema Terang/Gelap dengan preferensi yang persisten

### Implementasi Teknis

#### ✅ Activities & Intent
- **SplashActivity**: Peluncur aplikasi dengan tampilan logo dan navigasi otomatis
- **LoginActivity**: Autentikasi dengan form registrasi/login
- **MainActivity**: Container utama dengan manajemen navigasi
- **Intent**: Navigasi mulus antar aktivitas dengan passing data

#### ✅ Fragments & Navigation
- **HomeFragment**: Menampilkan lagu spiritual trending
- **SearchFragment**: Mencari dan menemukan musik baru
- **PlaylistFragment**: Mengelola lagu tersimpan dan playlist
- **Navigation Component**: Navigasi bawah dengan manajemen fragment yang tepat

#### ✅ Implementasi RecyclerView
- **Tampilan Daftar Lagu**: Lagu trending, hasil pencarian, dan playlist
- **Custom Adapter**: SongAdapter dengan click handler dan update dinamis
- **Scrolling Halus**: Performa optimal dengan pola ViewHolder

#### ✅ Background Threading
- **ExecutorService**: Panggilan API dan operasi database di background
- **Handler**: Update UI dan tracking progress
- **Media Playback**: Pemrosesan audio di background

#### ✅ Networking dengan Retrofit
- **Integrasi API Deezer**: Mencari lagu spiritual berdasarkan kata kunci
- **Error Handling**: Skenario kegagalan jaringan dengan opsi retry
- **Dukungan Offline**: Degradasi yang baik saat internet tidak tersedia
- **JSON Parsing**: Konversi otomatis ke objek Java

#### ✅ Persistensi Data Lokal
- **Database SQLite**: Kredensial pengguna, lagu yang disukai, dan preferensi
- **SharedPreferences**: Pengaturan tema dan manajemen sesi
- **DatabaseHelper**: Skema database yang tepat dan dukungan migrasi
- **SongDAO**: Lapisan akses data dengan operasi CRUD

#### ✅ Manajemen Tema
- **Mode Terang/Gelap**: Pergantian tema seluruh sistem
- **Pengaturan Persisten**: Preferensi tema disimpan secara lokal
- **Update Dinamis**: Penerapan tema real-time tanpa restart

## 🏗️ Arsitektur

### Dependencies
- **Retrofit 2.9.0**: HTTP client untuk panggilan API
- **OkHttp 4.12.0**: Lapisan jaringan dengan logging
- **Picasso 2.71828**: Loading dan caching gambar
- **Navigation Component 2.7.6**: Navigasi fragment
- **Material Design**: Komponen UI modern
- **RecyclerView**: Tampilan list yang efisien

## 🚀 Setup & Installation

### Prasyarat
- Android Studio Arctic Fox atau yang lebih baru
- Android SDK 24+ (Android 7.0)
- Java 11
- Koneksi internet untuk panggilan API

### Instalasi
1. Clone repository
```bash
git clone https://github.com/your-username/WorshipSound.git
```

2. Buka di Android Studio
3. Build dan jalankan proyek
4. Buat akun atau login untuk mulai menggunakan aplikasi

### Konfigurasi API
Aplikasi menggunakan API publik Deezer:
- **Base URL**: `https://api.deezer.com/`
- **Tidak perlu API key** untuk fungsionalitas pencarian dasar
- **Rate limiting**: Penggunaan yang sopan sesuai ketentuan Deezer

## 🎵 Penggunaan

### Pencarian dan Penemuan
1. Gunakan tab **Search** untuk menemukan lagu spiritual
2. Cari berdasarkan judul lagu, artis, atau kata kunci seperti "worship", "gospel"
3. Preview lagu dengan tombol play
4. Like lagu untuk menyimpannya ke koleksi Anda

### Manajemen Playlist
1. Kunjungi tab **Playlists** untuk melihat lagu tersimpan
2. Jelajahi playlist berbeda (Liked Songs, Worship Favorites, Gospel Classics)
3. Hapus lagu menggunakan tombol like atau opsi menu
4. Lihat detail lagu dan kelola koleksi Anda

### Kustomisasi Tema
1. Gunakan toggle tema di menu utama
2. Beralih antara mode Terang dan Gelap
3. Preferensi tema otomatis tersimpan

## 🛠️ Detail Teknis

### Skema Database
```sql
-- Tabel users
CREATE TABLE users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    first_name TEXT,
    last_name TEXT,
    created_at INTEGER NOT NULL,
    is_dark_theme INTEGER DEFAULT 0
);

-- Tabel songs
CREATE TABLE songs (
    song_id INTEGER PRIMARY KEY AUTOINCREMENT,
    deezer_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    artist TEXT NOT NULL,
    album TEXT,
    duration INTEGER,
    preview_url TEXT,
    album_cover TEXT,
    is_liked INTEGER DEFAULT 0,
    playlist_name TEXT,
    added_at INTEGER NOT NULL
);
```

### Endpoint API
- **Search**: `GET /search?q={query}&limit={limit}&index={index}`
- **Enhanced Search**: Otomatis menambahkan konteks spiritual ke query
- **Error Handling**: Penanganan error komprehensif untuk semua panggilan API

### Fitur Keamanan
- Validasi password (minimum 6 karakter)
- Validasi format email
- Pencegahan SQL injection dengan parameterized queries
- Manajemen sesi dengan penyimpanan aman

## 🎨 Fitur UI/UX

- **Material Design 3**: Interface modern dan indah
- **Layout Responsif**: Bekerja di berbagai ukuran layar  
- **Animasi Halus**: Transisi halaman dan loading states
- **Aksesibilitas**: Deskripsi konten yang tepat dan penanganan fokus
- **Error States**: Pesan error yang user-friendly dan opsi retry
- **Loading Indicators**: Progress bar dan skeleton screens

## 🧪 Testing

Aplikasi ini mencakup penanganan error komprehensif dan telah diuji untuk:
- Masalah konektivitas jaringan
- Kegagalan dan timeout API
- Operasi database
- Pergantian tema
- Skenario pemutaran audio
- Flow autentikasi pengguna

## 📋 Kepatuhan Persyaratan

Proyek ini memenuhi semua persyaratan Final Mobile Lab:

✅ **Multiple Activities** dengan navigasi Intent  
✅ **Arsitektur berbasis Fragment** dengan Navigation Component  
✅ **RecyclerView** untuk list dinamis  
✅ **Background threading** dengan Executor dan Handler  
✅ **Retrofit networking** dengan integrasi API Deezer  
✅ **SQLite database** dan SharedPreferences  
✅ **Manajemen tema** dengan mode Terang/Gelap  
✅ **UI/UX profesional** dengan Material Design  
✅ **Error handling** dan dukungan offline  
✅ **Clean architecture** dan best practices  

## 📄 Lisensi

Proyek ini dikembangkan untuk tujuan pendidikan sebagai bagian dari mata kuliah Mobile Programming.

## 👨‍💻 Penulis

Dibuat dengan ❤️ untuk Proyek Akhir Mobile Lab

---

**WorshipSound** - Membawa musik rohani ke ujung jari Anda 🎵🙏
