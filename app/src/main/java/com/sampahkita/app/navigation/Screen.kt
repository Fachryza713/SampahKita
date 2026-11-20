package com.sampahkita.app.navigation

import android.net.Uri

sealed class Screen(val route: String, val judul: String) {
    data object Splash : Screen("splash", "Splash")
    data object Login : Screen("login", "Masuk")

    // Nasabah
    data object Home : Screen("home", "Beranda")
    data object Peta : Screen("peta", "Peta Bank Sampah")
    data object Katalog : Screen("katalog", "Katalog Harga")
    data object TiketSetor : Screen("tiket_setor", "Setor Sampah") {
        const val ARG_BANK_SAMPAH_ID = "bankSampahId"
        const val ARG_ITEMS = "items"

        /** Pola route dengan argumen opsional; "tiket_setor" saja tetap cocok. */
        val routePattern = "$route?$ARG_BANK_SAMPAH_ID={$ARG_BANK_SAMPAH_ID}&$ARG_ITEMS={$ARG_ITEMS}"

        /** @param items kategoriId → perkiraan berat (kg). */
        fun createRoute(bankSampahId: String? = null, items: Map<String, Double> = emptyMap()): String {
            val query = buildList {
                bankSampahId?.let { add("$ARG_BANK_SAMPAH_ID=${Uri.encode(it)}") }
                if (items.isNotEmpty()) add("$ARG_ITEMS=${Uri.encode(encodeItems(items))}")
            }
            return if (query.isEmpty()) route else route + "?" + query.joinToString("&")
        }

        /** {"k-01": 1.5, "k-02": 2.0} → "k-01:1.5,k-02:2.0". */
        fun encodeItems(items: Map<String, Double>): String =
            items.entries.joinToString(",") { "${it.key}:${it.value}" }

        fun decodeItems(teks: String?): Map<String, Double> =
            teks.orEmpty().split(',').mapNotNull { bagian ->
                val (id, kg) = bagian.split(':').takeIf { it.size == 2 } ?: return@mapNotNull null
                kg.toDoubleOrNull()?.let { id to it }
            }.toMap()
    }
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
