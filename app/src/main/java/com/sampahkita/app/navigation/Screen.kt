package com.sampahkita.app.navigation

sealed class Screen(val route: String, val judul: String) {
    data object Splash : Screen("splash", "Splash")
    data object Login : Screen("login", "Masuk")

    // Nasabah
    data object Home : Screen("home", "Beranda")
    data object Peta : Screen("peta", "Peta Bank Sampah")
    data object Katalog : Screen("katalog", "Katalog Harga")
    data object TiketSetor : Screen("tiket_setor", "Setor Sampah")
    data object Tabungan : Screen("tabungan", "Buku Tabungan")
    data object Pencairan : Screen("pencairan", "Pencairan Saldo")

    // Bersama
    data object Profil : Screen("profil", "Profil")

    // Petugas
    data object HomePetugas : Screen("home_petugas", "Beranda Petugas")
    data object Scan : Screen("scan", "Scan QR")
    data object KelolaKatalog : Screen("kelola_katalog", "Kelola Katalog")
    data object ValidasiPencairan : Screen("validasi_pencairan", "Validasi Pencairan")
}
