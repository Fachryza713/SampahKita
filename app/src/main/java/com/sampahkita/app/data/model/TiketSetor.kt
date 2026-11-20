package com.sampahkita.app.data.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
) {
    /** Tiket berlaku 24 jam sejak dibuat; format sama dengan [dibuat]. */
    val berlakuSampai: String
        get() = runCatching {
            LocalDateTime.parse(dibuat, FORMAT_WAKTU).plusHours(24).format(FORMAT_WAKTU)
        }.getOrDefault(dibuat)

    private companion object {
        val FORMAT_WAKTU: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}
