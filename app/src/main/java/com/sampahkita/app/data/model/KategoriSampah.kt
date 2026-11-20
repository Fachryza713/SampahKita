package com.sampahkita.app.data.model

/**
 * @param ikon nama ikon (kunci teks), dipetakan ke ImageVector di lapisan UI.
 */
data class KategoriSampah(
    val id: String,
    val nama: String,
    val hargaPerKg: Long,
    val ikon: String,
)
