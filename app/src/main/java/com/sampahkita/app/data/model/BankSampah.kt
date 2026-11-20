package com.sampahkita.app.data.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class BankSampah(
    val id: String,
    val nama: String,
    val alamat: String,
    val jarakKm: Double,
    /** Format "08.00–15.00". */
    val jamBuka: String,
    val kontak: String,
)

private val formatJam = DateTimeFormatter.ofPattern("H.mm")

/** true jika [waktu] berada dalam jam operasional [BankSampah.jamBuka]. */
fun BankSampah.isBukaPada(waktu: LocalTime = LocalTime.now()): Boolean {
    val bagian = jamBuka.split('–', '-').map { it.trim() }
    if (bagian.size != 2) return false
    return runCatching {
        val buka = LocalTime.parse(bagian[0], formatJam)
        val tutup = LocalTime.parse(bagian[1], formatJam)
        !waktu.isBefore(buka) && waktu.isBefore(tutup)
    }.getOrDefault(false)
}
