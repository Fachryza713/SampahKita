package com.sampahkita.app.data.model

enum class StatusTiket { MENUNGGU, SELESAI, BATAL, KEDALUWARSA }

data class ItemSetor(
    val kategoriId: String,
    val perkiraanKg: Double,
    val beratAktualKg: Double? = null,
)

data class TiketSetor(
    val id: String,
    val kodeQr: String,
    val bankSampahId: String,
    val items: List<ItemSetor>,
    val status: StatusTiket,
    /** Format "yyyy-MM-dd HH:mm". */
    val dibuat: String,
)
